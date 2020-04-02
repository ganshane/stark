package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-02
  */
class Migrate_202004020913_CreateUserOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    //保持原始数据不动
    removeColumn("taobao_publisher_order","user_id")
    createTable("user_order",Comment("用户和订单的关联")){t=>
      t.column("id",BigintType, PrimaryKey,AutoIncrement)
      t.column("user_id",BigintType, NotNull,Comment("用户的ID"))
      t.column("trade_id",BigintType, NotNull,Comment("订单ID"),Unique)
      t.column("trace_time",TimestampType,Nullable,Comment("订单跟踪时候的时间戳"))
      t.column("click_time",TimestampType,Nullable,Comment("商品点击时间"))
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
