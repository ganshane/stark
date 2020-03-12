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
      t.column("phone", VarcharType, Limit(50), Nullable,Unique)
      t.column("open_id", VarcharType, Limit(50), Nullable,Unique)
      /*
       * 微信的昵称有很多是特殊字符，需要手动调整字符集
       * alter table user change nick_name nick_name varchar(50) character set utf8mb4   collate utf8mb4_unicode_ci ;
       */
      t.column("nick_name", VarcharType,Limit(50), Nullable)
      t.column("avatar", VarcharType, Limit(200), Nullable,Unique)
      t.column("relation_id", VarcharType, Limit(30), Nullable)
      t.column("invitation_id", VarcharType, Limit(20), Nullable)
      t.column("alipay", VarcharType, Limit(50), Nullable)
      t.column("alipay_name", VarcharType, Limit(50), Nullable)
      t.column("is_admin",IntegerType, Default(0))
      t.column("created_at", TimestampType,Nullable)
      t.column("updated_at", TimestampType,Nullable)
    }
    createTable("recharge",Comment("充值记录")){ t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("card_no", VarcharType, Limit(32), NotNull,Comment("卡号"),Unique)
      t.column("card_secret", VarcharType, Limit(32), Nullable,Comment("卡密码"))
      t.column("amount", IntegerType,NotNull,Comment("面值,单位:分"))
      t.column("owner_id", BigintType,Nullable,Comment("所属用户"))
      t.column("created_id", BigintType,Nullable,Comment("创建卡的用户"))
      t.column("created_at", TimestampType, Nullable)
      t.column("expired_at", TimestampType, Nullable,Comment("过期时间"))
      t.column("activated_at", TimestampType, Nullable,Comment("激活时间"))
    }
    createTable("consumption",Comment("消费记录")){ t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("amount", VarcharType, Limit(32), NotNull,Comment("消费金额，单位：分"))
      t.column("user_id", BigintType, NotNull)
      t.column("item_id", VarcharType, Limit(32), Nullable)
      t.column("item_img", IntegerType,NotNull)
      t.column("item_link", BigintType,Nullable)
      t.column("created_at", TimestampType, Nullable)
    }
    createTable("administrator",Comment("管理员")){ t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("name", VarcharType, Limit(32), NotNull)
      t.column("password", VarcharType, Limit(32), Nullable)
      t.column("created_at", TimestampType, Nullable)
    }
    createTable("online_user",Comment("在线的用户")){ t=>
      t.column("id",BigintType , NotNull, PrimaryKey,AutoIncrement)
      t.column("user_id", BigintType, NotNull,Unique)
      t.column("token", VarcharType, Limit(50), Nullable,Unique)
      t.column("created_at", TimestampType, Nullable)
      t.column("expired_at", TimestampType, Nullable)
      t.column("updated_at", TimestampType, Nullable)
    }

    execute("insert into user (phone,nick_name,is_admin) values('11111111111','jcai',1)")
    val token = "asdf"
    execute("insert into online_user(user_id,token) values(1,'"+token+"')")

    execute("insert into recharge (card_no,card_secret,amount) values('111','SSS',10000)")
    execute("insert into recharge (card_no,card_secret,amount) values('222','SSS',10000)")
  }

  /**
    * Concrete migration classes must define this method to back out of
    * this migration.  If the migration cannot be reversed, then a
    * IrreversibleMigrationException should be thrown.
    */
  override def down(): Unit = {
    dropTable("user")
    dropTable("recharge")
    dropTable("consumption")
    dropTable("administrator")
    dropTable("online_user")
  }
}
