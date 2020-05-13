package reward.internal.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.pdd.pop.sdk.http.api.response.PddDdkOrderListIncrementGetResponse
import javax.inject.Named
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reward.entities.CommerceOrderStatus.Type
import reward.entities.TraceOrder.{CommerceItem, CommerceType, TraceOrderStatus}
import reward.entities.{PddOrder, TraceOrder}
import reward.internal.BaseCommerceOrderServiceProcessor
import reward.services.PddService
import reward.services.PddService.CustomParameter

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
@Service
@Named("PDD")
class PddCommerceOrderService extends BaseCommerceOrderServiceProcessor[PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem, PddOrder] {
  @Autowired
  private val objectMapper: ObjectMapper = null

  /**
    * save or update origin order information.
    *
    * @param originOrder origin order
    * @return order old status,commerce order entity,new old status
    */
  override def saveOrUpdate(originOrder: PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem): (Type, PddOrder, Type) = {
    val newStatus = PddOrder.convertAsCommerceOrderStatus(originOrder.getOrderStatus)
    var oldStatus = newStatus
    val pddOrderOpt = PddOrder.find_by_orderSn(originOrder.getOrderSn) headOption
    val pddOrder = pddOrderOpt match {
      case Some(pddOrderEntity) =>
        oldStatus = pddOrderEntity.getCommerceOrderStatus
        copyProperties(pddOrderEntity, originOrder)
      case _ =>
        copyProperties(new PddOrder, originOrder)
    }
    (oldStatus, pddOrder.save(), newStatus)
  }

  /**
    * find trace order by commerce order information
    *
    * @param entity entity
    * @return TraceOrder entity
    */
  override def findTraceOrderOption(entity: PddOrder): Option[TraceOrder] = {
    val customParameterOpt: Option[CustomParameter] = {
      try {
        if (StringUtils.isEmpty(entity.customParameters)) {
          //        throw new RuntimeException("custom parameter not found! order_sn:"+entity.orderSn)
          //        only for test
          warn("custom parameter not found,order_sn:" + entity.orderSn)
          val value = "{\"uid\":\"65\"}"
          Some(objectMapper.readValue(value, classOf[PddService.CustomParameter]))
        } else Some(objectMapper.readValue(entity.customParameters, classOf[PddService.CustomParameter]))
      } catch {
        case e: Throwable =>
          logger.error("customParameters:{}", entity.customParameters)
          logger.error(e.getMessage, e)
          None
      }
    }
    val userId: Long =
      customParameterOpt match {
        case Some(customParameter) if customParameter.uid != null => customParameter.uid.toLong
        case Some(customParameter) if customParameter.showId != null => 65L //直播订单
        case _ =>
          logger.error("customParameters:{}", entity.customParameters)
          -1L
      }
    if (userId > 0) {
      val coll = TraceOrder where
        //        TraceOrder.pid === pid and
        TraceOrder.userId === userId and
        TraceOrder.item === new CommerceItem(entity.goodsId, CommerceType.PDD) and
        TraceOrder.status[TraceOrderStatus.Type] === TraceOrderStatus.NEW orderBy
        //        TraceOrder.createdAt[DateTime] < taobaoOrder.clickTime orderBy
        TraceOrder.createdAt[DateTime].desc limit 1

      coll headOption
    } else None
  }

  private def copyProperties(pddOrder: PddOrder, originOrder: PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem): PddOrder = {
    pddOrder.orderSn = originOrder.getOrderSn
    pddOrder.goodsId = originOrder.getGoodsId
    pddOrder.goodsName = originOrder.getGoodsName
    pddOrder.goodsThumbnailUrl = originOrder.getGoodsThumbnailUrl
    pddOrder.goodsQuantity = originOrder.getGoodsQuantity
    pddOrder.goodsPrice = originOrder.getGoodsPrice
    pddOrder.pddUserAmount = originOrder.getOrderAmount
    pddOrder.pId = originOrder.getPId
    pddOrder.promotionRate = originOrder.getPromotionRate / 10 //拼多多的佣金比率是1000分的整数
    pddOrder.promotionAmount = originOrder.getPromotionAmount
    pddOrder.orderStatus = originOrder.getOrderStatus
    pddOrder.orderStatusDesc = originOrder.getOrderStatusDesc
    if (originOrder.getOrderCreateTime != null)
      pddOrder.orderCreateTime = new DateTime(originOrder.getOrderCreateTime * 1000)
    if (originOrder.getOrderPayTime != null)
      pddOrder.orderPayTime = new DateTime(originOrder.getOrderPayTime * 1000)

    if (originOrder.getOrderGroupSuccessTime != null)
      pddOrder.orderGroupSuccessTime = new DateTime(originOrder.getOrderGroupSuccessTime * 1000)
    if (originOrder.getOrderVerifyTime != null)
      pddOrder.orderVerifyTime = new DateTime(originOrder.getOrderVerifyTime * 1000)
    if (originOrder.getOrderModifyAt != null)
      pddOrder.orderModifyAt = new DateTime(originOrder.getOrderModifyAt * 1000)
    pddOrder.customParameters = originOrder.getCustomParameters
    pddOrder.cpaNew = originOrder.getCpaNew
    pddOrder.`type` = originOrder.getType
    if (originOrder.getOrderSettleTime != null)
      pddOrder.orderSettleTime = new DateTime(originOrder.getOrderSettleTime * 1000)
    pddOrder.authDuoId = originOrder.getAuthDuoId
    pddOrder.batchNo = originOrder.getBatchNo
    if (originOrder.getOrderReceiveTime != null)
      pddOrder.orderReceiveTime = new DateTime(originOrder.getOrderReceiveTime * 1000)
    pddOrder.groupId = originOrder.getGroupId
    pddOrder.failReason = originOrder.getFailReason
    pddOrder.orderId = originOrder.getOrderId
    pddOrder.zsDuoId = originOrder.getZsDuoId

    pddOrder

  }
}
