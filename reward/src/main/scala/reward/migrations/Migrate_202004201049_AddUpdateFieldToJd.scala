package reward.migrations

import stark.migration.{Migration, Nullable, TimestampType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-20
  */
class Migrate_202004201049_AddUpdateFieldToJd extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    addColumn("jd_order_info","updated_at",TimestampType,Nullable)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
