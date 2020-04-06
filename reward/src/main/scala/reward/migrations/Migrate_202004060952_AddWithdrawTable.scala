package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-06
  */
class Migrate_202004060952_AddWithdrawTable extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {

    addColumn("user_order","level",IntegerType,Comment("等级"))
    addColumn("user_order","withdraw_status",IntegerType,Comment("提现状态"))
    alterColumn("user_order","trade_id",BigintType, NotNull,Comment("订单ID"))
    addIndex("user_order",Array("user_id","trade_id"),Unique)

    createTable("user_withdraw",Comment("用户提现记录")){t=>
      t.column("id",BigintType,AutoIncrement,PrimaryKey)
      t.column("user_id",BigintType,Comment("提现用户ID"))
      t.column("user_order_id",BigintType,Comment("用户订单的ID"))
      t.column("red_pack_id",VarcharType,Limit(28),Comment("红包订单的ID"))
      t.column("level",IntegerType,Comment("用户提现订单的等级"))
      t.column("amount",IntegerType,Comment("付款金额"))
      t.column("send_result",IntegerType,Comment("发送红包结果状态"))
      t.column("send_result_code",VarcharType,Limit(32),Comment("发送红包结果错误代码"))
      t.column("send_listid",VarcharType,Limit(32),Comment("微信红包成功后的订单号"))
      t.column("apply_time",TimestampType,Nullable,Comment("申请时间"))
      t.column("success_time",TimestampType,Nullable,Comment("成功时间"))
    }
    addIndex("user_withdraw",Array("user_id","user_order_id"),Unique,Name("withdraw_user_order_id"))
    addIndex("user_withdraw","red_pack_id",Unique)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
