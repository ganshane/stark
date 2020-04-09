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
@Table(name = "user_statistic")
class UserStatistic extends ActiveRecord{
    @Id
    @JsonIgnore
    var userId:Long= _
    @Column
    var rechargeAmount:Long= _
    @Column
    var consumptionAmount:Long= _
    @Column
    var lastRecharge:DateTime = _
    @Column
    var lastConsume:DateTime = _

    var preWithdrawAmount:Long = _ //预估可提现金额
    var withdrawAmount:Long = _ //可提现金额
    var totalWithdrawAmount:Long = _ //已经提现金额

    var preOrderNum:Int = _
    var withdrawOrderNum:Int = _
    var totalOrderNum:Int = _

    @Column(name="level1_team_num")
    var level1TeamNum:Int = _
    @Column(name="level2_team_num")
    var level2TeamNum:Int = _

}
object UserStatistic extends ActiveRecordInstance[UserStatistic]
