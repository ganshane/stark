/*		
 * Copyright 2010 The EGF Co,. Ltd. 
 * site: http://www.egfit.com
 * file: $Id: TapestryIOCObjectSupplier.java 685 2010-03-17 08:04:57Z jcai $
 * created at:2010-3-5
 */
package lichen.ws.internal;

import lichen.ws.LichenWsConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.ServiceObjectSupplier;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.ioc.Registry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


/**
 * tapestry ioc object supplier
 *
 * @author <a href="mailto:jun.tsai@gmail.com">Jun Tsai</a>
 * @version $Revision: 685 $
 * @since 0.1
 */
public class TapestryIOCObjectSupplier implements ServiceObjectSupplier {
    /**
     * @see org.apache.axis2.ServiceObjectSupplier#getServiceObject(org.apache.axis2.description.AxisService)
     */
    public Object getServiceObject(AxisService axisService) throws AxisFault {
        try {
            //get servlet context object
            Parameter servletConfigParam = axisService.getAxisConfiguration()
                    .getParameter(HTTPConstants.HTTP_SERVLETCONFIG);
            if (servletConfigParam == null) {
                throw new Exception("Axis2 Can't find ServletConfigParameter");
            }
            Object obj = servletConfigParam.getValue();
            ServletContext servletContext;
            if (obj instanceof ServletConfig) {
                ServletConfig servletConfig = (ServletConfig) obj;
                servletContext = servletConfig.getServletContext();
            } else {
                throw new Exception("Axis2 Can't find ServletConfig");
            }
            //get gobal registry object
            Registry registry = (Registry) servletContext.getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME);
            //get service interface
            Class serviceInterface = (Class) axisService.getParameterValue(LichenWsConstants.SERVICE_INTERFACE);
            return registry.getService(serviceInterface);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

}