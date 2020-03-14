package reward.internal

import com.taobao.api.response.TbkOrderDetailsGetResponse.PublisherOrderDto
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import reward.RewardConstants
import reward.entities.TaobaoPublisherOrder
import reward.services.TaobaoService

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@Service
class TaobaoServiceImpl extends TaobaoService{
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
