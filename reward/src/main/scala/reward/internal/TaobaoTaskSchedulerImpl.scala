package reward.internal

import com.taobao.api.request.TbkOrderDetailsGetRequest
import com.taobao.api.response.TbkOrderDetailsGetResponse
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reward.RewardConstants
import reward.config.RewardConfig
import reward.entities.TaobaoPublisherOrder
import reward.services.{TaobaoService, TaobaoTaskScheduler}
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
class TaobaoTaskSchedulerImpl extends TaobaoTaskScheduler with LoggerSupport{
  @Autowired
  private val taobaoService:TaobaoService = null
  @Autowired
  private val config:RewardConfig = null
//  val TaoAppKey = "27706268"
//  val TaoSecret = "6505ff50b4d72c56fef583b2570593fa"
  //  val TaoAppKey = "28430902"
  //  val TaoSecret = "c982412aeb22bccc535b918de4165a3a"

  @Scheduled(fixedDelay = 100000L) //5 * 60 * 1000
  override def syncTaobaoOrder(): Unit ={
    if(config.taobao != null) {
      //test save
      //    val order = new TaobaoPublisherOrder
      //    order.tradeId = "123123"
      //    order.tkCreateTime = DateTime.now
      //    order.save
      val maxCreateTimeValue = select[TaobaoPublisherOrder](TaobaoPublisherOrder.tkCreateTime[DateTime].max)
      var maxCreateTime = maxCreateTimeValue.head.asInstanceOf[DateTime]
      if (maxCreateTime == null) maxCreateTime = DateTime.now().minusDays(5)
      logger.info("sync taobao order with max tkCreateTime ({})", maxCreateTime)
      syncTaobaoOrder(maxCreateTime, 1)
      logger.info("finish to sync taobao order with create time")

      val maxEarningTimeValue = select[TaobaoPublisherOrder](TaobaoPublisherOrder.tkEarningTime[DateTime].max)
      var maxEarningTime = maxEarningTimeValue.head.asInstanceOf[DateTime]
      if (maxEarningTime == null) maxEarningTime = DateTime.now().minusDays(5)
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
    val client = taobaoService.getOrCreateTaobaoClient()
    val req = new TbkOrderDetailsGetRequest
    req.setPageSize(100L)
    req.setQueryType(queryType.toLong)

    @tailrec
    def processResponse(response:TbkOrderDetailsGetResponse) {
      if (response.getData.getResults != null && response.getData.getResults.size() > 0) {
        val it = response.getData.getResults.iterator()
        while(it.hasNext){
          taobaoService.createOrUpdateOrder(it.next)
        }
        if (response.getData.getHasNext) {
          //还有下一页
          req.setPageNo(response.getData.getPageNo + 1)
          req.setPositionIndex(response.getData.getPositionIndex)
          processResponse(client.execute(req))
        }
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
      req.setStartTime(startTime.toString(RewardConstants.TAOBAO_DATETIME_FORMATE))
      req.setEndTime(endTime.toString(RewardConstants.TAOBAO_DATETIME_FORMATE))
      val response = client.execute(req)
      processResponse(response)

      if(!isAfterNow) loop(endTime)
    }
    loop(beginTime)
  }
}

object TaobaoTaskSchedulerImpl {
}
