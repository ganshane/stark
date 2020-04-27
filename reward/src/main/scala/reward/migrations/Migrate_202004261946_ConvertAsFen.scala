package reward.migrations

import stark.migration.{IntegerType, Migration, Nullable, TimestampType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-26
  */
class Migrate_202004261946_ConvertAsFen extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    alterColumn("taobao_publisher_order","deposit_price",IntegerType)
    alterColumn("taobao_publisher_order","item_price",IntegerType)
    alterColumn("taobao_publisher_order","pay_price",IntegerType)
    alterColumn("taobao_publisher_order","tb_deposit_time",TimestampType,Nullable)
    alterColumn("taobao_publisher_order","tb_paid_time",TimestampType,Nullable)
    alterColumn("taobao_publisher_order","tk_deposit_time",TimestampType,Nullable)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
