package reward.entities

import com.fasterxml.jackson.annotation.JsonInclude
import javax.persistence._
import org.joda.time.DateTime
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
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Transient
  var order:TaobaoPublisherOrder =_
  var createdAt:DateTime = _
}

object Consumption extends ActiveRecordInstance[Consumption]


