package reward.entities

import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 充值记录
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@Entity
@Table(name = "recharge")
class Recharge extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @Column
  var cardNo:String= _
  @Column
  var cardSecret:String= _
  @Column
  var amount:Int= _
  @Column
  var createdAt:DateTime = _
  @Column
  var createdId:Long= _
  @Column
  var ownerId:Long= _
  @Column
  var expiredAt:DateTime = _
  @Column
  var activatedAt:DateTime = _
}
object Recharge extends ActiveRecordInstance[Recharge]


