package reward.migrations

import stark.migration.{Migration, Name}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-06
  */
class Migrate_202004061620_RemoveUserOrderTradeIndex extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    //在h2会报错
    try {
      removeIndex("user_order", "trade_id", Name("trade_id"))
    }catch{
      case e:Throwable =>
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
