package reward.entities

import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 在线用户
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@Entity
@Table(name = "online_user")
class OnlineUser extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Int = _
  @Column
  var userId:Long = _
  @Column
  var token:String= _
  @Column
  var createdAt:DateTime = _
  @Column
  var expiredAt:DateTime = _
  @Column
  var updatedAt:DateTime = _
}

object OnlineUser extends ActiveRecordInstance[OnlineUser]

