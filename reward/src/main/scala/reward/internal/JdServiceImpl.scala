package reward.internal

import com.jd.open.api.sdk.{DefaultJdClient, JdClient}
import jd.union.open.promotion.common.get.request.{PromotionCodeReq, UnionOpenPromotionCommonGetRequest}
import org.springframework.stereotype.Service
import reward.services.JdService

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
@Service
class JdServiceImpl extends JdService{
  //jos https://api.jd.com/routerjson
  private val SERVER_URL="https://router.jd.com/api"
  private val accessToken="b57733a7028b09011e441a2ec416d0da68f30ba9872b378c85f288ebea29b650f583d9e209681189"
  private val appKey="190562315b06c899931ed8d341aafc47"
  private val appSecret="0415c3ed82df420f8c700732e7ae0c1b"
  private val client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret)
  def getClient(): JdClient = client

  def promotionUrl(): Unit ={
    val request = new UnionOpenPromotionCommonGetRequest
    val req = new PromotionCodeReq
    val couponUrl="http://coupon.m.jd.com/coupons/show.action?key=312a3c55d7ff4145ac76e8e72ef51c78&roleId=29730492&to=item.jd.com/47277785579.html"
    req.setCouponUrl(couponUrl)
    req.setMaterialId("https://item.jd.com/"+47277785579L+".html")
//    req.setSiteId("223314")
//    req.setPid("3000688743")
    req.setPositionId("3000688743".toLong)
    request.setPromotionCodeReq(req)
    val response = client.execute(request)
    println(response.getCode)
    println(response.getMessage)
    println(response.getData)
  }
}
object  JdServiceImpl{
  def main(args: Array[String]): Unit = {
    val service = new JdServiceImpl
    service.promotionUrl()
  }
}
