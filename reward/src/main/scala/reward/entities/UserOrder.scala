package reward.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.UserWithdraw.WithdrawResultConverter
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-02
  */
@Entity
@Table(name = "user_order")
class UserOrder extends ActiveRecord with Serializable{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  var userId:Long=_
//  @OneToMany(fetch = FetchType.LAZY)
//  @JoinColumn(name="userId",referencedColumnName = "userId",insertable = false,updatable = false)
//  var userRelation:java.util.List[UserRelation] =_
  @JsonProperty
  var tradeId:Long=_

  var traceTime:DateTime=_
  var clickTime:DateTime=_

  //对应提取的费用
  var preFee:Int = _
  var fee:Int = _

  //以下字段为冗余字段，方便查询
  var level:Int = _
  @Convert(converter = classOf[WithdrawResultConverter])
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  var withdrawStatus:UserWithdraw.WithdrawResult.Type =_
}
object UserOrder extends ActiveRecordInstance[UserOrder]{
  def main(args: Array[String]): Unit = {
    val objectMapper = new ObjectMapper()
    val order = new UserOrder
    order.id=123
    order.tradeId = 929024384375848670L
    order.withdrawStatus = UserWithdraw.WithdrawResult.CAN_APPLY
    objectMapper.writeValue(System.out,order)
  }
}



