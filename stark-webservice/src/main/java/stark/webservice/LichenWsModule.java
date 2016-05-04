package stark.webservice;

import stark.webservice.internal.EchoServiceImpl;
import stark.webservice.internal.WebServicePublisherImpl;
import stark.webservice.internal.WebserviceRequestFilter;
import stark.webservice.services.EchoService;
import stark.webservice.services.WebServicePublisher;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestHandler;

/**
 * 集成webservice
 * @author jcai
 */
public class LichenWsModule {
    public static void bind(ServiceBinder binder){
        binder.bind(EchoService.class, EchoServiceImpl.class).withSimpleId();
        binder.bind(WebServicePublisher.class, WebServicePublisherImpl.class);
    }
    @Contribute(HttpServletRequestHandler.class)
    public static void provideAxis2WebService(OrderedConfiguration<HttpServletRequestFilter> configuration){
        configuration.addInstance("axis2", WebserviceRequestFilter.class,"before:GZIP");
    }
    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(LichenWsSymbols.WEB_SERVICE_PATH, "ws");
    }
}
