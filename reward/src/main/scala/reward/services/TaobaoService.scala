package reward.services
import com.aliyuncs.auth.sts.AssumeRoleResponse
import com.taobao.api.TaobaoClient

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
trait TaobaoService {
  def getJsClientAccessInfo():java.util.HashMap[String,Any]
  def getOssAccessInfo(): AssumeRoleResponse.Credentials
  def getOrCreateTaobaoClient(): TaobaoClient
//  def createOrUpdateOrder(originOrder:PublisherOrderDto): Unit
}
