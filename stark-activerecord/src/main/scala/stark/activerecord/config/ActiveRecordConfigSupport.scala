package stark.activerecord.config

import javax.xml.bind.annotation.{XmlAttribute, XmlElement, XmlElementWrapper, XmlRootElement}

import java.util

/**
 * config orm
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
trait ActiveRecordConfigSupport {
  /** JPA相关配置 **/
  @XmlElementWrapper(name = "jpa")
  @XmlElement(name = "property")
  var jpaProperties  = new util.ArrayList[JpaProperty]
}

/**
 * JPA属性配置
 */
@XmlRootElement(name = "property")
class JpaProperty {
  @XmlAttribute(name = "name")
  var name: String = null
  @XmlAttribute(name = "value")
  var value: String = null
}
