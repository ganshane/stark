package reward.entities

import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-02
  */
@Entity
@Table(name = "user_order")
class UserOrder extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  var userId:Long=_
  var tradeId:Long=_
  var traceTime:DateTime=_
  var clickTime:DateTime=_
}
object UserOrder extends ActiveRecordInstance[UserOrder]
