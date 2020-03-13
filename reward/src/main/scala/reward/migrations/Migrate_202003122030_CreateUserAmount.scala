package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-12
  */
class Migrate_202003122030_CreateUserAmount extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("user_amount", Comment("用户金额汇总")) { t =>
      t.column("id", BigintType, NotNull, PrimaryKey)
      t.column("recharge_amount", BigintType, NotNull)
      t.column("consumption_amount", BigintType, NotNull)
      t.column("last_recharge",TimestampType,Nullable)
      t.column("last_consume",TimestampType,Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    dropTable("user_amount")
  }
}
