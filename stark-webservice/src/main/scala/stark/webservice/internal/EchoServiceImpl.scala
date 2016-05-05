/*		
 * Copyright 2010 The EGF Co,. Ltd. 
 * site: http://www.egfit.com
 * file: $Id: EchoServiceImpl.java 685 2010-03-17 08:04:57Z jcai $
 * created at:2010-3-4
 */
package stark.webservice.internal;


import javax.activation.DataHandler
import javax.jws.WebMethod

import stark.webservice.services.EchoService;

/**
 * only for test
 *
 * @author <a href="mailto:jun.tsai@gmail.com">Jun Tsai</a>
 * @version $Revision: 685 $
 * @since 0.1
 */
class EchoServiceImpl extends EchoService
{
  @WebMethod
  override def echoString(text: String): String = text

  @WebMethod
  override def Query(text: String): Array[String] = Array[String]("1","2","3","4")

  @WebMethod
  override def TestAttachment(file: DataHandler): DataHandler = file
}