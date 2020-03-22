package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-22
  */
class Migrate_202003221523_CreateAppConfig extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("app_config"){t=>
      t.column("config_key", VarcharType,Limit(50), NotNull, PrimaryKey)
      t.column("config_value", VarcharType,Limit(250), NotNull)
      t.column("created_at",TimestampType,Nullable)
      t.column("updated_at",TimestampType,Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    dropTable("app_config")
  }
}
