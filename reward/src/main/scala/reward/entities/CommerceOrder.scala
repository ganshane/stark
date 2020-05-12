package reward.entities

import com.fasterxml.jackson.annotation.{JsonGetter, JsonIgnore}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence.Transient
import org.joda.time.DateTime

/**
  * 统一的订单接口
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
trait CommerceOrder {
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  @JsonGetter("commerce_order_status")
  @Transient
  def getCommerceOrderStatus:CommerceOrderStatus.Type

  @Transient
  @JsonIgnore
  def getTradeId:Long
  @Transient
  @JsonIgnore
  def toCommerceOrderPK:CommerceOrderPK
  @Transient
  def getCommission:Int
  @Transient
  def getEstimateCommission:Int
  @Transient
  @JsonIgnore
  def getClickTime:DateTime

  //for web
  @Transient
  @JsonGetter("item_id")
  def getItemId:String
  @Transient
  @JsonGetter("item_title")
  def getItemTitle: String
  @Transient
  @JsonGetter("item_price")
  def getItemPrice: Int
  @Transient
  @JsonGetter("item_pic")
  def getItemPic: String


  @Transient
  @JsonGetter("paid_time")
  def getPaidTime: DateTime
  @Transient
  @JsonGetter("earning_time")
  def getEarningTime: DateTime

  @Transient
  @JsonGetter("shop_type")
  def getShopType: String
  @Transient
  @JsonGetter("shop_name")
  def getShopName: String

  @Transient
  @JsonGetter("order_id")
  def getOrderId: String
  @Transient
  @JsonGetter("order_amount")
  def getOrderAmount:Int
  @Transient
  @JsonGetter("item_num")
  def getItemNum : Int

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
