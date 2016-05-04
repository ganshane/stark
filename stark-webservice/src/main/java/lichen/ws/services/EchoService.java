/*		
 * Copyright 2010 The EGF Co,. Ltd. 
 * site: http://www.egfit.com
 * file: $Id: EchoService.java 283 2010-03-11 07:41:26Z jcai $
 * created at:2010-3-5
 */
package lichen.ws.services;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * only for test
 * @author <a href="mailto:jun.tsai@gmail.com">Jun Tsai</a>
 * @version $Revision: 283 $
 * @since 0.1
 */
@WebService(serviceName="EchoService",targetNamespace="http://www.egfit.com/")
public interface EchoService {
    @WebMethod
    public abstract String echoString(@WebParam(name="myName") String text);
    @WebMethod
    public abstract String[] Query(String text);
}