package reward.services
import com.aliyuncs.auth.sts.AssumeRoleResponse
import com.taobao.api.TaobaoClient
import com.taobao.api.response.TbkOrderDetailsGetResponse.PublisherOrderDto

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
trait TaobaoService {
  def getOssAccessInfo(): AssumeRoleResponse.Credentials
  def getOrCreateTaobaoClient(): TaobaoClient
  def createOrUpdateOrder(originOrder:PublisherOrderDto): Unit
}
