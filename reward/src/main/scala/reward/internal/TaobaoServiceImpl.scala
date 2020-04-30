package reward.internal

import java.util.Date
import java.util.concurrent.TimeUnit

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.common.utils.BinaryUtil
import com.aliyun.oss.model.{MatchMode, PolicyConditions}
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.auth.sts.{AssumeRoleRequest, AssumeRoleResponse}
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.fasterxml.jackson.databind.ObjectMapper
import com.taobao.api.{DefaultTaobaoClient, TaobaoClient}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reward.config.RewardConfig
import reward.entities.User
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
  private var credential: AssumeRoleResponse.Credentials = null
  private val CREDENTIAL_EXPIRED_DURATION=TimeUnit.HOURS.toSeconds(1) //最大一小时
  private var credentialExpiredTime = 0L

  override def getOrCreateTaobaoClient(): TaobaoClient = taobaoClient

  override def getJsClientAccessInfo(user:User)={
//    val accessId = this.config.aliyun.id //"<yourAccessKeyId>" // 请填写您的AccessKeyId。
//    val accessKey = this.config.aliyun.secret //"<yourAccessKeySecret>" // 请填写您的AccessKeySecret。
    val endpointHost = "oss-cn-qingdao.aliyuncs.com" // 请填写您的 endpoint。
    val bucket = "taofenxiang" // 请填写您的 bucketname 。
    val host = "https://" + bucket + "." + endpointHost // host的格式为 bucketname.endpoint

    val dir = "2020/"+user.id // 用户上传文件时指定的前缀。


//    val client = new OSSClient(endpoint, accessId, accessKey)
    val expireTime = 30
    val expireEndTime = System.currentTimeMillis + expireTime * 1000
    val expiration = new Date(expireEndTime)
    val policyConds = new PolicyConditions
    policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000)
//    policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir)
    policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir)

    if(this.credentialExpiredTime < System.currentTimeMillis()){
      this.initCredential()
    }
    val ossClient = new OSSClientBuilder().build(endpointHost, this.credential.getAccessKeyId,credential.getAccessKeySecret,credential.getSecurityToken)

    val postPolicy = ossClient.generatePostPolicy(expiration, policyConds)
    val binaryData = postPolicy.getBytes("utf-8")
    val encodedPolicy = BinaryUtil.toBase64String(binaryData)
    val postSignature = ossClient.calculatePostSignature(postPolicy)

    val respMap = new java.util.HashMap[String,Any]
    respMap.put("accessid", credential.getAccessKeyId)
    respMap.put("token",credential.getSecurityToken)
    respMap.put("policy", encodedPolicy)
    respMap.put("signature", postSignature)
    respMap.put("dir", dir)
    respMap.put("host", host)
    respMap.put("expire", String.valueOf(expireEndTime / 1000))
    // respMap.put("expire", formatISO8601Date(expiration));

    respMap
  }
  private def initCredential(): Unit ={
    this.credential = getOssAccessInfo()
    this.credentialExpiredTime = System.currentTimeMillis() + CREDENTIAL_EXPIRED_DURATION * 1000L - TimeUnit.MINUTES.toMillis(5)
  }

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
    request.setDurationSeconds(CREDENTIAL_EXPIRED_DURATION)
    val response = client.getAcsResponse(request)

    response.getCredentials
  }
  /*
  private def saveUserStatisticFromNewOrder(userOrder: UserOrder): Unit ={
    //更新用户状态
    val us = userService.getOrCreateUserStatistic(userOrder.userId)
    if(userOrder.withdrawStatus == WithdrawResult.CAN_APPLY) {
      us.withdrawAmount += userOrder.fee
      us.withdrawOrderNum += 1
    }else if(userOrder.withdrawStatus != WithdrawResult.UNAPPLY){
      us.preWithdrawAmount += userOrder.preFee
      us.preOrderNum += 1
    }
    //写入总订单数
    us.totalOrderNum += 1
    us.save()
  }
  @Transactional
  override def createOrUpdateOrder(originOrder:PublisherOrderDto): Unit ={
    val newStatus = TaobaoPublisherOrder.convertAsCommerceOrderStatus(originOrder.getTkStatus)
    var oldStatus = newStatus
    val taobaoOrderOpt = TaobaoPublisherOrder.findOption(originOrder.getTradeId.toLong)
    val taobaoOrder = taobaoOrderOpt match{
      case Some(taobaoOrderEntity) =>
        oldStatus = taobaoOrderEntity.getCommerceOrderStatus
        copyProperties(taobaoOrderEntity,originOrder)
      case _ =>
        copyProperties(new TaobaoPublisherOrder,originOrder)
    }
    taobaoOrder.save()
    val tradeId = taobaoOrder.tradeId
    val userOrders = UserOrder.find_by_tradeOrder(new CommerceOrder(tradeId,CommerceType.TAOBAO))
    val appConfigOpt = AppConfig.find_by_key(RewardConstants.COMMISSION_CONFIG_KEY).headOption
    val commissionConfig = appConfigOpt.map(_.readAsCommissionConfig(objectMapper)).getOrElse(new CommissionConfig)
    if(userOrders.nonEmpty) { //已经有订单匹配
      if(newStatus != oldStatus &&
        (newStatus == CommerceOrderStatus.SETTLED || newStatus == CommerceOrderStatus.FAIL)
      ) { //状态发生变化才进行处理
        userOrders.foreach(uo => {
          //如果佣金已经被支付，则需要调整提现状态
          if (newStatus == CommerceOrderStatus.SETTLED) {
            //新状态发生变化
            //收到佣金
            uo.withdrawStatus = WithdrawResult.CAN_APPLY
            uo.fee = commissionConfig.findCommissionRate(uo.level) * taobaoOrder.pubShareFee/100
            uo.save()

            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preWithdrawAmount -= uo.preFee
            us.preOrderNum -= 1
            us.withdrawOrderNum += 1
            us.withdrawAmount += uo.fee
            us.save()

          } else if (newStatus == CommerceOrderStatus.FAIL) {
            //订单关闭
            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preOrderNum -= 1
            us.preWithdrawAmount -= uo.preFee
            us.save()

            uo.withdrawStatus = WithdrawResult.UNAPPLY
            uo.preFee = 0
            uo.fee=0
            uo.save()
          }
        })
      }
    }else { //新的订单数据过来
      val pid = "mm_%s_%s_%s".format(taobaoOrder.pubId, taobaoOrder.siteId, taobaoOrder.adzoneId)
      /**
        * 通过淘宝的pid itemid clickTime 来进行定位
        */
      val coll = TraceOrder where
        TraceOrder.pid === pid and
        TraceOrder.item === new CommerceItem(taobaoOrder.itemId,CommerceType.TAOBAO) and
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
            userOrder.tradeOrder= new CommerceOrder(tradeId,CommerceType.TAOBAO)
            userOrder.level = 0
            userOrder.withdrawStatus =
              if (newStatus == CommerceOrderStatus.SETTLED){
                userOrder.fee=commissionConfig.findCommissionRate(userOrder.level) * taobaoOrder.pubShareFee /100
                WithdrawResult.CAN_APPLY
              }else if(newStatus == CommerceOrderStatus.FAIL) {
                WithdrawResult.UNAPPLY
              }else {
                userOrder.preFee = commissionConfig.findCommissionRate(userOrder.level) * taobaoOrder.pubSharePreFee /100
                WithdrawResult.PRE_APPLY
              }

            userOrder.save()

            saveUserStatisticFromNewOrder(userOrder)

            { //增加父以及爷订单
              UserRelation.find_by_userId(traceOrder.userId).foreach(ur=>{
                val order = new UserOrder
                order.clickTime = taobaoOrder.clickTime
                order.traceTime = traceOrder.createdAt
                order.userId = ur.parentId
                order.tradeOrder = new CommerceOrder(tradeId,CommerceType.TAOBAO)
                order.level = ur.level
                order.withdrawStatus = userOrder.withdrawStatus
                if(order.withdrawStatus == WithdrawResult.CAN_APPLY)
                  order.fee = commissionConfig.findCommissionRate(order.level) * taobaoOrder.pubShareFee /100
                else if(order.withdrawStatus != WithdrawResult.UNAPPLY){
                  order.preFee = commissionConfig.findCommissionRate(order.level) * taobaoOrder.pubSharePreFee /100
                }
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
            consumption.commerceType =  traceOrder.item.commerceType
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

  private val format = DateTimeFormat .forPattern(RewardConstants.TAOBAO_DATETIME_FORMAT)
  private def convertDateTime(value:String) :DateTime ={
    try{
      DateTime.parse(value, format)
    }catch{
      case _: Throwable =>
        logger.error("fail parse datetime value:"+value)
        null
    }
  }
  def copyProperties(taobaoPublisherOrder: TaobaoPublisherOrder,t:PublisherOrderDto): TaobaoPublisherOrder={
    taobaoPublisherOrder.adzoneId = t.getAdzoneId
    taobaoPublisherOrder.adzoneName = t.getAdzoneName
    taobaoPublisherOrder.alimamaRate = t.getAlimamaRate
    taobaoPublisherOrder.alimamaShareFee = t.getAlimamaShareFee
    if(!StringUtils.isEmpty(t.getAlipayTotalPrice))
      taobaoPublisherOrder.alipayTotalPrice = (t.getAlipayTotalPrice.toDouble * 100).intValue()
    taobaoPublisherOrder.alscId = t.getAlscId
    taobaoPublisherOrder.alscPid = t.getAlscPid
    if(!StringUtils.isEmpty(t.getClickTime))
      taobaoPublisherOrder.clickTime = DateTime.parse(t.getClickTime,format)
    if(!StringUtils.isEmpty(t.getDepositPrice))
      taobaoPublisherOrder.depositPrice = (t.getDepositPrice.toDouble * 100).intValue()
    taobaoPublisherOrder.flowSource = t.getFlowSource
    taobaoPublisherOrder.incomeRate = t.getIncomeRate
    taobaoPublisherOrder.itemCategoryName = t.getItemCategoryName
    taobaoPublisherOrder.itemId = t.getItemId
    taobaoPublisherOrder.itemImg = t.getItemImg
    taobaoPublisherOrder.itemLink = t.getItemLink
    taobaoPublisherOrder.itemNum = t.getItemNum
    if(!StringUtils.isEmpty(t.getItemPrice))
      taobaoPublisherOrder.itemPrice = (t.getItemPrice.toDouble * 100).intValue()
    taobaoPublisherOrder.itemTitle = t.getItemTitle
    taobaoPublisherOrder.orderType = t.getOrderType
    if(!StringUtils.isEmpty(t.getPayPrice))
      taobaoPublisherOrder.payPrice = (t.getPayPrice.toDouble * 100).intValue()
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
    if(!StringUtils.isEmpty(t.getTbDepositTime))
        taobaoPublisherOrder.tbDepositTime = convertDateTime(t.getTbDepositTime)
    if(!StringUtils.isEmpty(t.getTbPaidTime))
      taobaoPublisherOrder.tbPaidTime = convertDateTime(t.getTbPaidTime)
    taobaoPublisherOrder.terminalType = t.getTerminalType
    taobaoPublisherOrder.tkCommissionFeeForMediaPlatform = t.getTkCommissionFeeForMediaPlatform
    taobaoPublisherOrder.tkCommissionPreFeeForMediaPlatform = t.getTkCommissionPreFeeForMediaPlatform
    taobaoPublisherOrder.tkCommissionRateForMediaPlatform = t.getTkCommissionRateForMediaPlatform
    if(!StringUtils.isEmpty(t.getTkCreateTime)){
      taobaoPublisherOrder.tkCreateTime = convertDateTime(t.getTkCreateTime)
    }
    if(!StringUtils.isEmpty(t.getTkDepositTime))
      taobaoPublisherOrder.tkDepositTime = convertDateTime(t.getTkDepositTime)
    if(!StringUtils.isEmpty(t.getTkEarningTime)){
      taobaoPublisherOrder.tkEarningTime = convertDateTime(t.getTkEarningTime)
    }

    taobaoPublisherOrder.tkOrderRole = t.getTkOrderRole
    if(!StringUtils.isEmpty(t.getTkPaidTime)) {
      taobaoPublisherOrder.tkPaidTime = convertDateTime(t.getTkPaidTime)
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
  */
}
