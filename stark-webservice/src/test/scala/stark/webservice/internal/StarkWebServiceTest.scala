package stark.webservice.internal

import javax.servlet.ServletContext

import org.apache.tapestry5.TapestryFilter
import org.eclipse.jetty.server.Server
import org.junit.{Test, Assert}
import stark.utils.services.JettyServerCreator
import stark.webservice.services.{StarkWebServiceClient, EchoService}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
class StarkWebServiceTest {

  class WsFilter extends TapestryFilter {
    protected override def provideExtraModuleClasses(context: ServletContext): Array[Class[_]] = {
      Array[Class[_]](Class.forName("stark.webservice.StarkWebServiceModule"))
    }
  }

  @Test
  @throws(classOf[Exception])
  def testAxis2 {
    val server: Server = JettyServerCreator.createTapestryWebapp("localhost", 12345, "stark.webservice.StarkWebServiceModule", "ws", new WsFilter)
    server.start
    val hiService: EchoService = StarkWebServiceClient.createClient(classOf[EchoService], "http://localhost:12345/ws/EchoService?wsdl", "http://www.egfit.com/")
    Assert.assertEquals("xiaoming", hiService.echoString("xiaoming"))
    server.stop
  }
}
