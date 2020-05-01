package reward.migrations

import stark.migration.{Limit, Migration, VarcharType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-05-01
  */
class Migrate_202005011513_AdjustItemImg extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    alterColumn("taobao_publisher_order","item_img",VarcharType,Limit(200))
    alterColumn("taobao_publisher_order","item_link",VarcharType,Limit(200))
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
