package reward.config

import javax.xml.bind.annotation._
import stark.activerecord.config.ActiveRecordConfigSupport
import stark.utils.services.WebServerConfigSupport

/**
  * 配置文件
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@XmlRootElement(name = "stark_reward")
@XmlType(name = "starkRewardConfig")
class RewardConfig
  extends WebServerConfigSupport
  with ActiveRecordConfigSupport{
  @XmlElement(name = "log_file")
  var logFile: String = _

  @XmlElement(name = "database")
  var db: DatabaseConfig = new DatabaseConfig()
  @XmlElement(name = "wechat_mp")
  var wechat: WechatMpConfig= new WechatMpConfig()
  @XmlElement(name = "taobao")
  var taobao: ApiConfig= _
  @XmlElement(name = "aliyun")
  var aliyun: AliyunConfig= _
}
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatabaseConfig")
class DatabaseConfig extends Serializable{
  @XmlAttribute(name = "name")
  var poolName: String = _
  @XmlElement(name = "driver")
  var driver: String = _
  @XmlElement(name = "user")
  var user: String = _
  @XmlElement(name = "password")
  var password: String = _
  @XmlElement(name = "url")
  var url: String = _
  @XmlElement(name = "max_pool_size")
  var max: Int= 10
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WechatMpConfig")
class WechatMpConfig {
  @XmlElement(name = "id")
  var id: String = _
  @XmlElement(name = "secret")
  var secret: String = _
}
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AppConfig")
class ApiConfig{
  @XmlAttribute(name = "id")
  var id: String = _
  @XmlAttribute(name = "secret")
  var secret: String = _
}
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AliyunConfig")
class AliyunConfig extends ApiConfig {
  @XmlAttribute(name = "arn")
  var arn: String = _
}