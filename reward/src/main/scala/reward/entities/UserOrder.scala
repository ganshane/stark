package reward.entities

import com.fasterxml.jackson.annotation.{JsonProperty, JsonUnwrapped}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import javax.persistence._
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
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
  var tradeOrder:CommerceOrderPK = _

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


  @Transient
  @JsonProperty("order")
  var order:AnyRef= _
//  @Transient
//  @JsonProperty("order")
//  var jdOrder:JdOrder= _
//  @Transient
//  @JsonProperty("order")
//  var pddOrder:PddOrder = _
  @Transient
  def setTaobaoOrder(taobaoPublisherOrder: TaobaoPublisherOrder): this.type ={
    this.order= taobaoPublisherOrder
    this
  }
  @Transient
  def setJdOrder(jdOrderInfo: JdOrder): this.type ={
    this.order= jdOrderInfo
    this
  }
  @Transient
  def setPddOrder(pddOrder: PddOrder): this.type ={
    this.order= pddOrder
    this
  }
  def initCommerceOrder: this.type = {
    tradeOrder.commerceType match {
      case CommerceType.TAOBAO =>
        setTaobaoOrder(TaobaoPublisherOrder.find(tradeOrder.tradeId))
      case CommerceType.JD =>
        setJdOrder(JdOrder.find(tradeOrder.tradeId))
      case CommerceType.PDD =>
        setPddOrder(PddOrder.find(tradeOrder.tradeId))
      case other =>
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wrong commercetype " + other)
    }
  }
}

@Embeddable
class CommerceOrderPK{
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

  override def toString: String = {
    "id:%s,type:%s".format(tradeId,commerceType)
  }
}
object UserOrder extends ActiveRecordInstance[UserOrder]{
  def main(args: Array[String]): Unit = {
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)

    val jdOrder = new JdOrder
    jdOrder.validCode=13
    println(objectMapper.writeValueAsString(jdOrder))

//    objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//    objectMapper.configure(SerializationFeature.EAGER_SERIALIZER_FETCH, false)
    val order = new UserOrder
    order.id=123
    order.tradeOrder = new CommerceOrderPK(929024384375848670L,CommerceType.TAOBAO)
    order.withdrawStatus = UserWithdraw.WithdrawResult.CAN_APPLY
    println(objectMapper.writeValueAsString(order))
  }
}



