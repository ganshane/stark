package stark.webservice.internal.axis1

import javax.xml.namespace.QName
import org.apache.axis.client.Service

/**
  * call jax-rpc
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
object Axis1Client {
  /**
    * call remote webservice (jax-rpc)
    * @param url remote url
    * @param method service method
    * @param parameters method parameter
    * @return result object
    */
  def callRemote[T](url:String ,method:String ,parameters:Any*):T={
    val service = new Service()
    val call = service.createCall()
    call.setTargetEndpointAddress(url)
    call.setOperationName(new QName(url,method))
    call.invoke(parameters.map(_.asInstanceOf[AnyRef]).toArray).asInstanceOf[T]
  }
}
