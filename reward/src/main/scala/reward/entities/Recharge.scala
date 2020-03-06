package reward.entities

import io.swagger.annotations.ApiModelProperty
import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 充值记录
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@Entity
@Table(name = "recharge")
class Recharge extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @Column
  var cardNo:String= _
  @Column
  var cardSecret:String= _
  @Column
  var amount:Int= _
  @Column
  var createdAt:DateTime = _
  @ApiModelProperty(value="创建人的ID",example = "1")
  @Column
  var createdId:java.lang.Long= _
  @Column
  @ApiModelProperty(value="券所属人的ID",example = "1")
  var ownerId:java.lang.Long= _
  @Column
  var expiredAt:DateTime = _
  @Column
  var activatedAt:DateTime = _
}
object Recharge extends ActiveRecordInstance[Recharge]


