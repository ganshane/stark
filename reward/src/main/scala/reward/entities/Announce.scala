package reward.entities

import io.swagger.annotations.ApiModel
import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@Entity
@Table(name = "announce")
@ApiModel("系统消息")
class Announce extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @Column
  var content:String = _
  @Column
  var url:String = _
  @Column
  var createdAt:DateTime = _
}
object Announce extends ActiveRecordInstance[Announce]
