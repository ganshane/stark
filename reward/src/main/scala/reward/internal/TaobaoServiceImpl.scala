package reward.internal

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.auth.sts.{AssumeRoleRequest, AssumeRoleResponse}
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.taobao.api.response.TbkOrderDetailsGetResponse.PublisherOrderDto
import com.taobao.api.{DefaultTaobaoClient, TaobaoClient}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Minutes}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import reward.RewardConstants
import reward.config.RewardConfig
import reward.entities.TraceOrder.TraceOrderStatus
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities._
import reward.services.{TaobaoService, WxService}
import stark.utils.services.LoggerSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@Service
class TaobaoServiceImpl extends TaobaoService with LoggerSupport{
  private lazy val taobaoClient = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", config.taobao.id, config.taobao.secret)
  private lazy val endpoint = "sts.aliyuncs.com"
  private lazy val roleSessionName = "reward"
  @Autowired
  private val config:RewardConfig  = null
  @Autowired
  private val wxService:WxService= null

  override def getOrCreateTaobaoClient(): TaobaoClient = taobaoClient


  override def getOssAccessInfo(): AssumeRoleResponse.Credentials = {
//    val accessKeyId = "LTAI4Fx8NYk2KYnBLhuTzDe5"
//    val accessKeySecret = "TQHWw3acq0rZ1mu96wM9WH9o3MtAK6"
//    val roleArn = "acs:ram::1161114981954871:role/taofenxiang-oss"
    val profile = DefaultProfile.getProfile("", config.aliyun.id, config.aliyun.secret)
    // 用profile构造client
    val client = new DefaultAcsClient(profile)
    val request = new AssumeRoleRequest
    request.setSysEndpoint(endpoint)
    request.setSysMethod(MethodType.POST)
    request.setRoleArn(config.aliyun.arn)
    request.setRoleSessionName(roleSessionName)

    client.getAcsResponse(request).getCredentials
  }
  @Transactional
  override def createOrUpdateOrder(originOrder:PublisherOrderDto): Unit ={
    val taobaoOrderOpt = TaobaoPublisherOrder.findOption(originOrder.getTradeId.toLong)
    val (taobaoOrder,updateStatus) = taobaoOrderOpt match{
      case Some(taobaoOrderEntity) =>
        val status = originOrder.getTkStatus == RewardConstants.TK_PAID_STATUS && taobaoOrderEntity.tkStatus != RewardConstants.TK_PAID_STATUS
        (copyProperties(taobaoOrderEntity,originOrder),status)
      case _ =>
        (copyProperties(new TaobaoPublisherOrder,originOrder),false)
    }
    taobaoOrder.save()
    val tradeId = taobaoOrder.tradeId
    val userOrderOption = UserOrder.find_by_tradeId(tradeId).headOption
    userOrderOption match {
      case Some(_) => //已经有订单匹配
        if(updateStatus){
          //如果佣金已经被支付，则需要调整提现状态
          UserOrder.find_by_tradeId(tradeId).foreach(uo=>{
            if(uo.withdrawStatus == WithdrawResult.UNAPPLY){
              uo.withdrawStatus = WithdrawResult.CAN_APPLY
              uo.save()
            }
          })
        }
      case None =>
        val pid = "mm_%s_%s_%s".format(taobaoOrder.pubId, taobaoOrder.siteId, taobaoOrder.adzoneId)
        val coll = TraceOrder where
          TraceOrder.pid === pid and
          TraceOrder.status[TraceOrderStatus.Type] === TraceOrderStatus.NEW and
          TraceOrder.createdAt[DateTime] < taobaoOrder.clickTime orderBy
          TraceOrder.createdAt[DateTime].desc limit 1
        coll.headOption match {
          case Some(traceOrder) =>
            val minutesDiff = Minutes.minutesBetween(taobaoOrder.clickTime,traceOrder.createdAt).getMinutes
            if(minutesDiff < 10) { //当拷贝二维码后，十分钟还未进入手机淘宝的，则忽略
              val userOrder = new UserOrder
              userOrder.clickTime = taobaoOrder.clickTime
              userOrder.traceTime = traceOrder.createdAt
              userOrder.userId = traceOrder.userId
              userOrder.tradeId = tradeId
              userOrder.level = 0
              userOrder.withdrawStatus = if(taobaoOrder.tkStatus == RewardConstants.TK_PAID_STATUS) WithdrawResult.CAN_APPLY else WithdrawResult.UNAPPLY
              userOrder.save()

              { //增加父以及爷订单
                UserRelation.find_by_userId(traceOrder.userId).foreach(ur=>{
                  val order = new UserOrder
                  order.clickTime = taobaoOrder.clickTime
                  order.traceTime = traceOrder.createdAt
                  order.userId = ur.parentId
                  order.tradeId = tradeId
                  order.level = ur.level
                  order.withdrawStatus = userOrder.withdrawStatus
                  order.save()
                })
              }
              //同时还要更新trace_order表示这条数据已经使用
              traceOrder.status = TraceOrderStatus.DETECTED
              traceOrder.detectedTime = DateTime.now()
              traceOrder.save()
              //更新消费记录
              val consumption = new Consumption
              consumption.createdAt=DateTime.now()
              consumption.amount=traceOrder.couponAmount
              consumption.tradeId = taobaoOrder.tradeId
              consumption.userId = traceOrder.userId
              consumption.save()
              //更新用户余额
              val userAmountOpt = UserAmount.findOption(traceOrder.userId)
              val userAmount = userAmountOpt match{
                case Some(ua) => ua
                case _ =>
                  val ua = new UserAmount
                  ua.id = traceOrder.userId
                  ua.rechargeAmount = 0
                  ua.consumptionAmount = 0
                  ua
              }

              userAmount.consumptionAmount += traceOrder.couponAmount
              userAmount.lastConsume = DateTime.now
              userAmount.save()
              val user = User.find(traceOrder.userId)
              wxService.sendConsumptionMessage(user.openId,userAmount,traceOrder.couponAmount)
            }
          case _ =>
        }
    }
  }

  private val format = DateTimeFormat .forPattern(RewardConstants.TAOBAO_DATETIME_FORMATE)
  def copyProperties(taobaoPublisherOrder: TaobaoPublisherOrder,t:PublisherOrderDto): TaobaoPublisherOrder={
    taobaoPublisherOrder.adzoneId = t.getAdzoneId
    taobaoPublisherOrder.adzoneName = t.getAdzoneName
    taobaoPublisherOrder.alimamaRate = t.getAlimamaRate
    taobaoPublisherOrder.alimamaShareFee = t.getAlimamaShareFee
    taobaoPublisherOrder.alipayTotalPrice = t.getAlipayTotalPrice
    taobaoPublisherOrder.alscId = t.getAlscId
    taobaoPublisherOrder.alscPid = t.getAlscPid
    if(!StringUtils.isEmpty(t.getClickTime))
      taobaoPublisherOrder.clickTime = DateTime.parse(t.getClickTime,format)
    taobaoPublisherOrder.depositPrice = t.getDepositPrice
    taobaoPublisherOrder.flowSource = t.getFlowSource
    taobaoPublisherOrder.incomeRate = t.getIncomeRate
    taobaoPublisherOrder.itemCategoryName = t.getItemCategoryName
    taobaoPublisherOrder.itemId = t.getItemId
    taobaoPublisherOrder.itemImg = t.getItemImg
    taobaoPublisherOrder.itemLink = t.getItemLink
    taobaoPublisherOrder.itemNum = t.getItemNum
    taobaoPublisherOrder.itemPrice = t.getItemPrice
    taobaoPublisherOrder.itemTitle = t.getItemTitle
    taobaoPublisherOrder.orderType = t.getOrderType
    taobaoPublisherOrder.payPrice = t.getPayPrice
    taobaoPublisherOrder.pubId = t.getPubId
    taobaoPublisherOrder.pubShareFee = t.getPubShareFee
    taobaoPublisherOrder.pubSharePreFee = t.getPubSharePreFee
    taobaoPublisherOrder.pubShareRate = t.getPubShareRate
    taobaoPublisherOrder.refundTag = t.getRefundTag
    taobaoPublisherOrder.relationId = t.getRelationId
    taobaoPublisherOrder.sellerNick = t.getSellerNick
    taobaoPublisherOrder.sellerShopTitle = t.getSellerShopTitle
    taobaoPublisherOrder.siteId = t.getSiteId
    taobaoPublisherOrder.siteName = t.getSiteName
    taobaoPublisherOrder.specialId = t.getSpecialId
    taobaoPublisherOrder.subsidyFee = t.getSubsidyFee
    taobaoPublisherOrder.subsidyRate = t.getSubsidyRate
    taobaoPublisherOrder.subsidyType = t.getSubsidyType
    taobaoPublisherOrder.tbDepositTime = t.getTbDepositTime
    taobaoPublisherOrder.tbPaidTime = t.getTbPaidTime
    taobaoPublisherOrder.terminalType = t.getTerminalType
    taobaoPublisherOrder.tkCommissionFeeForMediaPlatform = t.getTkCommissionFeeForMediaPlatform
    taobaoPublisherOrder.tkCommissionPreFeeForMediaPlatform = t.getTkCommissionPreFeeForMediaPlatform
    taobaoPublisherOrder.tkCommissionRateForMediaPlatform = t.getTkCommissionRateForMediaPlatform
    if(!StringUtils.isEmpty(t.getTkCreateTime)){
      taobaoPublisherOrder.tkCreateTime = DateTime.parse(t.getTkCreateTime,format)
    }
    taobaoPublisherOrder.tkDepositTime = t.getTkDepositTime
    if(!StringUtils.isEmpty(t.getTkEarningTime)){
      taobaoPublisherOrder.tkEarningTime = DateTime.parse(t.getTkEarningTime,format)
    }

    taobaoPublisherOrder.tkOrderRole = t.getTkOrderRole
    if(!StringUtils.isEmpty(t.getTkPaidTime)) {
      taobaoPublisherOrder.tkPaidTime = DateTime.parse(t.getTkPaidTime,format)
    }

    taobaoPublisherOrder.tkStatus = t.getTkStatus
    taobaoPublisherOrder.tkTotalRate = t.getTkTotalRate
    taobaoPublisherOrder.totalCommissionFee = t.getTotalCommissionFee
    taobaoPublisherOrder.totalCommissionRate = t.getTotalCommissionRate
    //主键
    taobaoPublisherOrder.tradeId = t.getTradeId.toLong

    taobaoPublisherOrder.tradeParentId = t.getTradeParentId
    taobaoPublisherOrder.unid = t.getUnid

    taobaoPublisherOrder
  }
}
