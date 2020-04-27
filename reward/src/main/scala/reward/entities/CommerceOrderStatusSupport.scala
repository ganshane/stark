package reward.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence.Transient

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-26
  */
trait CommerceOrderStatusSupport {
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  @JsonProperty("commerce_order_status")
  @Transient
  def getCommerceOrderStatus:CommerceOrderStatus.Type
}
object CommerceOrderStatus extends Enumeration {
  type Type = Value
  val UNKNOWN:Type = Value(-1) //未知状态
  val NEW:Type= Value(1) //等待支付
  val PAID:Type= Value(2) // 支付完成
  val FINISHED:Type= Value(3) //订单确认收货
  val FAIL:Type= Value(4) //订单失败
  val SETTLED:Type= Value(5) //订单已经结算
}
