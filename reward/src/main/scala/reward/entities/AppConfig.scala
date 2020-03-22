package reward.entities

import io.swagger.annotations.ApiModel
import javax.persistence.{Column, Entity, Id, Table}
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 系统配置
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-22
  */
@Entity
@Table(name = "app_config")
@ApiModel("用户信息")
class AppConfig extends ActiveRecord{
  @Id
  @Column(name="config_key")
  var key:String = _
  @Column(name="config_value")
  var value:String = _
  @Column
  var createdAt:DateTime = _
  @Column
  var updatedAt:DateTime = _

}
object AppConfig extends ActiveRecordInstance[AppConfig]
