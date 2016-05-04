package stark.webservice.services

import javax.jws.{WebMethod, WebParam, WebService}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */

@WebService(serviceName = "EchoService", targetNamespace = "http://www.egfit.com/")
trait EchoService {
  @WebMethod def echoString(@WebParam(name = "myName") text: String): String

  @WebMethod def Query(text: String): Array[String]
}
