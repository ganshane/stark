package reward.migrations

import stark.migration.{Default, IntegerType, Migration}

/**
  * 增加消费金额到用户订单表
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-05-14
  */
class Migrate_202005140941_AddConsumptionToUserOrder extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    addColumn("user_order","consumption_money",IntegerType,Default(0))
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
