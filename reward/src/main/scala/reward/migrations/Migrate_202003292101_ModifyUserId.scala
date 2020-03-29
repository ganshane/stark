package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-29
  */
class Migrate_202003292101_ModifyUserId extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    dropTable("online_user")
    createTable("online_user",Comment("在线的用户")){ t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("user_id", BigintType)
      t.column("token", VarcharType, Limit(50), Nullable,Unique)
      t.column("created_at", TimestampType, Nullable)
      t.column("expired_at", TimestampType, Nullable)
      t.column("updated_at", TimestampType, Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {}
}
