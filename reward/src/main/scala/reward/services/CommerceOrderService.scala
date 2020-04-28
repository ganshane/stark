package reward.services

import reward.entities.{CommerceOrder, CommerceOrderStatus, TraceOrder}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
trait CommerceOrderService[O,E <: CommerceOrder] {
  /**
    * save or update origin order information.
    * @param originOrder origin order
    * @return order old status,commerce order entity,new old status
    */
  def saveOrUpdate(originOrder:O):(CommerceOrderStatus.Type,E,CommerceOrderStatus.Type)

  /**
    * find trace order by commerce order information
    * @param entity entity
    * @return TraceOrder entity
    */
  def findTraceOrderOption(entity:E):Option[TraceOrder]

  def processOrder(originOrder:O):Unit
}
