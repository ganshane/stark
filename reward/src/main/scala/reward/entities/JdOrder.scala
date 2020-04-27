package reward.entities

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.CommerceOrderStatus.Type
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 京东订单同步数据
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-19
  */
@Entity
@Table(name = "jd_order")
class JdOrder extends ActiveRecord with CommerceOrderStatusSupport{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @JsonProperty("trade_parent_id")
  var orderId:Long = _
  @JsonProperty("item_id")
  var skuId:Long = _
  @JsonProperty("tb_paid_time")
  var finishTime:DateTime= _
  var orderTime:DateTime= _

  @JsonProperty("alipay_total_price")
  var actualCosPrice :Int = _
  @JsonProperty("pub_share_fee")
  var actualFee :Int = _
  @JsonProperty("pub_share_rate")
  var commissionRate:Int = _
  var estimateCosPrice :Int = _
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
}
object JdOrder extends ActiveRecordInstance[JdOrder]{
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
