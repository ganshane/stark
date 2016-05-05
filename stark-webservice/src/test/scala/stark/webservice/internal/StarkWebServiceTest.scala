package stark.webservice.internal

import javax.activation.DataHandler
import javax.servlet.ServletContext

import org.apache.axiom.attachments.ByteArrayDataSource
import org.apache.commons.io.IOUtils
import org.apache.tapestry5.TapestryFilter
import org.eclipse.jetty.server.Server
import org.junit.{Test, Assert}
import stark.utils.services.JettyServerCreator
import stark.webservice.internal.axis1.Axis1Client
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
  @Test
  def testAxis1 {
    val server: Server = JettyServerCreator.createTapestryWebapp("localhost", 12345, "stark.webservice.StarkWebServiceModule", "axis1", new WsFilter)
    server.start

    val version = Axis1Client.callRemote[String]("http://localhost:12345/services/Version","getVersion")
    Assert.assertTrue(version.indexOf("Apache Axis version: 1.4")==0)

    val text = Axis1Client.callRemote[String]("http://localhost:12345/services/EchoService","echoString","asdf")
    Assert.assertEquals("asdf",text)

    val file = new DataHandler(new ByteArrayDataSource("asdf".getBytes))
    val result = Axis1Client.callRemote[DataHandler]("http://localhost:12345/services/EchoService","TestAttachment",file)
    val resultText = IOUtils.toString(result.getInputStream)
    Assert.assertEquals("asdf",resultText)

    server.stop
  }
}
