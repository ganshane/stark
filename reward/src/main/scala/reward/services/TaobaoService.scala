package reward.services
import com.aliyuncs.auth.sts.AssumeRoleResponse
import com.taobao.api.TaobaoClient
import reward.entities.User

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
trait TaobaoService {
  def getJsClientAccessInfo(user:User):java.util.HashMap[String,Any]
  def getOssAccessInfo(): AssumeRoleResponse.Credentials
  def getOrCreateTaobaoClient(): TaobaoClient
  def tryParserTkl(messageText:String):Option[String]
//  def createOrUpdateOrder(originOrder:PublisherOrderDto): Unit
}
