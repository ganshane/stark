package reward.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-12
  */
@Entity
@Table(name = "user_amount")
class UserAmount extends ActiveRecord{
    @Id
    @JsonIgnore
    var id:Long= _
    @Column
    var rechargeAmount:Long= _
    @Column
    var consumptionAmount:Long= _
    @Column
    var lastRecharge:DateTime = _
    @Column
    var lastConsume:DateTime = _

}
object UserAmount extends ActiveRecordInstance[UserAmount]
