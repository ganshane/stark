package reward.entities

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-26
  */
trait OrderStatusSupport {
  def getOrderStatus:Int
}
object OrderStatusSupport{
  object CommerceOrderStatus extends Enumeration {
    type Type = Value
    val NEW:Type= Value(1) //等待支付
    val PAID:Type= Value(2) //
    val FAIL:Type= Value(3)
    val SETTLE:Type= Value(3)
  }
}
