package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
class Migrate_202003174403_CreateSlide extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("slide"){t=>
      t.column("id",IntegerType,PrimaryKey,AutoIncrement)
      t.column("url",VarcharType,Limit(100),NotNull)
      t.column("img_url",VarcharType,Limit(200))
      t.column("status",IntegerType,Default(0))
      t.column("created_at",TimestampType,Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    dropTable("slide")
  }
}
