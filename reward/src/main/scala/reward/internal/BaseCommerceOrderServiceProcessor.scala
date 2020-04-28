package reward.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import reward.RewardConstants
import reward.config.RewardConfig
import reward.entities.AppConfig.CommissionConfig
import reward.entities.TraceOrder.TraceOrderStatus
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities._
import reward.services.{CommerceOrderService, UserService, WxService}
import stark.utils.services.LoggerSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
abstract class BaseCommerceOrderServiceProcessor[O,E <: CommerceOrder] extends CommerceOrderService[O,E] with LoggerSupport{
  @Autowired
  private val config:RewardConfig  = null
  @Autowired
  private val wxService:WxService= null
  @Autowired
  private val userService:UserService = null
  @Autowired
  private val objectMapper:ObjectMapper = null

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
  @Transactional
  override def processOrder(originOrder: O): Unit = {
    val(oldStatus,commerceOrderEntity,newStatus) = saveOrUpdate(originOrder)
    val commerceOrderPK = commerceOrderEntity.toCommerceOrderPK
    val userOrders = UserOrder.find_by_tradeOrder(commerceOrderPK)
    val appConfigOpt = AppConfig.find_by_key(RewardConstants.COMMISSION_CONFIG_KEY).headOption
    val commissionConfig = appConfigOpt.map(_.readAsCommissionConfig(objectMapper)).getOrElse(new CommissionConfig)
    if(userOrders.nonEmpty) { //已经有订单匹配
      if(newStatus != oldStatus &&
        (newStatus == CommerceOrderStatus.SETTLED || newStatus == CommerceOrderStatus.FAIL)
      ) { //状态发生变化才进行处理
        userOrders.foreach(uo => {
          //如果佣金已经被支付，则需要调整提现状态
          if (newStatus == CommerceOrderStatus.SETTLED) {
            //新状态发生变化
            //收到佣金
            uo.withdrawStatus = WithdrawResult.CAN_APPLY
            uo.fee = commissionConfig.findCommissionRate(uo.level) * commerceOrderEntity.getCommission /100
            uo.save()

            val us = userService.getOrCreateUserStatistic(uo.userId)
            us.preWithdrawAmount -= uo.preFee
            us.preOrderNum -= 1
            us.withdrawOrderNum += 1
            us.withdrawAmount += uo.fee
            us.save()

          } else if (newStatus == CommerceOrderStatus.FAIL) {
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
        * 通过淘宝的pid itemid clickTime 来进行定位
        */
      //      val coll = TraceOrder where
      //        TraceOrder.pid === pid and
      //        TraceOrder.item === new CommerceItem(taobaoOrder.getItemId,CommerceType.TAOBAO) and
      //        TraceOrder.status[TraceOrderStatus.Type] === TraceOrderStatus.NEW and
      //        TraceOrder.createdAt[DateTime] < taobaoOrder.getClickTime orderBy
      //        TraceOrder.createdAt[DateTime].desc limit 1
      val traceOrderOption = findTraceOrderOption(commerceOrderEntity)
      traceOrderOption match {
        case Some(traceOrder) =>
//          val minutesDiff = Minutes.minutesBetween(taobaoOrder.getClickTime,traceOrder.createdAt).getMinutes
//          if(minutesDiff < 10) { //当拷贝二维码后，十分钟还未进入手机淘宝的，则忽略
            val userOrder = new UserOrder
            userOrder.clickTime = commerceOrderEntity.getClickTime
            userOrder.traceTime = traceOrder.createdAt
            userOrder.userId = traceOrder.userId
            userOrder.tradeOrder=  commerceOrderPK //CommerceOrderPK(tradeId,CommerceType.TAOBAO)
            userOrder.level = 0
            userOrder.withdrawStatus =
              if (newStatus == CommerceOrderStatus.SETTLED){
                userOrder.fee=commissionConfig.findCommissionRate(userOrder.level) * commerceOrderEntity.getCommission /100
                WithdrawResult.CAN_APPLY
              }else if(newStatus == CommerceOrderStatus.FAIL) {
                WithdrawResult.UNAPPLY
              }else {
                userOrder.preFee = commissionConfig.findCommissionRate(userOrder.level) * commerceOrderEntity.getEstimateCommission /100
                WithdrawResult.PRE_APPLY
              }

            userOrder.save()

            saveUserStatisticFromNewOrder(userOrder)

            { //增加父以及爷订单
              UserRelation.find_by_userId(traceOrder.userId).foreach(ur=>{
                val order = new UserOrder
                order.clickTime = commerceOrderEntity.getClickTime
                order.traceTime = traceOrder.createdAt
                order.userId = ur.parentId
                order.tradeOrder = commerceOrderPK //new CommerceOrderPK(tradeId,CommerceType.TAOBAO)
                order.level = ur.level
                order.withdrawStatus = userOrder.withdrawStatus
                if(order.withdrawStatus == WithdrawResult.CAN_APPLY)
                  order.fee = commissionConfig.findCommissionRate(order.level) * commerceOrderEntity.getCommission /100
                else if(order.withdrawStatus != WithdrawResult.UNAPPLY){
                  order.preFee = commissionConfig.findCommissionRate(order.level) * commerceOrderEntity.getEstimateCommission /100
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
            consumption.tradeId = commerceOrderEntity.getTradeId
            consumption.commerceType =  traceOrder.item.commerceType
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
          warn("commerce order[{}] not traced",commerceOrderPK)
      }
    }
  }
}
