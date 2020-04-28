package reward.entities

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModelProperty
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.CommerceOrderStatus.Type
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * https://open.taobao.com/api.htm?spm=a2e0r.13193907.0.0.233424adiQRoB7&docId=43328&docType=2
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-13
  */
@Entity
@Table(name = "taobao_publisher_order")
class TaobaoPublisherOrder extends ActiveRecord with CommerceOrderStatusSupport {
  @Column(name="adzone_id")
  @ApiModelProperty(example = "1")
  var adzoneId:java.lang.Long = _
  @Column(name="adzone_name")
  var adzoneName:String = _
  @Column(name="alimama_rate")
  var alimamaRate:String = _
  @Column(name="alimama_share_fee")
  var alimamaShareFee:String = _
  @Column(name="alipay_total_price")
  var alipayTotalPrice:Int= _
  @Column(name="alsc_id")
  var alscId:String = _
  @Column(name="alsc_pid")
  var alscPid:String = _
  @Column(name="click_time")
  var clickTime:DateTime = _
  @Column(name="deposit_price")
  var depositPrice:Int= _
  @Column(name="flow_source")
  var flowSource:String = _
  @Column(name="income_rate")
  var incomeRate:String = _
  @Column(name="item_category_name")
  var itemCategoryName:String = _
  @Column(name="item_id")
  @ApiModelProperty(example = "1")
  var itemId:java.lang.Long = _
  @Column(name="item_img")
  var itemImg:String = _
  @Column(name="item_link")
  var itemLink:String = _
  @Column(name="item_num")
  @ApiModelProperty(example = "1")
  var itemNum:java.lang.Long = _
  @Column(name="item_price")
  var itemPrice:Int= _
  @Column(name="item_title")
  var itemTitle:String = _
  @Column(name="order_type")
  var orderType:String = _
  @Column(name="pay_price")
  var payPrice:Int= _
  @Column(name="pub_id")
  @ApiModelProperty(example = "1")
  var pubId:java.lang.Long = _
  @Column(name="pub_share_fee")
  var pubShareFee:Int= _
  @Column(name="pub_share_pre_fee")
  var pubSharePreFee:Int= _
  @Column(name="pub_share_rate")
  var pubShareRate:String = _
  @Column(name="refund_tag")
  @ApiModelProperty(example = "1")
  var refundTag:java.lang.Long = _
  @Column(name="relation_id")
  @ApiModelProperty(example = "1")
  var relationId:java.lang.Long = _
  @Column(name="seller_nick")
  var sellerNick:String = _
  @Column(name="seller_shop_title")
  var sellerShopTitle:String = _
  @Column(name="site_id")
  @ApiModelProperty(example = "1")
  var siteId:java.lang.Long = _
  @Column(name="site_name")
  var siteName:String = _
  @Column(name="special_id")
  @ApiModelProperty(example = "1")
  var specialId:java.lang.Long = _
  @Column(name="subsidy_fee")
  var subsidyFee:String = _
  @Column(name="subsidy_rate")
  var subsidyRate:String = _
  @Column(name="subsidy_type")
  var subsidyType:String = _
  @Column(name="tb_deposit_time")
  var tbDepositTime:DateTime= _
  @Column(name="tb_paid_time")
  var tbPaidTime:DateTime = _
  @Column(name="terminal_type")
  var terminalType:String = _
  @Column(name="tk_commission_fee_for_media_platform")
  var tkCommissionFeeForMediaPlatform:String = _
  @Column(name="tk_commission_pre_fee_for_media_platform")
  var tkCommissionPreFeeForMediaPlatform:String = _
  @Column(name="tk_commission_rate_for_media_platform")
  var tkCommissionRateForMediaPlatform:String = _
  @Column(name="tk_create_time")
  var tkCreateTime:DateTime= _
  @Column(name="tk_deposit_time")
  var tkDepositTime:DateTime = _
  @Column(name="tk_earning_time")
  var tkEarningTime:DateTime= _
  @Column(name="tk_order_role")
  @ApiModelProperty(example = "1")
  var tkOrderRole:java.lang.Long = _
  @Column(name="tk_paid_time")
  var tkPaidTime:DateTime= _
  /*
      已付款：指订单已付款，但还未确认收货 已收货：指订单已确认收货，但商家佣金未支付 已结算：指订单已确认收货，且商家佣金已支付成功 已失效：指订单关闭/订单佣金小于0.01元，订单关闭主要有：1）买家超时未付款； 2）买家付款前，买家/卖家取消了订单；3）订单付款后发起售中退款成功；3：订单结算，12：订单付款， 13：订单失效，14：订单成功
   */
  @Column(name="tk_status")
  @ApiModelProperty(example = "1")
  var tkStatus:java.lang.Long = _
  @Column(name="tk_total_rate")
  var tkTotalRate:String = _
  @Column(name="total_commission_fee")
  var totalCommissionFee:String = _
  @Column(name="total_commission_rate")
  var totalCommissionRate:String = _
  @Id
  var tradeId:Long= _
  @Column(name="trade_parent_id")
  var tradeParentId:String = _
  @Column(name="unid")
  var unid:String = _

  @Transient
  @JsonProperty
  var userOrder:UserOrder= _
  @Transient
  def setUserOrder(userOrder: UserOrder): this.type ={
    this.userOrder = userOrder
    this
  }

  //1）买家超时未付款； 2）买家付款前，买家/卖家取消了订单；3）订单付款后发起售中退款成功；3：订单结算，12：订单付款， 13：订单失效，14：订单成功
  override def getCommerceOrderStatus: Type = {
    TaobaoPublisherOrder.convertAsCommerceOrderStatus(tkStatus)
  }
}

object TaobaoPublisherOrder extends ActiveRecordInstance[TaobaoPublisherOrder]{
  //https://open.taobao.com/api.htm?spm=a2e0r.13193907.0.0.233424adiQRoB7&docId=43328&docType=2
  //see tk_status
  def convertAsCommerceOrderStatus(tkStatus:Long): Type = {
    tkStatus.intValue() match{
      case 3 =>
        CommerceOrderStatus.SETTLED
      case 12 =>
        CommerceOrderStatus.PAID
      case 13 =>
        CommerceOrderStatus.FAIL
      case 14 =>
        CommerceOrderStatus.FINISHED
      case _ =>
        CommerceOrderStatus.UNKNOWN
    }
  }
}
