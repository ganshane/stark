package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-04
  */
class Migrate_202004041440_ModifyConsumptionLinkOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    removeColumn("consumption","item_link")
    removeColumn("consumption","item_id")
    removeColumn("consumption","item_img")
    addColumn("consumption","trade_id",BigintType)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
