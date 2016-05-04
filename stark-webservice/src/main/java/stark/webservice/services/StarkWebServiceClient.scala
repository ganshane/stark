package stark.webservice.services

import java.net.{MalformedURLException, URL}
import javax.xml.namespace.QName

import stark.utils.services.StarkException

/**
  * 基于jax-ws的webservice的客户端
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-05-04
  */
object StarkWebServiceClient {
  /**
    * 创建webservice的客户端
    *
    * @param serviceInterface 服务的接口类
    * @param wsdlURL          wsdl的url地址
    * @param namespace        命名空间
    * @param serviceName      服务名称
    * @param portName         webservice的服务入口名称
    * @tparam T               服务对象
    * @return 服务对象
    */
  def createClient[T](serviceInterface: Class[T], wsdlURL: String, namespace: String, serviceName: String, portName: String): T = {
    var url: URL = null
    try {
      url = new URL(wsdlURL)
    }
    catch {
      case e: MalformedURLException => {
        throw StarkException.wrap(e)
      }
    }
    val qName: QName = new QName(namespace, serviceName)
    val service = javax.xml.ws.Service.create(url, qName)
    service.getPort(new QName(namespace, portName), serviceInterface)
  }

  /**
    * 创建webservice的客户端,默认的port是serviceName+"HttpSoap12Endpoint"
    *
    * @param serviceInterface 服务的接口类
    * @param wsdlURL          wsdl的url地址
    * @param namespace        命名空间
    * @param serviceName      服务名称
    * @tparam T               服务对象
    * @return 服务对象
    */
  def createClient[T](serviceInterface: Class[T], wsdlURL: String, namespace: String, serviceName: String): T = {
    createClient(serviceInterface, wsdlURL, namespace, serviceName, serviceName + "HttpSoap12Endpoint")
  }

  /**
    * 创建webservice的客户端.
    * <p>
    * 默认的serviceName是接口的名称
    * 默认的port是serviceName+"HttpSoap12Endpoint"
    * </p>
    *
    * @param serviceInterface 服务的接口类
    * @param wsdlURL          wsdl的url地址
    * @param namespace        命名空间
    * @tparam T               服务对象
    * @return 服务对象
    */
  def createClient[T](serviceInterface: Class[T], wsdlURL: String, namespace: String): T = {
    createClient(serviceInterface, wsdlURL, namespace, serviceInterface.getSimpleName)
  }
}
