package reward.migrations

import stark.migration.{Limit, Migration, VarcharType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-29
  */
class Migrate_202003292033_AddUnionIdColumn extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    addColumn("user","union_id",VarcharType,Limit(100))
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    removeColumn("user","union_id")
  }
}
