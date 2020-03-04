package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
class Migrate_202003041655_Init extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("user", Comment("用户")) { t =>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("phone", VarcharType, Limit(11), NotNull)
      t.column("name", VarcharType, Limit(20), Nullable)
      t.column("nick_name", VarcharType,Limit(20), Nullable)
      t.column("relation_id", VarcharType, Limit(30), Nullable)
      t.column("invitation_id", VarcharType, Limit(20), Nullable)
      t.column("alipay", VarcharType, Limit(50), Nullable)
      t.column("alipay_name", VarcharType, Limit(50), Nullable)
      t.column("created_at", TimestampType,Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    dropTable("user")
  }
}
