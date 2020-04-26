package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-26
  */
class Migrate_202004261552_UpdateJdOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    dropTable("jd_order_info")
    createTable("jd_order"){t=>
      t.column("id",BigintType,AutoIncrement,PrimaryKey)
      t.column("order_id",BigintType)
      t.column("sku_id",BigintType)
      t.column("order_time",TimestampType,Nullable)
      t.column("finish_time",TimestampType,Nullable)

      t.column("actual_cos_price",IntegerType)
      t.column("actual_fee",IntegerType)
      t.column("commission_rate",IntegerType)
      t.column("estimate_cos_price",IntegerType)
      t.column("estimate_fee",IntegerType)
      t.column("final_rate",IntegerType)
      t.column("cid1",BigintType)
      t.column("frozen_sku_num",BigintType)
      t.column("pid",VarcharType,Limit(50))
      t.column("position_id ",BigintType)
      t.column("price",IntegerType)
      t.column("cid2 ",BigintType)
      t.column("site_id ",BigintType)

      t.column("sku_name",VarcharType,Limit(200))
      t.column("sku_num ",BigintType)
      t.column("sku_return_num ",BigintType)
      t.column("sub_side_rate",IntegerType)
      t.column("subsidy_rate",IntegerType)
      t.column("cid3 ",BigintType)
      t.column("union_alias",VarcharType,Limit(100))
      t.column("union_tag",VarcharType,Limit(100))
      t.column("union_traffic_group",SmallintType)
      t.column("valid_code",IntegerType)
      t.column("sub_union_id",VarcharType,Limit(100))
      t.column("trace_type",SmallintType)
      t.column("pay_month",VarcharType,Limit(100))
      t.column("pop_id ",BigintType)
      t.column("ext1",VarcharType,Limit(100))
      t.column("cp_act_id ",BigintType)
      t.column("union_role",SmallintType)
      t.column("gift_coupon_key",VarcharType,Limit(100))
      t.column("gift_coupon_ocs_amount",IntegerType)
    }
    addIndex("jd_order",Array("order_id","sku_id"),Unique)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
