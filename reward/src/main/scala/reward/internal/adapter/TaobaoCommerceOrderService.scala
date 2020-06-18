package reward.internal.adapter

import com.taobao.api.response.TbkOrderDetailsGetResponse.PublisherOrderDto
import javax.inject.Named
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reward.RewardConstants
import reward.entities.CommerceOrderStatus.Type
import reward.entities.TraceOrder.{CommerceItem, CommerceType, TraceOrderStatus}
import reward.entities.{TaobaoPublisherOrder, TraceOrder}
import reward.internal.BaseCommerceOrderServiceProcessor

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
@Service
@Named("TAOBAO")
class TaobaoCommerceOrderService extends BaseCommerceOrderServiceProcessor[PublisherOrderDto,TaobaoPublisherOrder]{
  /**
    * save or update origin order information.
    *
    * @param originOrder origin order
    * @return order old status,commerce order entity,new old status
    */
  override def saveOrUpdate(originOrder: PublisherOrderDto): (Type, TaobaoPublisherOrder, Type) = {
    val newStatus = TaobaoPublisherOrder.convertAsCommerceOrderStatus(originOrder.getTkStatus)
    var oldStatus = newStatus
    val taobaoOrderOpt = TaobaoPublisherOrder.findOption(originOrder.getTradeId.toLong)
    val taobaoOrder = taobaoOrderOpt match{
      case Some(taobaoOrderEntity) =>
        oldStatus = taobaoOrderEntity.getCommerceOrderStatus
        copyProperties(taobaoOrderEntity,originOrder)
      case _ =>
        copyProperties(new TaobaoPublisherOrder(),originOrder)
    }
    (oldStatus,taobaoOrder.save(),newStatus)
  }

  /**
    * find trace order by commerce order information
    *
    * @param entity entity
    * @return TraceOrder entity
    */
  override def findTraceOrderOption(entity: TaobaoPublisherOrder): Option[TraceOrder] = {
    val coll = TraceOrder where
      TraceOrder.pid === entity.getPositionId and
      TraceOrder.item === new CommerceItem(entity.itemId,CommerceType.TAOBAO) and
      TraceOrder.status === TraceOrderStatus.NEW and
      TraceOrder.createdAt < entity.clickTime orderBy
      TraceOrder.createdAt.desc limit 1

    coll headOption
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
  private def copyProperties(taobaoPublisherOrder: TaobaoPublisherOrder,t:PublisherOrderDto): TaobaoPublisherOrder={
    taobaoPublisherOrder.adzoneId = t.getAdzoneId
    taobaoPublisherOrder.adzoneName = t.getAdzoneName
    taobaoPublisherOrder.alimamaRate = t.getAlimamaRate
    taobaoPublisherOrder.alimamaShareFee = (t.getAlimamaShareFee.toDouble * 100).intValue()
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
}
