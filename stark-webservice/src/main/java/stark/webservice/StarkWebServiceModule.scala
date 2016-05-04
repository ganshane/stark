package stark.webservice

import org.apache.tapestry5.ioc.annotations.Contribute
import org.apache.tapestry5.ioc.{ServiceBinder, OrderedConfiguration, MappedConfiguration}
import org.apache.tapestry5.services.{HttpServletRequestFilter, HttpServletRequestHandler}
import stark.webservice.internal.{StarkWebServiceRequestFilter, WebServicePublisherImpl, EchoServiceImpl}
import stark.webservice.services.{WebServicePublisher, EchoService}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
object StarkWebServiceModule {
  def bind(binder: ServiceBinder) {
    binder.bind(classOf[EchoService], classOf[EchoServiceImpl]).withSimpleId
    binder.bind(classOf[WebServicePublisher], classOf[WebServicePublisherImpl])
  }

  @Contribute(classOf[HttpServletRequestHandler])
  def provideAxis2WebService(configuration: OrderedConfiguration[HttpServletRequestFilter]) {
    configuration.addInstance("axis2", classOf[StarkWebServiceRequestFilter], "before:GZIP")
  }

  def contributeFactoryDefaults(configuration: MappedConfiguration[String, AnyRef]) {
    configuration.add(StarkWebServiceSymbols.WEB_SERVICE_PATH, "ws")
  }
}
