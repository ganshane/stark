package reward.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.{Column, Entity, Id, Table}
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
  @Column
  @JsonIgnore
  var id:Long= _
  var level:Int= _
  var userId:Long=_
  var parentId:Long=_
  @Column
  var createdAt:DateTime = _
}
object UserRelation extends ActiveRecordInstance[UserRelation]
