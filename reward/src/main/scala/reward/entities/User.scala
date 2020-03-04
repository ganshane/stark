package reward.entities

import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@Entity
@Table(name = "user")
class User extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Int = _
  @Column
  var phone:String= _
  @Column
  var name:String= _
  @Column
  var nickName:String= _
  @Column
  var relationId:String= _
  @Column
  var invitationId:String= _
  @Column
  var alipay:String = _
  @Column
  var alipayName:String = _
  @Column
  var createdAt:DateTime = _
}

object User extends ActiveRecordInstance[User]{
}