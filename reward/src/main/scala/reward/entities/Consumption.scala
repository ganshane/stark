package reward.entities

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.TraceOrder.{CommerceType, CommerceTypeToIntegerConverter}
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 消费记录
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@Entity
@Table(name = "consumption")
class Consumption extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @Column
  var userId:Long= _
  @Column
  var amount:Int= _
  @Column
  var tradeId:Long= _

  @Column(name="commerce_type")
  @Convert(converter = classOf[CommerceTypeToIntegerConverter])
  @JsonSerialize(using=classOf[ScalaEnumerationSerializer])
  var commerceType:CommerceType.Type=_

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Transient
  var order:AnyRef=_
  var createdAt:DateTime = _
  @Transient
  def initCommerceOrder: this.type = {
    commerceType match {
      case CommerceType.JD =>
        this.order = JdOrder.find(tradeId)
      case CommerceType.PDD =>
        this.order = PddOrder.find(tradeId)
      case _ =>
        this.order = TaobaoPublisherOrder.find(tradeId)
    }
    this
  }
}

object Consumption extends ActiveRecordInstance[Consumption]


