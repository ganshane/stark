package stark.webservice.internal.axis1

import java.io.ByteArrayInputStream
import javax.jws.WebService
import javax.xml.namespace.QName

import org.apache.axis.configuration.{EngineConfigurationFactoryDefault, FileProvider}
import org.apache.axis.handlers.soap.SOAPService
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
    for (activity <- activities) {
      val serviceInterface: Class[_] = activity.getServiceInterface
      val annotation: WebService = serviceInterface.getAnnotation(classOf[WebService])
      if (annotation != null) {
        val serviceName = serviceInterface.getSimpleName
        buffer ++= template.replaceAll("SERVICE_NAME",serviceName).replaceAll("INTERFACE",serviceInterface.getName)
      }
    }

    content = content.replaceAll("###",buffer.toString())

    val provider = new FileProvider(new ByteArrayInputStream(content.getBytes())){
      override def getService(qname: QName): SOAPService = {
        //TODO 此处进行拦截
        super.getService(qname)
      }
    }
    provider
  }
//  override def getClientEngineConfig: EngineConfiguration = throw new UnsupportedOperationException
}

