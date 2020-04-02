package reward.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.{Column, Entity, Id, Table}
import org.joda.time.DateTime
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
  @Column
  @JsonIgnore
  var id:Long= _
  var userId:Long = _
  var pid:String = _
  var createdAt:DateTime= _
}
object TraceOrder extends ActiveRecordInstance[TraceOrder]