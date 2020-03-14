package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
class Migrate_202003141503_CreateAnnounce extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("announce"){t=>
      t.column("id",IntegerType,PrimaryKey,AutoIncrement)
      t.column("content",VarcharType,Limit(100),NotNull)
      t.column("url",VarcharType,Limit(200))
      t.column("created_at",TimestampType,Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    dropTable("announce")
  }
}
