package reward.entities

import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-31
  */
@Entity
@Table(name = "user_relation")
class UserRelation extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  var level:Int= _
  var userId:Long=_
  var parentId:Long=_
  @Column
  var createdAt:DateTime = _
}
object UserRelation extends ActiveRecordInstance[UserRelation]
