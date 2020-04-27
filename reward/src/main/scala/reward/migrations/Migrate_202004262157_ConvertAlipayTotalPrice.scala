package reward.migrations

import stark.migration.{IntegerType, Migration}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-26
  */
class Migrate_202004262157_ConvertAlipayTotalPrice extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    alterColumn("taobao_publisher_order","alipay_total_price",IntegerType)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
