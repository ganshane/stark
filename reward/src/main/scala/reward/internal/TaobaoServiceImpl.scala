package reward.internal

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.auth.sts.{AssumeRoleRequest, AssumeRoleResponse}
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.taobao.api.response.TbkOrderDetailsGetResponse.PublisherOrderDto
import com.taobao.api.{DefaultTaobaoClient, TaobaoClient}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import reward.RewardConstants
import reward.config.RewardConfig
import reward.entities.TaobaoPublisherOrder
import reward.services.TaobaoService

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@Service
class TaobaoServiceImpl(@Autowired config:RewardConfig) extends TaobaoService{
  private lazy val taobaoClient = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest", config.taobao.id, config.taobao.secret)
  private lazy val endpoint = "sts.aliyuncs.com"
  private lazy val roleSessionName = "reward"

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
    val taobaoOrderOpt = TaobaoPublisherOrder.findOption(originOrder.getTradeId)
    val taobaoOrder = taobaoOrderOpt match{
      case Some(taobaoOrderEntity) =>
        copyProperties(taobaoOrderEntity,originOrder)
      case _ =>
        copyProperties(new TaobaoPublisherOrder,originOrder)
    }
    println(taobaoOrder.tkCreateTime,taobaoOrder.totalCommissionRate)
    taobaoOrder.save
  }

  def copyProperties(taobaoPublisherOrder: TaobaoPublisherOrder,t:PublisherOrderDto): TaobaoPublisherOrder={
    val format = DateTimeFormat .forPattern(RewardConstants.TAOBAO_DATETIME_FORMATE)

    taobaoPublisherOrder.adzoneId = t.getAdzoneId
    taobaoPublisherOrder.adzoneName = t.getAdzoneName
    taobaoPublisherOrder.alimamaRate = t.getAlimamaRate
    taobaoPublisherOrder.alimamaShareFee = t.getAlimamaShareFee
    taobaoPublisherOrder.alipayTotalPrice = t.getAlipayTotalPrice
    taobaoPublisherOrder.alscId = t.getAlscId
    taobaoPublisherOrder.alscPid = t.getAlscPid
    taobaoPublisherOrder.clickTime = t.getClickTime
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
    if(StringUtils.isEmpty(taobaoPublisherOrder.tradeId))
      taobaoPublisherOrder.tradeId = t.getTradeId
    taobaoPublisherOrder.tradeParentId = t.getTradeParentId
    taobaoPublisherOrder.unid = t.getUnid

    taobaoPublisherOrder
  }
}
