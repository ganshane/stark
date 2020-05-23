package reward.config

import java.util

import javax.xml.bind.annotation._
import javax.xml.bind.annotation.adapters.{XmlAdapter, XmlJavaTypeAdapter}
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
  @XmlElement(name = "aliyun")
  var aliyun: AliyunConfig= _

  @XmlElement(name = "taobao")
  @XmlJavaTypeAdapter(classOf[MyHashMapAdapter])
  var taobao:Map[String,String] = _
  @XmlElementWrapper(name = "jd")
  @XmlElement(name = "property")
  var jd = new util.ArrayList[PlatformProperty]
  @XmlElementWrapper(name = "pdd")
  @XmlElement(name = "property")
  var pdd = new util.ArrayList[PlatformProperty]
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
@XmlRootElement(name = "property")
@XmlType(name = "PlatformProperty")
class PlatformProperty {
  @XmlAttribute(name = "name")
  var name: String = null
  @XmlAttribute(name = "value")
  var value: String = null
}
final class MyHashMapWrapper{
  @XmlElement(name = "property")
  var list=  new util.ArrayList[PlatformProperty]()
}
final class MyHashMapAdapter extends XmlAdapter[MyHashMapWrapper,Map[String,String]]{
  override def unmarshal(v: MyHashMapWrapper): Map[String, String] = {
    var map = Map[String,String]()
    if(v!= null) {
      val it = v.list.iterator()
      while(it.hasNext){
        val property = it.next()
        map +=  property.name->property.value
      }
    }
    map
  }

  override def marshal(v: Map[String, String]): MyHashMapWrapper = ???
}
