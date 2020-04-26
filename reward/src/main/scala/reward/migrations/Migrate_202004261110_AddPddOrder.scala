package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-26
  */
class Migrate_202004261110_AddPddOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("pdd_order"){t=>
      t.column("id",BigintType,AutoIncrement,PrimaryKey)
      t.column("order_sn",VarcharType,Limit(200))
      t.column("goods_id",BigintType)
      t.column("goods_name",VarcharType,Limit(200))
      t.column("goods_thumbnail_url",VarcharType,Limit(200))
      t.column("goods_quantity",BigintType)
      t.column("goods_price",BigintType)
      t.column("order_amount",BigintType)
      t.column("p_id",VarcharType,Limit(200))
      t.column("promotion_rate",BigintType)
      t.column("promotion_amount",BigintType)
      t.column("order_status",IntegerType)
      t.column("order_status_desc",VarcharType,Limit(200))
      t.column("order_create_time",TimestampType,Nullable)
      t.column("order_pay_time",TimestampType,Nullable)
      t.column("order_group_success_time",TimestampType,Nullable)
      t.column("order_verify_time",TimestampType,Nullable)
      t.column("order_modify_at",TimestampType,Nullable)
      t.column("custom_parameters",VarcharType,Limit(200))
      t.column("cpa_new",IntegerType)
      t.column("promotion_type",IntegerType)
      t.column("order_settle_time",TimestampType,Nullable)
      t.column("auth_duo_id",BigintType)
      t.column("batch_no",VarcharType,Limit(200))
      t.column("order_receive_time",TimestampType,Nullable)
      t.column("group_id",BigintType)
      t.column("fail_reason",VarcharType,Limit(200))
      t.column("order_id",VarcharType,Limit(200))
      t.column("zs_duo_id",BigintType)
    }

    execute("insert into trace_order(user_id,pid,item_id,commerce_type,status) " +
      "values(2,'pid',12364804,3,0)")
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
