package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-13
  */
class Migrate_202003132336_CreateTaobaoOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("taobao_publisher_order"){t=>
      t.column("adzone_id", BigintType) // private Long adzoneId;
      t.column("adzone_name", VarcharType,Limit(100)) // private String adzoneName;
      t.column("alimama_rate", VarcharType,Limit(100)) // private String alimamaRate;
      t.column("alimama_share_fee", VarcharType,Limit(100)) // private String alimamaShareFee;
      t.column("alipay_total_price", VarcharType,Limit(100)) // private String alipayTotalPrice;
      t.column("alsc_id", VarcharType,Limit(100)) // private String alscId;
      t.column("alsc_pid", VarcharType,Limit(100)) // private String alscPid;
      t.column("click_time", VarcharType,Limit(100)) // private String clickTime;
      t.column("deposit_price", VarcharType,Limit(100)) // private String depositPrice;
      t.column("flow_source", VarcharType,Limit(100)) // private String flowSource;
      t.column("income_rate", VarcharType,Limit(100)) // private String incomeRate;
      t.column("item_category_name", VarcharType,Limit(100)) // private String itemCategoryName;
      t.column("item_id", BigintType) // private Long itemId;
      t.column("item_img", VarcharType,Limit(100)) // private String itemImg;
      t.column("item_link", VarcharType,Limit(100)) // private String itemLink;
      t.column("item_num", VarcharType,Limit(100)) // private Long itemNum;
      t.column("item_price", VarcharType,Limit(100)) // private String itemPrice;
      t.column("item_title", VarcharType,Limit(100)) // private String itemTitle;
      t.column("order_type", VarcharType,Limit(100)) // private String orderType;
      t.column("pay_price", VarcharType,Limit(100)) // private String payPrice;
      t.column("pub_id", BigintType) // private Long pubId;
      t.column("pub_share_fee", VarcharType,Limit(100)) // private String pubShareFee;
      t.column("pub_share_pre_fee", VarcharType,Limit(100)) // private String pubSharePreFee;
      t.column("pub_share_rate", VarcharType,Limit(100)) // private String pubShareRate;
      t.column("refund_tag", BigintType) // private Long refundTag;
      t.column("relation_id", BigintType) // private Long relationId;
      t.column("seller_nick", VarcharType,Limit(100)) // private String sellerNick;
      t.column("seller_shop_title", VarcharType,Limit(100)) // private String sellerShopTitle;
      t.column("site_id", BigintType) // private Long siteId;
      t.column("site_name", VarcharType,Limit(100)) // private String siteName;
      t.column("special_id", BigintType) // private Long specialId;
      t.column("subsidy_fee", VarcharType,Limit(100)) // private String subsidyFee;
      t.column("subsidy_rate", VarcharType,Limit(100)) // private String subsidyRate;
      t.column("subsidy_type", VarcharType,Limit(100)) // private String subsidyType;
      t.column("tb_deposit_time", VarcharType,Limit(100)) // private String tbDepositTime;
      t.column("tb_paid_time", VarcharType,Limit(100)) // private String tbPaidTime;
      t.column("terminal_type", VarcharType,Limit(100)) // private String terminalType;
      t.column("tk_commission_fee_for_media_platform", VarcharType,Limit(100)) // private String tkCommissionFeeForMediaPlatform;
      t.column("tk_commission_pre_fee_for_media_platform", VarcharType,Limit(100)) // private String tkCommissionPreFeeForMediaPlatform;
      t.column("tk_commission_rate_for_media_platform", VarcharType,Limit(100)) // private String tkCommissionRateForMediaPlatform;
      t.column("tk_create_time", TimestampType,Nullable) // private String tkCreateTime;
      t.column("tk_deposit_time", VarcharType,Limit(100)) // private String tkDepositTime;
      t.column("tk_earning_time", TimestampType,Nullable) // private String tkEarningTime;
      t.column("tk_order_role", BigintType) // private Long tkOrderRole;
      t.column("tk_paid_time", TimestampType,Nullable) // private String tkPaidTime;
      t.column("tk_status", BigintType) // private Long tkStatus;
      t.column("tk_total_rate", VarcharType,Limit(100)) // private String tkTotalRate;
      t.column("total_commission_fee", VarcharType,Limit(100)) // private String totalCommissionFee;
      t.column("total_commission_rate", VarcharType,Limit(100)) // private String totalCommissionRate;
      t.column("trade_id", VarcharType,Limit(100),PrimaryKey) // private String tradeId;
      t.column("trade_parent_id", VarcharType,Limit(100)) // private String tradeParentId;
      t.column("unid", VarcharType,Limit(100)) // private String unid;
    }
    addIndex("taobao_publisher_order","tk_create_time") //创建时间
    addIndex("taobao_publisher_order","tk_paid_time") //淘客付款时间
    addIndex("taobao_publisher_order","tk_earning_time") //订单确认收货后且商家完成佣金支付的时间
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
