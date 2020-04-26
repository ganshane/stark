package reward.internal

import java.util
import java.util.concurrent.ConcurrentLinkedQueue

import com.fasterxml.jackson.databind.ObjectMapper
import com.pdd.pop.sdk.http.PopHttpClient
import com.pdd.pop.sdk.http.api.request.{PddDdkGoodsPidGenerateRequest, PddDdkGoodsPidQueryRequest}
import com.pdd.pop.sdk.http.api.response.PddDdkOrderListIncrementGetResponse
import javax.transaction.Transactional
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reward.RewardConstants
import reward.entities.AppConfig.CommissionConfig
import reward.entities.TraceOrder.{CommerceItem, CommerceType, TraceOrderStatus}
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities._
import reward.services.{PddService, UserService, WxService}
import stark.utils.services.LoggerSupport

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
  * 针对pdd的服务类
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
@Service
class PddServiceImpl extends PddService with LoggerSupport{
  private val clientId="869872c316ea408fb16b61b0fcdccc0b"
  private val clientSecret="7735676a0bcc5b42cf26d428b2ef1616041f90f5"
  private val client = new PopHttpClient(clientId, clientSecret)
  @Autowired
  private val objectMapper:ObjectMapper = null
  @Autowired
  private val userService:UserService= null
  @Autowired
  private val wxService:WxService= null
  override def queryAllPids(): util.Queue[String]={
    val queue = new ConcurrentLinkedQueue[String]()
    loopGetAllPid().foreach(queue.offer)
    queue
  }

  override def getClient(): PopHttpClient = client


  override def createPidByUserId(userId: Long): String = {
    val request= new PddDdkGoodsPidGenerateRequest
    request.setNumber(1L)
    val response = client.syncInvoke(request)
    val errorResponse = response.getErrorResponse
      if(errorResponse != null)
        throw new RuntimeException(errorResponse.getErrorMsg)
    response.getPIdGenerateResponse.getPIdList.get(0).getPId
  }

  @tailrec
  private def loopGetAllPid(page:Int=1,pageSize:Int=100,data:List[String]=List()): List[String]={
    val request = new PddDdkGoodsPidQueryRequest
    request.setPage(page)
    request.setPageSize(pageSize)
    val response = client.syncInvoke(request)
    if(response.getErrorResponse == null) {
      val pidResponse = response.getPIdQueryResponse
      val list = pidResponse.getPIdList.toList.map(_.getPId)
      if(list.size < pageSize){
        data ::: list
      }else{
        loopGetAllPid(page+1,pageSize,data ::: list )
      }
    }else{
      throw new RuntimeException("query pid excepiton,msg:"+response.getErrorResponse.getErrorMsg)
    }
  }

  def copyProperties(pddOrder: PddOrder, originOrder: PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem) :PddOrder= {
    pddOrder.orderSn = originOrder.getOrderSn
    pddOrder.goodsId = originOrder.getGoodsId
    pddOrder.goodsName = originOrder.getGoodsName
    pddOrder.goodsThumbnailUrl = originOrder.getGoodsThumbnailUrl
    pddOrder.goodsQuantity = originOrder.getGoodsQuantity
    pddOrder.goodsPrice = originOrder.getGoodsPrice
    pddOrder.orderAmount = originOrder.getOrderAmount
    pddOrder.pId  = originOrder.getPId
    pddOrder.promotionRate = originOrder.getPromotionRate
    pddOrder.promotionAmount = originOrder.getPromotionAmount
    pddOrder.orderStatus  = originOrder.getOrderStatus
    pddOrder.orderStatusDesc = originOrder.getOrderStatusDesc
    if(originOrder.getOrderCreateTime != null)
      pddOrder.orderCreateTime = new DateTime(originOrder.getOrderCreateTime*1000)
    if(originOrder.getOrderPayTime != null)
      pddOrder.orderPayTime = new DateTime(originOrder.getOrderPayTime * 1000)

    if(originOrder.getOrderGroupSuccessTime != null)
      pddOrder.orderGroupSuccessTime =new DateTime( originOrder.getOrderGroupSuccessTime*1000)
    if(originOrder.getOrderVerifyTime != null)
      pddOrder.orderVerifyTime = new DateTime(originOrder.getOrderVerifyTime*1000)
    if(originOrder.getOrderModifyAt != null)
      pddOrder.orderModifyAt =new DateTime( originOrder.getOrderModifyAt * 1000)
    pddOrder.customParameters = originOrder.getCustomParameters
    pddOrder.cpaNew = originOrder.getCpaNew
    pddOrder.`type` = originOrder.getType
    if(originOrder.getOrderSettleTime != null)
      pddOrder.orderSettleTime  =new DateTime( originOrder.getOrderSettleTime * 1000)
    pddOrder.authDuoId = originOrder.getAuthDuoId
    pddOrder.batchNo = originOrder.getBatchNo
    if(originOrder.getOrderReceiveTime != null)
      pddOrder.orderReceiveTime  =new DateTime( originOrder.getOrderReceiveTime * 1000)
    pddOrder.groupId = originOrder.getGroupId
    pddOrder.failReason = originOrder.getFailReason
    pddOrder.orderId  = originOrder.getOrderId
    pddOrder.zsDuoId = originOrder.getZsDuoId

    pddOrder

  }
  private def saveUserStatisticFromNewOrder(userOrder: UserOrder): Unit ={
    //更新用户状态
    val us = userService.getOrCreateUserStatistic(userOrder.userId)
    if(userOrder.withdrawStatus == WithdrawResult.CAN_APPLY) {
      us.withdrawAmount += userOrder.fee
      us.withdrawOrderNum += 1
    }else if(userOrder.withdrawStatus != WithdrawResult.UNAPPLY){
      us.preWithdrawAmount += userOrder.preFee
      us.preOrderNum += 1
    }
    //写入总订单数
    us.totalOrderNum += 1
    us.save()
  }

  //https://open.pinduoduo.com/#/apidocument/port?portId=pdd.ddk.order.list.increment.get
  @Transactional
  override def createOrUpdateOrder(originOrder: PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem): Unit = {
    val newStatus = originOrder.getOrderStatus
    var oldStatus = newStatus
    val pddOrderOpt = PddOrder.find_by_orderSn(originOrder.getOrderSn) headOption
    val pddOrder = pddOrderOpt match{
      case Some(pddOrderEntity) =>
        oldStatus = pddOrderEntity.orderStatus
        copyProperties(pddOrderEntity,originOrder)
      case _ =>
        copyProperties(new PddOrder,originOrder)
    }
    pddOrder.save()
    val tradeId = pddOrder.id
    val userOrders = UserOrder.find_by_tradeOrder(new CommerceOrder(tradeId,CommerceType.PDD))
    val appConfigOpt = AppConfig.find_by_key(RewardConstants.COMMISSION_CONFIG_KEY).headOption
    val commissionConfig = appConfigOpt.map(_.readAsCommissionConfig(objectMapper)).getOrElse(new CommissionConfig)
    if(userOrders.nonEmpty) { //已经有订单匹配
      if(newStatus != oldStatus &&
        (newStatus == RewardConstants.PDD_ORDER_STATUS_CLOSED ||
          newStatus == RewardConstants.PDD_ORDER_STATUS_FAIL_AUDIT
          )
      ) { //状态发生变化才进行处理
        userOrders.foreach(uo => {
          //如果佣金已经被支付，则需要调整提现状态
          if (newStatus == RewardConstants.PDD_ORDER_STATUS_CLOSED ) {
            //新状态发生变化
            //收到佣金
            uo.withdrawStatus = WithdrawResult.CAN_APPLY
            uo.fee = (commissionConfig.findCommissionRate(uo.level) * pddOrder.promotionAmount/100).intValue()
            uo.save()

            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preWithdrawAmount -= uo.preFee
            us.preOrderNum -= 1
            us.withdrawOrderNum += 1
            us.withdrawAmount += uo.fee
            us.save()

          } else if (newStatus == RewardConstants.PDD_ORDER_STATUS_FAIL_AUDIT ) {
            //订单关闭
            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preOrderNum -= 1
            us.preWithdrawAmount -= uo.preFee
            us.save()

            uo.withdrawStatus = WithdrawResult.UNAPPLY
            uo.preFee = 0
            uo.fee=0
            uo.save()
          }
        })
      }
    }else { //新的订单数据过来
      /**
        * 通过pdd的pid commerceType来进行定位
        */
      val customParameter=
        if(StringUtils.isEmpty(originOrder.getCustomParameters)) {
//          throw new RuntimeException("custom parameter not found! order_sn:"+originOrder.getOrderSn)
          //only for test
          warn("custom parameter not found,order_sn:"+originOrder.getOrderSn)
          val value="{\"uid\":\"65\"}"
          objectMapper.readValue(value,classOf[PddService.CustomParameter])
        }
        else objectMapper.readValue(originOrder.getCustomParameters,classOf[PddService.CustomParameter])

      val coll = TraceOrder where
//        TraceOrder.pid === pid and
        TraceOrder.userId === customParameter.userId and
        TraceOrder.item === new CommerceItem(originOrder.getGoodsId,CommerceType.PDD) and
        TraceOrder.status[TraceOrderStatus.Type] === TraceOrderStatus.NEW orderBy
//        TraceOrder.createdAt[DateTime] < taobaoOrder.clickTime orderBy
        TraceOrder.createdAt[DateTime].desc limit 1

      coll.headOption match {
        case Some(traceOrder) =>
//          val minutesDiff = Minutes.minutesBetween(taobaoOrder.clickTime,traceOrder.createdAt).getMinutes
//          if(minutesDiff < 10) { //当拷贝二维码后，十分钟还未进入手机淘宝的，则忽略
            val userOrder = new UserOrder
            userOrder.clickTime = pddOrder.orderCreateTime
            userOrder.traceTime = traceOrder.createdAt
            userOrder.userId = traceOrder.userId
            userOrder.tradeOrder= new CommerceOrder(tradeId,CommerceType.PDD)
            userOrder.level = 0
            userOrder.withdrawStatus =
              if (pddOrder.orderStatus == RewardConstants.PDD_ORDER_STATUS_CLOSED){
                userOrder.fee=(commissionConfig.findCommissionRate(userOrder.level) * pddOrder.promotionAmount /100).intValue()
                WithdrawResult.CAN_APPLY
              }else if(newStatus == RewardConstants.PDD_ORDER_STATUS_FAIL_AUDIT) {
                WithdrawResult.UNAPPLY
              }else {
                userOrder.preFee = (commissionConfig.findCommissionRate(userOrder.level) * pddOrder.promotionAmount /100).intValue()
                WithdrawResult.PRE_APPLY
              }

            userOrder.save()

            saveUserStatisticFromNewOrder(userOrder)

            { //增加父以及爷订单
              UserRelation.find_by_userId(traceOrder.userId).foreach(ur=>{
                val order = new UserOrder
                order.clickTime = pddOrder.orderCreateTime
                order.traceTime = traceOrder.createdAt
                order.userId = ur.parentId
                order.tradeOrder = new CommerceOrder(tradeId,CommerceType.PDD)
                order.level = ur.level
                order.withdrawStatus = userOrder.withdrawStatus
                if(order.withdrawStatus == WithdrawResult.CAN_APPLY)
                  order.fee = (commissionConfig.findCommissionRate(order.level) * pddOrder.promotionAmount /100).intValue()
                else if(order.withdrawStatus != WithdrawResult.UNAPPLY){
                  order.preFee = (commissionConfig.findCommissionRate(order.level) * pddOrder.promotionAmount/100).intValue()
                }
                order.save()

                saveUserStatisticFromNewOrder(order)
              })
            }
            //同时还要更新trace_order表示这条数据已经使用
            traceOrder.status = TraceOrderStatus.DETECTED
            traceOrder.detectedTime = DateTime.now()
            traceOrder.save()
            //更新消费记录
            val consumption = new Consumption
            consumption.createdAt=DateTime.now()
            consumption.amount=traceOrder.couponAmount
            consumption.tradeId = tradeId
            consumption.userId = traceOrder.userId
            consumption.save()
            //更新用户余额
            val us = userService.getOrCreateUserStatistic(traceOrder.userId)
            us.consumptionAmount += traceOrder.couponAmount
            us.lastConsume = DateTime.now
            us.save()
            val user = User.find(traceOrder.userId)
            wxService.sendConsumptionMessage(user.openId,us,traceOrder.couponAmount)
//          }
        case _ =>
          warn("pdd order[{}] not traced,goodsId:{}",originOrder.getOrderSn,originOrder.getGoodsId)
      }
    }
  }
}
