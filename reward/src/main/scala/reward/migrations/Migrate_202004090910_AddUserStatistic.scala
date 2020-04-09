package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-09
  */
class Migrate_202004090910_AddUserStatistic extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    dropTable("user_amount")
    createTable("user_statistic",Comment("用户统计数据")){t=>
      t.column("user_id", BigintType, NotNull, PrimaryKey)
      t.column("recharge_amount", BigintType, NotNull)
      t.column("consumption_amount", BigintType, NotNull)
      t.column("last_recharge",TimestampType,Nullable)
      t.column("last_consume",TimestampType,Nullable)

      t.column("pre_withdraw_amount",BigintType,Default(0))
      t.column("withdraw_amount",BigintType,Default(0))
      t.column("total_withdraw_amount",BigintType,Default(0))

      t.column("pre_order_num",IntegerType,Default(0))
      t.column("withdraw_order_num",IntegerType,Default(0))
      t.column("total_order_num",IntegerType,Default(0))

      t.column("level1_team_num",IntegerType,Default(0))
      t.column("level2_team_num",IntegerType,Default(0))
    }

    alterColumn("taobao_publisher_order","pub_share_fee",IntegerType,Default(0))
    alterColumn("taobao_publisher_order","pub_share_pre_fee",IntegerType,Default(0))
    addColumn("user_order","pre_fee",IntegerType,Comment("预估费用"))
    addColumn("user_order","fee",IntegerType,Comment("实际提现费用"))
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
