package reward.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.UserWithdraw.WithdrawResultConverter
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-06
  */
@Entity
@Table(name = "user_withdraw")
class UserWithdraw extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  var userId:Long=_
  var userOrderId:Long=_
  var redPackId:String=_
  var level:Int=_
  var amount:Int= _
  @Convert(converter = classOf[WithdrawResultConverter])
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  var sendResult:UserWithdraw.WithdrawResult.Type=_
  var sendResultCode:String = _
  var sendListid:String = _
  var applyTime:DateTime = _
  var successTime:DateTime = _
}
object UserWithdraw extends ActiveRecordInstance[UserWithdraw]{
  object WithdrawResult extends Enumeration{
    type Type=Value
    val UNAPPLY:Type=Value(0)
    val PRE_APPLY:Type=Value(1)
    val CAN_APPLY:Type=Value(2)
    val APPLY:Type=Value(3)
    val SUCCESS:Type=Value(4)
    val FAIL:Type=Value(5)
  }
  class WithdrawResultConverter extends ScalaEnumerationConverter[WithdrawResult.type](WithdrawResult)
}
