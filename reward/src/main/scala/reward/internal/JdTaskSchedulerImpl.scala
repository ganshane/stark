package reward.internal

import com.jd.open.api.sdk.DefaultJdClient
import jd.union.open.order.query.request.{OrderReq, UnionOpenOrderQueryRequest}
import org.joda.time.DateTime
import org.springframework.scheduling.annotation.Scheduled
import stark.utils.services.LoggerSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-19
  */
class JdTaskSchedulerImpl extends LoggerSupport {
  private val SERVER_URL="https://router.jd.com/api"
  private val accessToken="b57733a7028b09011e441a2ec416d0da68f30ba9872b378c85f288ebea29b650f583d9e209681189"
  private val appKey="190562315b06c899931ed8d341aafc47"
  private val appSecret="0415c3ed82df420f8c700732e7ae0c1b"
  private val client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret)
  @Scheduled(fixedDelay = 100000L) //5 * 60 * 1000
  def syncJdOrder(): Unit ={
    val request = new UnionOpenOrderQueryRequest
    val orderReq = new OrderReq
    orderReq.setType(3) //按照订单更新时间查询
    orderReq.setPageSize(100)
    //TODO 从数据库得到最小时间
    orderReq.setTime(DateTime.now().toString("yyyyMMddHH")) //设置时间

    request.setOrderReq(orderReq)

    val response = client.execute(request)
    if(response.getData != null ){
      response.getData.foreach(println)
    }
    println(response.getHasMore)
  }
}
object  JdTaskSchedulerImpl{
  def main(args: Array[String]): Unit = {
    val scheduler = new JdTaskSchedulerImpl
    scheduler.syncJdOrder()
  }
}