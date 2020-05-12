package reward.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.CommerceOrderStatus.Type
import reward.entities.TraceOrder.CommerceType
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 京东订单同步数据
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-19
  */
@Entity
@Table(name = "jd_order")
class JdOrder extends ActiveRecord with CommerceOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @Column(name="order_id")
  @JsonProperty(value="trade_parent_id",access = JsonProperty.Access.READ_ONLY)
  var jdOrderId:Long = _
  @JsonProperty("item_id")
  var skuId:Long = _
  var finishTime:DateTime= _
  @JsonProperty("tb_paid_time")
  var orderTime:DateTime= _


  var actualCosPrice :Int = _
  @JsonProperty("pub_share_fee")
  var actualFee :Int = _
  @JsonProperty("pub_share_rate")
  var commissionRate:Int = _
  @JsonProperty("alipay_total_price")
  var estimateCosPrice :Int = _
  @Transient @JsonProperty("item_endprice")
  def getItemEndprice=this.estimateCosPrice

  @JsonProperty("pub_share_pre_fee")
  var estimateFee :Int = _
  var finalRate :Int = _
  var cid1:Long = _
  var frozenSkuNum:Long = _
  var pid :String = _
  var positionId :Long = _
  @JsonProperty("item_price")
  var price :Int = _
  var cid2 :Long = _
  var siteId :Long = _

  @JsonProperty("item_title")
  var skuName :String = _
  @JsonProperty("item_num")
  var skuNum :Long = _
  @JsonProperty("item_img")
  var itemImg:String = _
  var skuReturnNum :Long = _
  var subSideRate :Int = _
  var subsidyRate :Int = _
  var cid3 :Long = _
  var unionAlias :String = _
  var unionTag :String = _
  var unionTrafficGroup :Int= _
  var validCode :Int= _
  var subUnionId :String = _
  var traceType :Int= _
  var payMonth :String = _
  var popId :Long = _
  var ext1 :String = _
  var cpActId :Long = _
  var unionRole :Int= _
  var giftCouponKey :String = _
  var giftCouponOcsAmount :Int = _

  @JsonProperty("order_type")
  @Transient
  var orderType="jd"
//  @JsonProperty("item_img")
//  @Transient
//  def getItemImg:String={
//    "https://misc.360buyimg.com/jdf/1.0.0/unit/global-header/5.0.0/i/jdlogo-201708-@2x.png"
//  }

  //https://union.jd.com/openplatform/api/10419
  override def getCommerceOrderStatus: Type = {
    JdOrder.convertAsCommerceOrderStatus(validCode)
  }

  override def getTradeId: Long = id

  override def toCommerceOrderPK: CommerceOrderPK = new CommerceOrderPK(getTradeId,CommerceType.JD)

  override def getCommission: Int = actualFee

  override def getEstimateCommission: Int = estimateFee

  override def getClickTime: DateTime = orderTime

  override def getItemId: String = this.skuId.toString

  override def getItemTitle: String = this.skuName

  override def getItemPrice: Int = this.price

  override def getShopType: String = "JD"

  override def getShopName: String = null

  override def getOrderId: String = this.jdOrderId.toString

  override def getOrderAmount: Int =this.actualCosPrice

  override def getItemNum: Int = this.skuNum.intValue()
  def getItemPic=this.itemImg
  def getEarningTime=this.finishTime
  def getPaidTime=this.orderTime
}
object JdOrder extends ActiveRecordInstance[JdOrder]{
  def main(args: Array[String]): Unit = {
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)
    val order = new JdOrder
    order.skuName = "asdf"
    order.jdOrderId=12L
    println(objectMapper.writeValueAsString(order))
  }
  def convertAsCommerceOrderStatus(validCode:Int):CommerceOrderStatus.Type={
    validCode match{
      case 15 =>
        CommerceOrderStatus.NEW
      case 16 =>
        CommerceOrderStatus.PAID
      case 17 =>
        CommerceOrderStatus.FINISHED
      case 18 =>
        CommerceOrderStatus.SETTLED
      case other if other <= 14 =>
        CommerceOrderStatus.FAIL
      case _  =>
        CommerceOrderStatus.UNKNOWN
    }
  }

}
