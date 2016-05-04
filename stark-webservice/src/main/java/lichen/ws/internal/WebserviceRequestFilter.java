package lichen.ws.internal;

import lichen.ws.LichenWsSymbols;
import lichen.ws.services.WebServicePublisher;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationGlobals;
import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestHandler;
import stark.utils.services.StarkException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 实现针对axis2的集成，便于使用tapestry能够自动导出一些webservice的服务类
 * @author jcai
 */
public class WebserviceRequestFilter implements HttpServletRequestFilter{
    private final AxisServlet _axisServlet;
    private final String _webservicePath;

    public WebserviceRequestFilter(final ApplicationGlobals applicationGlobals,
                                   final @Symbol(LichenWsSymbols.WEB_SERVICE_PATH) String webservicePath) throws ServletException {
        this._webservicePath = webservicePath;
        _axisServlet = new AxisServlet(){
            @Override
            public void init(ServletConfig config) throws ServletException {
                //add tapestry ioc object supplier
                super.init(config);
                try {
                    axisConfiguration.addParameter(Constants.SERVICE_OBJECT_SUPPLIER,TapestryIOCObjectSupplier.class.getName());
                    configContext.setServicePath(webservicePath);
                    //axisConfiguration.addParameter("EnableChildFirstClassLoading",true);
                    //get ioc registry object
                    Registry registry = (Registry) config.getServletContext().getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME);
                    if(registry != null){
                        //publish  service object annotioned by @WebService
                        WebServicePublisher publisher = registry.getService(WebServicePublisher.class);
                        publisher.registryWebServiceObject(axisConfiguration);
                    }

                } catch (AxisFault e) {
                    throw new RuntimeException(e);
                }
            }
        };
        ServletConfig config = new ServletConfig() {
            @Override
            public String getServletName() {
                return "axis";
            }

            @Override
            public ServletContext getServletContext() {
                return applicationGlobals.getServletContext();
            }

            @Override
            public String getInitParameter(String s) {
                return null;
            }

            @Override
            public Enumeration getInitParameterNames() {
                return null;
            }
        };
        _axisServlet.init(config);
    }
    @Override
    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler) throws IOException {
        if(request.getServletPath().startsWith("/"+_webservicePath)){
            try {
                _axisServlet.service(request,response);
            } catch (ServletException e) {
                throw StarkException.wrap(e);
            }
            return true;
        }
        return handler.service(request,response);
    }
}
