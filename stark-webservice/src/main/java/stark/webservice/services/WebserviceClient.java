package stark.webservice.services;


import stark.utils.services.StarkException;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 基于jax-ws的webservice的客户端
 * @author jcai
 */
public class WebserviceClient {
    /**
     * 创建webservice的客户端
     * @param serviceInterface 服务的接口类
     * @param wsdlURL wsdl的url地址
     * @param namespace 命名空间
     * @param serviceName 服务名称
     * @param portName webservice的服务入口名称
     * @param <T> 服务对象
     * @return 服务对象
     */
    public static <T> T createClient(Class<T> serviceInterface,
                              String wsdlURL,
                              String namespace,
                              String serviceName,
                              String portName){
        //wsdl网络路径
        URL url = null;
        try {
            url = new URL(wsdlURL);
        } catch (MalformedURLException e) {
            throw StarkException.wrap(e);
        }
        //服务描述中服务端点的限定名称  两个参数分别为 命名空间 服务名
        QName qName = new QName(namespace, serviceName);
        //创建服务对象
        javax.xml.ws.Service service = javax.xml.ws.Service.create(url, qName);
        //得到webservice对象
        return service.getPort(new QName(namespace,portName),serviceInterface);
    }

    /**
     * 创建webservice的客户端,默认的port是serviceName+"HttpSoap12Endpoint"
     * @param serviceInterface 服务的接口类
     * @param wsdlURL wsdl的url地址
     * @param namespace 命名空间
     * @param serviceName 服务名称
     * @param <T> 服务对象
     * @return 服务对象
     */
    public static <T> T createClient(Class<T> serviceInterface,
                              String wsdlURL,
                              String namespace,
                              String serviceName){
        return createClient(serviceInterface,wsdlURL,namespace,serviceName,serviceName+"HttpSoap12Endpoint");
    }
    /**
     * 创建webservice的客户端.
     * <p>
     * 默认的serviceName是接口的名称
     * 默认的port是serviceName+"HttpSoap12Endpoint"
     * </p>
     * @param serviceInterface 服务的接口类
     * @param wsdlURL wsdl的url地址
     * @param namespace 命名空间
     * @param <T> 服务对象
     * @return 服务对象
     */
    public static <T> T createClient(Class<T> serviceInterface,
                                     String wsdlURL,
                                     String namespace){
        return createClient(serviceInterface,wsdlURL,namespace,serviceInterface.getSimpleName());
    }
}
