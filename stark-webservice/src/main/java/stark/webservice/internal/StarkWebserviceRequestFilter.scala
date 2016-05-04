package stark.webservice.internal

import java.io.IOException
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.{ServletContext, ServletConfig, ServletException}

import org.apache.axis2.{AxisFault, Constants}
import org.apache.axis2.transport.http.AxisServlet
import org.apache.tapestry5.TapestryFilter
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.annotations.Symbol
import org.apache.tapestry5.services.{HttpServletRequestHandler, ApplicationGlobals, HttpServletRequestFilter}
import stark.utils.services.StarkException
import stark.webservice.StarkWebServiceSymbols
import stark.webservice.services.WebServicePublisher
import java.util

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
class StarkWebServiceRequestFilter(applicationGlobals: ApplicationGlobals, @Symbol(StarkWebServiceSymbols.WEB_SERVICE_PATH) webservicePath: String)
  extends HttpServletRequestFilter {
  private lazy val _axisServlet = new AxisServlet() {
    @throws(classOf[ServletException])
    override def init(config: ServletConfig) {
      super.init(config)
      try {
        axisConfiguration.addParameter(Constants.SERVICE_OBJECT_SUPPLIER, classOf[TapestryServiceSupplier].getName)
        configContext.setServicePath(webservicePath)
        val registry: Registry = config.getServletContext.getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME).asInstanceOf[Registry]
        if (registry != null) {
          val publisher: WebServicePublisher = registry.getService(classOf[WebServicePublisher])
          publisher.registryWebServiceObject(axisConfiguration)
        }
      }
      catch {
        case e: AxisFault => {
          throw new RuntimeException(e)
        }
      }
    }
  }
  private lazy val config = new ServletConfig() {
    def getServletName: String =  "axis"

    def getServletContext: ServletContext =  applicationGlobals.getServletContext

    def getInitParameter(s: String): String = null

    def getInitParameterNames: util.Enumeration[String] = null
  }

  _axisServlet.init(config)

  @throws(classOf[IOException])
  def service(request: HttpServletRequest, response: HttpServletResponse, handler: HttpServletRequestHandler): Boolean = {
    if (request.getServletPath.startsWith("/" + webservicePath)) {
      try {
        _axisServlet.service(request, response)
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
