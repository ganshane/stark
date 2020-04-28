package reward.internal

import java.util.concurrent.TimeUnit

import com.pdd.pop.sdk.http.api.request.PddDdkOrderListIncrementGetRequest
import com.pdd.pop.sdk.http.api.response.PddDdkOrderListIncrementGetResponse
import com.taobao.api.request.TbkOrderDetailsGetRequest
import com.taobao.api.response.TbkOrderDetailsGetResponse
import com.taobao.api.response.TbkOrderDetailsGetResponse.PublisherOrderDto
import javax.inject.Named
import jd.union.open.order.query.request.{OrderReq, UnionOpenOrderQueryRequest}
import jd.union.open.order.query.response.{OrderResp, SkuInfo}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reward.config.RewardConfig
import reward.entities.{JdOrder, PddOrder, TaobaoPublisherOrder}
import reward.services._
import stark.activerecord.services.DSL.select
import stark.utils.services.LoggerSupport

import scala.annotation.tailrec

/**
  * 操作淘宝的API
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-13
  */
@Service
class SyncCommerceOrderSchedulerImpl extends SyncCommerceOrderScheduler with LoggerSupport{
  @Autowired
  private val taobaoService:TaobaoService = null
  @Autowired
  private var pddService:PddService = null
  @Autowired
  private var jdService:JdService= null
  @Autowired
  @Named("TAOBAO")
  private val taobaoCommerceOrderService:CommerceOrderService[PublisherOrderDto,TaobaoPublisherOrder]= null
  @Autowired
  @Named("PDD")
  private val pddCommerceOrderService:CommerceOrderService[PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem,PddOrder]= null
  @Autowired
  @Named("JD")
  private val jdCommerceOrderService:CommerceOrderService[(OrderResp,SkuInfo),JdOrder]= null
  @Autowired
  private val config:RewardConfig = null
//  val TaoAppKey = "27706268"
//  val TaoSecret = "6505ff50b4d72c56fef583b2570593fa"
  //  val TaoAppKey = "28430902"
  //  val TaoSecret = "c982412aeb22bccc535b918de4165a3a"

  @Scheduled(fixedDelay = 100000L) //5 * 60 * 1000
  def sync(): Unit ={
    try { this.syncTaobaoOrder()
    }catch{case e: Throwable => error(e.getMessage,e)}
    try { this.syncJd()
    }catch{case e: Throwable => error(e.getMessage,e)}
    try { this.syncPdd()
    }catch{case e: Throwable => error(e.getMessage,e)}
  }
  override def syncTaobaoOrder(): Unit ={
    if(config.taobao != null) {
      //test save
      //    val order = new TaobaoPublisherOrder
      //    order.tradeId = "123123"
      //    order.tkCreateTime = DateTime.now
      //    order.save
      val maxCreateTimeValue = select[TaobaoPublisherOrder](TaobaoPublisherOrder.tkCreateTime[DateTime].max)
      var maxCreateTime = maxCreateTimeValue.head.asInstanceOf[DateTime]
      if (maxCreateTime == null) maxCreateTime = DateTime.now().minusDays(90)
      logger.info("sync taobao order with max tkCreateTime ({})", maxCreateTime)
      syncTaobaoOrder(maxCreateTime, 1)
      logger.info("finish to sync taobao order with create time")

      val maxEarningTimeValue = select[TaobaoPublisherOrder](TaobaoPublisherOrder.tkEarningTime[DateTime].max)
      var maxEarningTime = maxEarningTimeValue.head.asInstanceOf[DateTime]
      if (maxEarningTime == null) maxEarningTime = DateTime.now().minusDays(90)
      logger.info("sync taobao order with max maxEarningTime ({})", maxEarningTime)
      syncTaobaoOrder(maxEarningTime, 3)
      logger.info("finish to sync taobao order with earning time")
    }
  }

  /**
    *
    * @param beginTime
    * @param queryType 查询时间类型，1：按照订单淘客创建时间查询，2:按照订单淘客付款时间查询，3:按照订单淘客结算时间查询
    */
  def syncTaobaoOrder(beginTime:DateTime,queryType:Int): Unit ={
    val TAOBAO_DATETIME_FORMAT="yyyy-MM-dd HH:mm:ss"

    val client = taobaoService.getOrCreateTaobaoClient()
    val req = new TbkOrderDetailsGetRequest
    req.setPageSize(100L)
    req.setQueryType(queryType.toLong)

    @tailrec
    def processResponse(response:TbkOrderDetailsGetResponse) {
      if (response.isSuccess){
        if(response.getData.getResults != null) {
          val it = response.getData.getResults.iterator()
          while (it.hasNext) {
            taobaoCommerceOrderService.processOrder(it.next)
          }
          //休息一秒，避免限流
          Thread.sleep(TimeUnit.SECONDS.toMillis(2))
          if (response.getData.getHasNext) {
            //还有下一页
            req.setPageNo(response.getData.getPageNo + 1)
            req.setPositionIndex(response.getData.getPositionIndex)
            processResponse(client.execute(req))
          }
        }
      }else{
        throw new RuntimeException(response.getBody)
      }
    }
    @tailrec
    def loop(startTime:DateTime) {
      req.setPositionIndex(null) //clear position
      req.setPageNo(1L) //new query

      var endTime = startTime.plusHours(3) //订单查询结束时间，订单开始时间至订单结束时间，中间时间段日常要求不超过3个小时，但如618、双11、年货节等大促期间预估时间段不可超过20分钟，超过会提示错误，调用时请务必注意时间段的选择，以保证亲能正常调用！
      val isAfterNow = endTime.isAfterNow
      if(isAfterNow){
        endTime = DateTime.now()
      }
      req.setStartTime(startTime.toString(TAOBAO_DATETIME_FORMAT))
      req.setEndTime(endTime.toString(TAOBAO_DATETIME_FORMAT))
      val response = client.execute(req)
      processResponse(response)

      if(!isAfterNow) loop(endTime)
    }
    loop(beginTime)
  }

  def syncPdd(): Unit ={
    val orderOpt = PddOrder.where.orderBy(PddOrder.orderModifyAt[DateTime].desc).limit(1).headOption
    val beginTime =
      orderOpt match{
        case Some(order) =>
          order.orderModifyAt
        case _ =>
          DateTime.now().minusDays(3)
      }
    logger.info("sync pdd order with begin time ({})", beginTime)
    val pageSize = 50
    val request = new PddDdkOrderListIncrementGetRequest
    request.setReturnCount(false)
    request.setPageSize(pageSize)

    def loop(startTime:DateTime,page:Int=1): Unit ={
      request.setPage(page)

      var endTime = startTime.plusHours(24)
      val isAfterNow = endTime.isAfterNow
      if(isAfterNow){
        endTime = DateTime.now()
      }
      val startTimeSeconds = startTime.getMillis /1000 //1587225600000L //
      val endTimeSeconds = endTime.getMillis /1000 //1587830399999L
      request.setEndUpdateTime(endTimeSeconds)
      request.setStartUpdateTime(startTimeSeconds)
//      println("[pdd] startTime:{},endTime:{}",startTime.toString,endTime.toString)
//      println(startTimeSeconds,endTimeSeconds)
      val response = pddService.getClient().syncInvoke(request)
      if(response.getErrorResponse != null){
        throw new RuntimeException(response.getErrorResponse.getErrorMsg)
      }
      val orderListResponse = response.getOrderListGetResponse
      val orderList = orderListResponse.getOrderList
      val it = orderList.iterator()
      while(it.hasNext){
//        pddService.createOrUpdateOrder(it.next)
        pddCommerceOrderService.processOrder(it.next())
      }

      if(orderList.size() == pageSize){//还有数据没取完
        loop(startTime,page+1)
      }else if(!isAfterNow) {
        loop(endTime)
      }
    }
    loop(beginTime)
  }

  def syncJd(): Unit ={
    val orderOpt = JdOrder.where.orderBy(JdOrder.orderTime[DateTime].desc).limit(1).headOption
    val beginTime =
      orderOpt match{
        case Some(order) =>
          order.orderTime
        case _ =>
          DateTime.now().minusDays(3)
      }
    logger.info("sync jd order with begin time ({})", beginTime)
    val request = new UnionOpenOrderQueryRequest
    val orderReq = new OrderReq
    val pageSize = 50
    orderReq.setType(3) //按照订单更新时间查询
    orderReq.setPageSize(pageSize)

    request.setOrderReq(orderReq)


    def loop(startTime:DateTime,page:Int=1): Unit ={
      var endTime = startTime.plusHours(1)
      val isAfterNow = endTime.isAfterNow
      if(isAfterNow){
        endTime = DateTime.now()
      }
      orderReq.setPageNo(page)
      orderReq.setTime(startTime.toString("yyyyMMddHH")) //设置时间

      val response = jdService.getClient().execute(request)
      if(response.getCode != 200){
        throw new RuntimeException(response.getMessage)
      }
//      println(JsonUtil.toJson(response))
      val orderList = response.getData
      if(orderList != null) {
        orderList.foreach { order =>
          order.getSkuList.foreach { sku =>
            jdCommerceOrderService.processOrder(order, sku)
          }
        }
      }

      if(response.getHasMore){//还有数据没取完
        loop(startTime,page+1)
      }else if(!isAfterNow) {
        loop(endTime)
      }
    }
    loop(beginTime)
  }

  private[internal] def setPddService(pddService:PddService): Unit ={
    this.pddService = pddService
  }
}

object SyncCommerceOrderSchedulerImpl {
  def main(args: Array[String]): Unit = {
    val task = new SyncCommerceOrderSchedulerImpl
    val pddService = new PddServiceImpl
    task.setPddService(pddService)
    task.syncPdd()

//    import scala.collection.JavaConversions._
//    pddService.queryAllPids().foreach(println)
  }
}