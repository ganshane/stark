package reward.entities


import com.fasterxml.jackson.annotation.{JsonIgnore, JsonRawValue}
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations.ApiModel
import javax.persistence._
import org.joda.time.DateTime
import reward.entities.AppConfig.CommissionConfig
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
  @JsonRawValue
  var value:String = _
  @Column
  var createdAt:DateTime = _
  @Column
  var updatedAt:DateTime = _

  @JsonIgnore
  def readAsCommissionConfig(objectMapper: ObjectMapper):CommissionConfig={
    objectMapper.readValue(value,classOf[CommissionConfig])
  }
}
object AppConfig extends ActiveRecordInstance[AppConfig]{
  class CommissionConfig{
    var level_0:Int = _
    var level_1:Int = _
    var level_2:Int = _
    @JsonIgnore
    @Transient
    def findCommissionRate(level:Int)={
      level match{
        case 0 => level_0
        case 1=> level_1
        case 2 => level_2
        case _ =>
          throw  new IllegalStateException("level "+level+" not found")
      }
    }
  }
}
