package stark.webservice.internal.axis1

import java.io.ByteArrayInputStream
import javax.jws.WebService

import org.apache.axis.configuration.{EngineConfigurationFactoryDefault, FileProvider}
import org.apache.axis.{EngineConfiguration, EngineConfigurationFactory}
import org.apache.commons.io.IOUtils
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard

/**
  * EngineConfigurationFactory
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
object EngineConfigurationFactoryStark {
  var scoreboard:ServiceActivityScoreboard = null
  def newFactory (param: Object): EngineConfigurationFactory = {
    new EngineConfigurationFactoryStarkWithIOC(scoreboard)
  }
}
class EngineConfigurationFactoryStarkWithIOC(scoreboard: ServiceActivityScoreboard) extends EngineConfigurationFactoryDefault{
  override def getServerEngineConfig: EngineConfiguration = {
    val stream = getClass.getResourceAsStream("/stark/webservice/internal/axis1/server-config.wsdd")
    var content = IOUtils.toString(stream)
    IOUtils.closeQuietly(stream)
    val buffer = new StringBuilder
    import scala.collection.JavaConversions._
    val activities = scoreboard.getServiceActivity

    val template="""
    <service name="SERVICE_NAME" provider="java:RPC">
      <parameter name="allowedMethods" value="*"/>
      <parameter name="className" value="INTERFACE"/>
    </service>
      """.stripMargin

    /** 遍历所有添加 @WebService的接口，向axis1进行注册 **/
    for (activity <- activities) {
      val serviceInterface: Class[_] = activity.getServiceInterface
      val annotation: WebService = serviceInterface.getAnnotation(classOf[WebService])
      if (annotation != null) {
        val serviceName = serviceInterface.getSimpleName
        buffer ++= template.replaceAll("SERVICE_NAME",serviceName).replaceAll("INTERFACE",serviceInterface.getName)
      }
    }
    //生成完整的服务声明内容
    content = content.replaceAll("###",buffer.toString())
    new FileProvider(new ByteArrayInputStream(content.getBytes()))
  }
}

