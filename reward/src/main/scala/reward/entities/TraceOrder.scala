package reward.entities

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonUnwrapped}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.swagger.annotations.ApiModelProperty
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.TraceOrder.{CommerceItem, TraceOrderStatus, TraceOrderStatusToIntegerConverter}
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

  @Embedded
  @JsonUnwrapped
  var item:CommerceItem = _

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
  @Embeddable
  class CommerceItem{
    def this(itemId:Long,commerceType:CommerceType.Type){
      this()
      this.itemId= itemId
      this.commerceType = commerceType
    }

    @Column(name="item_id")
    @ApiModelProperty(example = "1")
    var itemId:java.lang.Long = _
    @Column(name="commerce_type")
    @Convert(converter = classOf[CommerceTypeToIntegerConverter])
    @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
    var commerceType: CommerceType.Type= _
  }

  object TraceOrderStatus extends Enumeration {
    type Type = Value
    val NEW:Type= Value(0)
    val DETECTED:Type= Value(1)
  }
  object CommerceType extends Enumeration{
    type Type= Value
    val TAOBAO:Type=Value(1)
    val JD:Type=Value(2)
    val PDD:Type=Value(3)
  }
  class TraceOrderStatusToIntegerConverter extends ScalaEnumerationConverter[TraceOrderStatus.type](TraceOrderStatus)
  class CommerceTypeToIntegerConverter extends ScalaEnumerationConverter[CommerceType.type](CommerceType)
}

