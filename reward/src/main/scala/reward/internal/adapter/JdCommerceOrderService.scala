package reward.internal.adapter

import javax.inject.Named
import jd.union.open.goods.promotiongoodsinfo.query.request.UnionOpenGoodsPromotiongoodsinfoQueryRequest
import jd.union.open.order.query.response.{OrderResp, SkuInfo}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reward.entities.CommerceOrderStatus.Type
import reward.entities.TraceOrder.{CommerceItem, CommerceType, TraceOrderStatus}
import reward.entities.{JdOrder, TraceOrder}
import reward.internal.BaseCommerceOrderServiceProcessor
import reward.services.JdService

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
@Service
@Named("JD")
class JdCommerceOrderService extends BaseCommerceOrderServiceProcessor[(OrderResp,SkuInfo),JdOrder]{
  @Autowired
  private val jdService:JdService = null
  /**
    * save or update origin order information.
    *
    * @param originOrder origin order
    * @return order old status,commerce order entity,new old status
    */
  override def saveOrUpdate(originOrders: (OrderResp, SkuInfo)): (Type, JdOrder, Type) = {
    val originOrder = originOrders._2
    val order = originOrders._1
    val newStatus = JdOrder.convertAsCommerceOrderStatus(originOrder.getValidCode)
    var oldStatus = newStatus
    val jdEntityOrderOpt = JdOrder.find_by_orderId_and_skuId(order.getOrderId,originOrder.getSkuId) headOption
    val jdEntityOrder = jdEntityOrderOpt match{
      case Some(pddOrderEntity) =>
        oldStatus = pddOrderEntity.getCommerceOrderStatus
        copyProperties(pddOrderEntity,order,originOrder)
      case _ =>
        copyProperties(new JdOrder,order,originOrder)
    }
    (oldStatus,jdEntityOrder.save(),newStatus)
  }

  /**
    * find trace order by commerce order information
    *
    * @param entity entity
    * @return TraceOrder entity
    */
  override def findTraceOrderOption(entity: JdOrder): Option[TraceOrder] = {
    val coll = TraceOrder where
      TraceOrder.pid === entity.positionId.toString and
      TraceOrder.item === new CommerceItem(entity.skuId,CommerceType.JD) and
      TraceOrder.status[TraceOrderStatus.Type] === TraceOrderStatus.NEW orderBy
      //        TraceOrder.createdAt[DateTime] < taobaoOrder.clickTime orderBy
      TraceOrder.createdAt[DateTime].desc limit 1

    coll headOption
  }
  private def copyProperties(jdOrder: JdOrder,order:OrderResp,originOrder: SkuInfo):JdOrder = {
    jdOrder.orderId=order.getOrderId
    jdOrder.skuId = originOrder.getSkuId
    if(order.getFinishTime > 0)
      jdOrder.finishTime=new DateTime(order.getFinishTime)
    if(order.getOrderTime > 0)
      jdOrder.orderTime=new DateTime(order.getOrderTime)
    jdOrder.actualCosPrice =(originOrder.getActualCosPrice * 100).intValue()
    jdOrder.actualFee =(originOrder.getActualFee*100).intValue()
    jdOrder.commissionRate= originOrder.getCommissionRate.intValue()
    jdOrder.estimateCosPrice =(originOrder.getEstimateCosPrice*100).intValue()
    jdOrder.estimateFee =(originOrder.getEstimateFee * 100).intValue()
    jdOrder.finalRate = originOrder.getFinalRate.intValue()
    jdOrder.cid1=originOrder.getCid1
    jdOrder.frozenSkuNum=originOrder.getFrozenSkuNum
    jdOrder.pid =originOrder.getPid
    jdOrder.positionId =originOrder.getPositionId
    jdOrder.price =(originOrder.getPrice*100).intValue()
    jdOrder.cid2 =originOrder.getCid2
    jdOrder.siteId =originOrder.getSiteId
    jdOrder.skuName =originOrder.getSkuName
    jdOrder.skuNum =originOrder.getSkuNum
    jdOrder.skuReturnNum =originOrder.getSkuReturnNum
    jdOrder.subSideRate =originOrder.getSubSideRate.intValue()
    jdOrder.subsidyRate =originOrder.getSubsidyRate.intValue()
    jdOrder.cid3 =originOrder.getCid3
    jdOrder.unionAlias =originOrder.getUnionAlias
    jdOrder.unionTag =originOrder.getUnionTag
    jdOrder.unionTrafficGroup =(originOrder.getUnionTrafficGroup).intValue()
    jdOrder.validCode =originOrder.getValidCode
    jdOrder.subUnionId =originOrder.getSubUnionId
    jdOrder.traceType =(originOrder.getTraceType).intValue()
    jdOrder.payMonth =originOrder.getPayMonth.toString
    jdOrder.popId =originOrder.getPopId
    jdOrder.ext1 =originOrder.getExt1
    jdOrder.cpActId =originOrder.getCpActId
    jdOrder.unionRole =originOrder.getUnionRole
    jdOrder.giftCouponKey =originOrder.getGiftCouponKey
    jdOrder.giftCouponOcsAmount =(originOrder.getGiftCouponOcsAmount * 100).intValue()
    //    jdOrder.updatedAt=DateTime.now

    if(StringUtils.isEmpty(jdOrder.itemImg)) {
      val request = new UnionOpenGoodsPromotiongoodsinfoQueryRequest
      request.setSkuIds(jdOrder.skuId.toString)
      val response = jdService.getClient().execute(request)
      if (response.getCode != 200) {
        throw new RuntimeException(response.getMessage)
      }
      jdOrder.itemImg = response.getData()(0).getImgUrl
    }


    jdOrder
  }
}
