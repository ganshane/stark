package reward.entities

import javax.persistence.{Column, Entity, Id, Table}
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-13
  */
@Entity
@Table(name = "taobao_publisher_order")
class TaobaoPublisherOrder extends ActiveRecord{
  @Column(name="adzone_id")
  var adzoneId:java.lang.Long = _
  @Column(name="adzone_name")
  var adzoneName:String = _
  @Column(name="alimama_rate")
  var alimamaRate:String = _
  @Column(name="alimama_share_fee")
  var alimamaShareFee:String = _
  @Column(name="alipay_total_price")
  var alipayTotalPrice:String = _
  @Column(name="alsc_id")
  var alscId:String = _
  @Column(name="alsc_pid")
  var alscPid:String = _
  @Column(name="click_time")
  var clickTime:String = _
  @Column(name="deposit_price")
  var depositPrice:String = _
  @Column(name="flow_source")
  var flowSource:String = _
  @Column(name="income_rate")
  var incomeRate:String = _
  @Column(name="item_category_name")
  var itemCategoryName:String = _
  @Column(name="item_id")
  var itemId:java.lang.Long = _
  @Column(name="item_img")
  var itemImg:String = _
  @Column(name="item_link")
  var itemLink:String = _
  @Column(name="item_num")
  var itemNum:java.lang.Long = _
  @Column(name="item_price")
  var itemPrice:String = _
  @Column(name="item_title")
  var itemTitle:String = _
  @Column(name="order_type")
  var orderType:String = _
  @Column(name="pay_price")
  var payPrice:String = _
  @Column(name="pub_id")
  var pubId:java.lang.Long = _
  @Column(name="pub_share_fee")
  var pubShareFee:String = _
  @Column(name="pub_share_pre_fee")
  var pubSharePreFee:String = _
  @Column(name="pub_share_rate")
  var pubShareRate:String = _
  @Column(name="refund_tag")
  var refundTag:java.lang.Long = _
  @Column(name="relation_id")
  var relationId:java.lang.Long = _
  @Column(name="seller_nick")
  var sellerNick:String = _
  @Column(name="seller_shop_title")
  var sellerShopTitle:String = _
  @Column(name="site_id")
  var siteId:java.lang.Long = _
  @Column(name="site_name")
  var siteName:String = _
  @Column(name="special_id")
  var specialId:java.lang.Long = _
  @Column(name="subsidy_fee")
  var subsidyFee:String = _
  @Column(name="subsidy_rate")
  var subsidyRate:String = _
  @Column(name="subsidy_type")
  var subsidyType:String = _
  @Column(name="tb_deposit_time")
  var tbDepositTime:String = _
  @Column(name="tb_paid_time")
  var tbPaidTime:String = _
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
  var tkDepositTime:String = _
  @Column(name="tk_earning_time")
  var tkEarningTime:DateTime= _
  @Column(name="tk_order_role")
  var tkOrderRole:java.lang.Long = _
  @Column(name="tk_paid_time")
  var tkPaidTime:DateTime= _
  @Column(name="tk_status")
  var tkStatus:java.lang.Long = _
  @Column(name="tk_total_rate")
  var tkTotalRate:String = _
  @Column(name="total_commission_fee")
  var totalCommissionFee:String = _
  @Column(name="total_commission_rate")
  var totalCommissionRate:String = _
  @Id
  @Column(name="trade_id")
  var tradeId:String = _
  @Column(name="trade_parent_id")
  var tradeParentId:String = _
  @Column(name="unid")
  var unid:String = _


}

object TaobaoPublisherOrder extends ActiveRecordInstance[TaobaoPublisherOrder]
