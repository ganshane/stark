/*		
 * Copyright 2010 The EGF Co,. Ltd. 
 * site: http://www.egfit.com
 * file: $Id: WebServicePublisher.java 283 2010-03-11 07:41:26Z jcai $
 * created at:2010-3-2
 */
package lichen.ws.services;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * webservice publisher service
 * @author <a href="mailto:jun.tsai@gmail.com">Jun Tsai</a>
 * @version $Revision: 283 $
 * @since 0.1
 */
public interface WebServicePublisher {
    /**
     * registry web service object to axis2
     * @param axisConfiguration axis configuration
     * @throws AxisFault fail to add service
     * @since 0.1
     */
    public void registryWebServiceObject(AxisConfiguration axisConfiguration) throws AxisFault;
}