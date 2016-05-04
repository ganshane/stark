package stark.webservice.internal

import javax.jws.WebService
import javax.servlet.{ServletConfig, ServletContext}

import org.apache.axis2.description.{AxisService, Parameter}
import org.apache.axis2.engine.AxisConfiguration
import org.apache.axis2.transport.http.HTTPConstants
import org.apache.axis2.{AxisFault, ServiceObjectSupplier}
import org.apache.tapestry5.TapestryFilter
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard
import stark.webservice.StarkWebServiceConstants
import stark.webservice.services.WebServicePublisher

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
class TapestryServiceSupplier extends ServiceObjectSupplier {

  /**
    * @see org.apache.axis2.ServiceObjectSupplier#getServiceObject(org.apache.axis2.description.AxisService)
    */
  @throws(classOf[AxisFault])
  override def getServiceObject(axisService: AxisService): AnyRef = {
    try {
      val servletConfigParam: Parameter = axisService.getAxisConfiguration.getParameter(HTTPConstants.HTTP_SERVLETCONFIG)
      if (servletConfigParam == null) {
        throw new Exception("Axis2 Can't find ServletConfigParameter")
      }
      val obj: AnyRef = servletConfigParam.getValue
      var servletContext: ServletContext = null
      obj match {
        case servletConfig: ServletConfig =>
          servletContext = servletConfig.getServletContext
        case _ =>
          throw new scala.Exception("Axis2 Can't find ServletConfig")
      }
      val registry: Registry = servletContext.getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME).asInstanceOf[Registry]
      val serviceInterface: Class[_] = axisService.getParameterValue(StarkWebServiceConstants.SERVICE_INTERFACE).asInstanceOf[Class[_]]
      registry.getService(serviceInterface).asInstanceOf[AnyRef]
    }
    catch {
      case e: Exception => {
        throw AxisFault.makeFault(e)
      }
    }
  }
}

class WebServicePublisherImpl extends WebServicePublisher {
  private var scoreboard: ServiceActivityScoreboard = null

  def this(scoreboard: ServiceActivityScoreboard) {
    this()
    this.scoreboard = scoreboard
  }

  @throws(classOf[AxisFault])
  def registryWebServiceObject(axisConfiguration: AxisConfiguration) {
    import scala.collection.JavaConversions._
    val activities = scoreboard.getServiceActivity
    for (activity <- activities) {
      val serviceInterface: Class[_] = activity.getServiceInterface
      val annotation: WebService = serviceInterface.getAnnotation(classOf[WebService])
      if (annotation != null) {
        var service: AxisService = null
        service = AxisService.createService(serviceInterface.getName, axisConfiguration)
        service.addParameter(StarkWebServiceConstants.SERVICE_INTERFACE, serviceInterface)
        axisConfiguration.addService(service)
        axisConfiguration.startService(service.getName)
      }
    }
  }
}

