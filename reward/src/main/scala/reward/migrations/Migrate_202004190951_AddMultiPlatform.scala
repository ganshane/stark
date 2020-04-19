package reward.migrations

import stark.migration.{Default, Migration, SmallintType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-19
  */
class Migrate_202004190951_AddMultiPlatform extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    addColumn("trace_order","commerce_type",SmallintType,Default(1))
    addColumn("user_order","commerce_type",SmallintType,Default(1))
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
