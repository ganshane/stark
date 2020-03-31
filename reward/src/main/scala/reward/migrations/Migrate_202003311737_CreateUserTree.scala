package reward.migrations

import stark.migration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-31
  */
class Migrate_202003311737_CreateUserTree extends Migration{
  /**
    * Concrete migration classes must define this method to migrate the
    * database up to a new migration.
    */
  override def up(): Unit = {
    createTable("user_relation",Comment("用户关系表")){t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("level",IntegerType, NotNull,Comment("用户归属于parent_id时候属于哪一级"))
      t.column("user_id",BigintType, NotNull,Comment("用户的ID"))
      t.column("parent_id",BigintType, NotNull,Comment("归属父、爷用户"))
      t.column("created_at", TimestampType,Nullable)
    }
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = ???
}
