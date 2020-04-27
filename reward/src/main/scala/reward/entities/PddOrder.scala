package reward.entities

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.CommerceOrderStatus.Type
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-25
  */
@Entity
@Table(name="pdd_order")
class PddOrder extends ActiveRecord with CommerceOrderStatusSupport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @JsonProperty("trade_parent_id")var orderSn:String = _
  @JsonProperty("item_id")var goodsId:Long = _
  @JsonProperty("item_title")var goodsName:String = _
  @JsonProperty("item_img")var goodsThumbnailUrl:String = _
  @JsonProperty("item_num")var goodsQuantity:Long = _
  @JsonProperty("item_price")var goodsPrice:Long = _
  @JsonProperty("alipay_total_price")var orderAmount:Long = _
  @JsonProperty("p_id")var pId :String= _
  @JsonProperty("pub_share_rate")var promotionRate:Long = _
  @JsonProperty("pub_share_fee")var promotionAmount:Long = _
  @JsonProperty("order_status")var orderStatus :Int= _
  @JsonProperty("order_status_desc")var orderStatusDesc:String = _
  @JsonProperty("order_create_time")var orderCreateTime:DateTime= _
  @JsonProperty("tb_paid_time")var orderPayTime:DateTime= _
  @JsonProperty("order_group_success_time")var orderGroupSuccessTime:DateTime= _
  @JsonProperty("order_verify_time")var orderVerifyTime:DateTime= _
  @JsonProperty("order_modify_at")var orderModifyAt:DateTime= _
  @JsonProperty("custom_parameters")var customParameters:String = _
  @JsonProperty("cpa_new")var cpaNew:Int= _
  @JsonProperty("type")@Column(name="promotion_type")var `type`:Int= _
  @JsonProperty("order_settle_time")var orderSettleTime :DateTime= _
  @JsonProperty("auth_duo_id")var authDuoId:Long = _
  @JsonProperty("batch_no")var batchNo:String = _
  @JsonProperty("order_receive_time")var orderReceiveTime :DateTime= _
  @JsonProperty("group_id")var groupId:Long = _
  @JsonProperty("fail_reason")var failReason:String = _
  @JsonProperty("order_id")var orderId :String = _
  @JsonProperty("zs_duo_id")var zsDuoId:Long= _

  @JsonProperty("order_type")
  @Transient
  var orderType="pdd"
  @JsonProperty("pub_share_pre_fee")
  @Transient
  def pub_share_pre_fee = this.promotionAmount

  //https://open.pinduoduo.com/#/apidocument/port?portId=pdd.ddk.order.list.increment.get
  override def getCommerceOrderStatus: Type = {
    orderStatus match{
      case -1 =>
        CommerceOrderStatus.NEW
      case x if x== 0 || x == 1 =>
        CommerceOrderStatus.PAID
      case x if x == 2 || x == 3 =>
        CommerceOrderStatus.FINISHED
      case x if x == 4 || x == 8 =>
        CommerceOrderStatus.FAIL
      case 5 =>
        CommerceOrderStatus.SETTLED
      case _ =>
        CommerceOrderStatus.UNKNOWN
    }
  }
}
object PddOrder extends ActiveRecordInstance[PddOrder]
