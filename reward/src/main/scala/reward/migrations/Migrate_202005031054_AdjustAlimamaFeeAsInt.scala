package reward.migrations

import stark.migration.{IntegerType, Migration}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-05-03
  */
class Migrate_202005031054_AdjustAlimamaFeeAsInt extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    //调整为整数
    alterColumn("taobao_publisher_order","alimama_share_fee",IntegerType)
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
