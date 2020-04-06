package reward.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.TraceOrder.{TraceOrderStatus, TraceOrderStatusToIntegerConverter}
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-30
  */
@Entity
@Table(name = "trace_order")
class TraceOrder extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  var id:Long= _
  var userId:Long = _
  var pid:String = _
  //优惠券金额
  var couponAmount:Int = _
  var createdAt:DateTime= _
  @Convert(converter = classOf[TraceOrderStatusToIntegerConverter])
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  var status:TraceOrderStatus.Type = _
  var detectedTime:DateTime = _
}
object TraceOrder extends ActiveRecordInstance[TraceOrder]{

  object TraceOrderStatus extends Enumeration {
    type Type = Value
    val NEW:Type= Value(0)
    val DETECTED:Type= Value(1)
  }
  class TraceOrderStatusToIntegerConverter extends ScalaEnumerationConverter[TraceOrderStatus.type](TraceOrderStatus)
}

