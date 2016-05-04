package stark.webservice.internal.axis1

import java.io.ByteArrayInputStream
import javax.xml.namespace.QName

import org.apache.axis.configuration.FileProvider
import org.apache.axis.handlers.soap.SOAPService
import org.apache.axis.{EngineConfiguration, EngineConfigurationFactory}
import org.apache.commons.io.IOUtils

/**
  * EngineConfigurationFactory
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
object EngineConfigurationFactoryStark {
  def newFactory (param: Object): EngineConfigurationFactory = {
    new EngineConfigurationFactoryStarkWithIOC
  }
}
class EngineConfigurationFactoryStarkWithIOC extends EngineConfigurationFactory{
  override def getServerEngineConfig: EngineConfiguration = {
    val stream = getClass.getResourceAsStream("/stark/webservice/internal/axis1/server-config.wsdd")
    val content = IOUtils.toByteArray(stream)
    IOUtils.closeQuietly(stream)

    val provider = new FileProvider(new ByteArrayInputStream(content)){
      override def getService(qname: QName): SOAPService = {
        //TODO 此处进行拦截
        super.getService(qname)
      }
    }
    provider
  }
  override def getClientEngineConfig: EngineConfiguration = throw new UnsupportedOperationException
}

