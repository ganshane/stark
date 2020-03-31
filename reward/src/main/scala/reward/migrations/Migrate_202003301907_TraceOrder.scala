package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-30
  */
class Migrate_202003301907_TraceOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    alterColumn("taobao_publisher_order","click_time",TimestampType,Nullable)
    addColumn("taobao_publisher_order","user_id",BigintType,Nullable)

    createTable("trace_order",Comment("跟踪订单")){t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("user_id", BigintType,NotNull)
      t.column("pid", VarcharType, Limit(50), NotNull)
      t.column("created_at", TimestampType, Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {}
}
