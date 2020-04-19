package reward.entities

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.TraceOrder.{CommerceType, CommerceTypeToIntegerConverter}
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
//  @JsonProperty
//  var tradeId:Long=_
//  @Column(name="order_type")
//  @Convert(converter = classOf[OrderTypeToIntegerConverter])
//  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
//  var orderType:OrderType.Type=_
  @Embedded
  @JsonUnwrapped
  var tradeOrder:CommerceOrder = _

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

@Embeddable
class CommerceOrder{
  def this(tradeId:Long,commerceType:CommerceType.Type){
    this()
    this.tradeId  = tradeId
    this.commerceType = commerceType
  }
  @Column(name="trade_id")
  @JsonProperty
  var tradeId:Long = _
  @Column(name="commerce_type")
  @Convert(converter = classOf[CommerceTypeToIntegerConverter])
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  var commerceType:CommerceType.Type=_
}
object UserOrder extends ActiveRecordInstance[UserOrder]{
  def main(args: Array[String]): Unit = {
    val objectMapper = new ObjectMapper()
    val order = new UserOrder
    order.id=123
    order.tradeOrder = new CommerceOrder(929024384375848670L,CommerceType.TAOBAO)
    order.withdrawStatus = UserWithdraw.WithdrawResult.CAN_APPLY
    objectMapper.writeValue(System.out,order)
  }
}



