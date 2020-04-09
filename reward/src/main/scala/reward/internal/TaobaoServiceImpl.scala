package reward.internal

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.auth.sts.{AssumeRoleRequest, AssumeRoleResponse}
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.fasterxml.jackson.databind.ObjectMapper
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
import reward.entities.AppConfig.CommissionConfig
import reward.entities.TraceOrder.TraceOrderStatus
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities._
import reward.services.{TaobaoService, UserService, WxService}
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
  @Autowired
  private val userService:UserService = null
  @Autowired
  private val objectMapper:ObjectMapper = null

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
  private def saveUserStatisticFromNewOrder(userOrder: UserOrder): Unit ={
    //更新用户状态
    val us = userService.getOrCreateUserStatistic(userOrder.userId)
    if(userOrder.withdrawStatus == WithdrawResult.CAN_APPLY) {
      us.withdrawAmount = userOrder.fee
      us.withdrawOrderNum += 1
    }else if(userOrder.withdrawStatus != WithdrawResult.UNAPPLY){
      us.preWithdrawAmount += userOrder.preFee
      us.preOrderNum += 1
    }
    us.save()
  }
  @Transactional
  override def createOrUpdateOrder(originOrder:PublisherOrderDto): Unit ={
    val newStatus = originOrder.getTkStatus
    var oldStatus = newStatus
    val taobaoOrderOpt = TaobaoPublisherOrder.findOption(originOrder.getTradeId.toLong)
    val taobaoOrder = taobaoOrderOpt match{
      case Some(taobaoOrderEntity) =>
        oldStatus = taobaoOrderEntity.tkStatus
        copyProperties(taobaoOrderEntity,originOrder)
      case _ =>
        copyProperties(new TaobaoPublisherOrder,originOrder)
    }
    taobaoOrder.save()
    val tradeId = taobaoOrder.tradeId
    val userOrders = UserOrder.find_by_tradeId(tradeId)
    val appConfigOpt = AppConfig.find_by_key(RewardConstants.COMMISSION_CONFIG_KEY).headOption
    val commissionConfig = appConfigOpt.map(_.readAsCommissionConfig(objectMapper)).getOrElse(new CommissionConfig)
    if(userOrders.nonEmpty) { //已经有订单匹配
      if(newStatus != oldStatus &&
        (newStatus == RewardConstants.TK_PAID_STATUS || newStatus == RewardConstants.ORDER_CLOSED_STATUS)
      ) { //状态发生变化才进行处理
        userOrders.foreach(uo => {
          //如果佣金已经被支付，则需要调整提现状态
          if (newStatus == RewardConstants.TK_PAID_STATUS) {
            //新状态发生变化
            //收到佣金
            uo.withdrawStatus == WithdrawResult.CAN_APPLY
            uo.fee = commissionConfig.findCommissionRate(uo.level) * taobaoOrder.pubShareFee/100
            uo.save()

            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preOrderNum -= 1
            us.withdrawOrderNum += 1
            us.preWithdrawAmount -= uo.preFee
            us.withdrawAmount += uo.fee
            us.save()

          } else if (newStatus == RewardConstants.ORDER_CLOSED_STATUS ) {
            //订单关闭
            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preOrderNum -= 1
            us.preWithdrawAmount -= uo.preFee
            us.save()

            uo.withdrawStatus == WithdrawResult.UNAPPLY
            uo.preFee = 0
            uo.fee=0
            uo.save()
          }
        })
      }
    }else { //新的订单数据过来
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
            userOrder.preFee = commissionConfig.findCommissionRate(userOrder.level) * taobaoOrder.pubSharePreFee /100
            userOrder.withdrawStatus =
              if(taobaoOrder.tkStatus == RewardConstants.TK_PAID_STATUS)
                WithdrawResult.CAN_APPLY
              else if(newStatus == RewardConstants.ORDER_CLOSED_STATUS)
                WithdrawResult.UNAPPLY
              else WithdrawResult.PRE_APPLY

            if(userOrder.withdrawStatus == WithdrawResult.CAN_APPLY)
              userOrder.fee=commissionConfig.findCommissionRate(userOrder.level) * taobaoOrder.pubShareFee /100
            userOrder.save()

            saveUserStatisticFromNewOrder(userOrder)

            { //增加父以及爷订单
              UserRelation.find_by_userId(traceOrder.userId).foreach(ur=>{
                val order = new UserOrder
                order.clickTime = taobaoOrder.clickTime
                order.traceTime = traceOrder.createdAt
                order.userId = ur.parentId
                order.tradeId = tradeId
                order.level = ur.level
                order.withdrawStatus = userOrder.withdrawStatus
                userOrder.preFee = commissionConfig.findCommissionRate(order.level) * taobaoOrder.pubSharePreFee /100
                if(userOrder.withdrawStatus == WithdrawResult.CAN_APPLY)
                  userOrder.fee = commissionConfig.findCommissionRate(order.level) * taobaoOrder.pubShareFee /100
                order.save()

                saveUserStatisticFromNewOrder(order)
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
            val us = userService.getOrCreateUserStatistic(traceOrder.userId)
            us.consumptionAmount += traceOrder.couponAmount
            us.lastConsume = DateTime.now
            us.save()
            val user = User.find(traceOrder.userId)
            wxService.sendConsumptionMessage(user.openId,us,traceOrder.couponAmount)
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
    taobaoPublisherOrder.pubShareFee = (t.getPubShareFee.toDouble * 100).intValue()
    taobaoPublisherOrder.pubSharePreFee = (t.getPubSharePreFee.toDouble*100).intValue()
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
