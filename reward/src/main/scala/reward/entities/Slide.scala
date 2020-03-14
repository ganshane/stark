package reward.entities

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@Entity
@Table(name = "slide")
@ApiModel("用户信息")
class Slide extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Long= _
  @Column
  var imgUrl:String= _
  @Column
  var url:String= _
  @Column
  @ApiModelProperty(name="status",value="状态 0 不展示,其他值为对应的排序,数值越大，越放在前面",example = "0")
  var status:Int= _ //状态 0 不展示,其他值为对应的排序,数值越大，越放在前面
  @Column
  var createdAt:DateTime = _
}
object Slide extends ActiveRecordInstance[Slide]
