package stark.webservice.internal.axis1

import java.util
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper, HttpServletResponse}
import javax.servlet.{ServletConfig, ServletContext, ServletException}

import org.apache.axis.deployment.wsdd.providers.WSDDJavaRPCProvider
import org.apache.axis.deployment.wsdd.{WSDDConstants, WSDDProvider, WSDDService}
import org.apache.axis.providers.java.RPCProvider
import org.apache.axis.transport.http.AxisServlet
import org.apache.axis.{EngineConfiguration, EngineConfigurationFactory, Handler, MessageContext}
import org.apache.tapestry5.ioc.ObjectLocator
import org.apache.tapestry5.ioc.annotations.Symbol
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard
import org.apache.tapestry5.services.{ApplicationGlobals, HttpServletRequestFilter, HttpServletRequestHandler}
import stark.utils.services.StarkException
import stark.webservice.StarkWebServiceSymbols

/**
  * webservice based on axis1
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
class Axis1WebServiceRequestFilter (objectLocator:ObjectLocator,
                                    applicationGlobals: ApplicationGlobals,
                                    scoreboard: ServiceActivityScoreboard,
                                    @Symbol(StarkWebServiceSymbols.WEB_SERVICE_AXIS1_PATH) webservicePath: String) extends HttpServletRequestFilter{

  private lazy val _axisServlet = new AxisServlet{
    //register scoreboard
    EngineConfigurationFactoryStark.scoreboard = scoreboard
    WSDDProvider.registerProvider(WSDDConstants.QNAME_JAVARPC_PROVIDER, new StarkWSDDJavaRPCProvider(objectLocator))
    //config axis engine factory
    System.setProperty(EngineConfigurationFactory.SYSTEM_PROPERTY_NAME,Class.forName("stark.webservice.internal.axis1.EngineConfigurationFactoryStark").getName)
    init(config)
  }
  private lazy val config = new ServletConfig() {
    def getServletName: String =  "axis"

    def getServletContext: ServletContext =  applicationGlobals.getServletContext

    def getInitParameter(s: String): String = {
      s match{
        case AxisServlet.INIT_PROPERTY_SERVICES_PATH =>
          "/"+webservicePath+"/"
        case AxisServlet.INIT_PROPERTY_DISABLE_SERVICES_LIST=>
          "false"
        //case INIT_PROPERTY_DEVELOPMENT_SYSTEM=>
        case other=>
          null
      }
    }

    def getInitParameterNames: util.Enumeration[String] = null
  }

  private lazy val regPattern=("/"+webservicePath+"/([a-z0-9A-Z]+)[^$]*").r
  override def service(request: HttpServletRequest, response: HttpServletResponse, handler: HttpServletRequestHandler): Boolean = {
    if (request.getServletPath.startsWith("/" + webservicePath)) {
      try {
        val requestDelegated = new HttpServletRequestWrapper(request){
          override def getPathInfo: String = {
            val path = request.getServletPath
            path match{
              case regPattern(f)=>
                f
              case other=>
                null
            }
          }
        }
        _axisServlet.service(requestDelegated, response)
      }
      catch {
        case e: ServletException => {
          throw StarkException.wrap(e)
        }
      }
      return true
    }
    handler.service(request, response)
  }
}

class StarkWSDDJavaRPCProvider(objectLocator: ObjectLocator) extends WSDDJavaRPCProvider{
  override def newProviderInstance(service: WSDDService, registry: EngineConfiguration): Handler = {
    new StarkRpcProvider(objectLocator)
  }
}
class StarkRpcProvider(objectLocator:ObjectLocator) extends RPCProvider{
  override def makeNewServiceObject(msgContext: MessageContext, clsName: String): AnyRef = {
    clsName match{
      case "org.apache.axis.Version" | "org.apache.axis.utils.Admin" =>
        super.makeNewServiceObject(msgContext,clsName)
      case other=>
        objectLocator.getService(Class.forName(clsName)).asInstanceOf[AnyRef]
    }
  }
}
