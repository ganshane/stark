package reward.internal

import java.util
import java.util.concurrent.ConcurrentLinkedQueue

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.jd.open.api.sdk.{DefaultJdClient, JdClient}
import javax.transaction.Transactional
import jd.union.open.goods.promotiongoodsinfo.query.request.UnionOpenGoodsPromotiongoodsinfoQueryRequest
import jd.union.open.order.query.response.{OrderResp, SkuInfo}
import jd.union.open.promotion.common.get.request.{PromotionCodeReq, UnionOpenPromotionCommonGetRequest}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reward.RewardConstants
import reward.entities.AppConfig.CommissionConfig
import reward.entities.TraceOrder.{CommerceItem, CommerceType, TraceOrderStatus}
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities._
import reward.services.JdService.{MiaoYouQuanResponse, PidData, PromotionPosition}
import reward.services.{JdService, UserService, WxService}
import stark.utils.services.LoggerSupport

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
@Service
class JdServiceImpl extends JdService with LoggerSupport{
  //jos https://api.jd.com/routerjson
  private val SERVER_URL="https://router.jd.com/api"
  private val accessToken="b57733a7028b09011e441a2ec416d0da68f30ba9872b378c85f288ebea29b650f583d9e209681189"
  private val appKey="190562315b06c899931ed8d341aafc47"
  private val appSecret="0415c3ed82df420f8c700732e7ae0c1b"
  private val client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret)

  private val MYQ_AP_KEY:String ="a781b1d1-60d0-f570-dfea-64f02c7f834b"
  private val MYQ_JD_CONNECT_KEY:String ="J4672635259984466"
  @Autowired
  private val userService:UserService =  null
  @Autowired
  private val wxService:WxService= null
  private val restTemplate:RestTemplate = {
    val rt = new RestTemplate()
    rt.getMessageConverters.foreach{
      case converter: MappingJackson2HttpMessageConverter => converter.getObjectMapper.registerModule(DefaultScalaModule)
      case _ =>
    }
    rt
  }
  private val objectMapper:ObjectMapper = {
    new ObjectMapper().registerModule(DefaultScalaModule)
  }

  def getClient(): JdClient = client
  def createPidByUserId(userId:Long):String={
    val url = "https://api.gofanli.cn/jingdong/createUnionPosition"
    val builder = UriComponentsBuilder.fromUriString(url)
    builder.queryParam("apkey",MYQ_AP_KEY)
    builder.queryParam("key_id",MYQ_JD_CONNECT_KEY)
    builder.queryParam("unionType",1.toString)
    builder.queryParam("type",3.toString)
    builder.queryParam("siteId",223314.toString)
    builder.queryParam("spaceNameList",userId.toString)

    val json = restTemplate.getForObject(builder.build().toUri,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[PidData]](){} )
    if(response.code == 200){
      response.data.resultList.head._2
    }else {
      throw new RuntimeException(response.msg)
    }
  }
  override def queryAllPids(): util.Queue[String]={
    val queue = new ConcurrentLinkedQueue[String]()
    loopGetAllPid().foreach(queue.offer)
    queue
  }

  @tailrec
  private def loopGetAllPid(page:Int=1,pageSize:Int=100,data:List[String]=List()): List[String]={
    var url = "http://api.web.21ds.cn/jingdong/getUnionPosition?"
    url += buildParameters(Map(
      "apkey" -> MYQ_AP_KEY,
      "key_id"->MYQ_JD_CONNECT_KEY,
      "unionType"->1,
      "pageIndex"->page,
      "pageSize"->pageSize
    ))

    val json = restTemplate.getForObject(url,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[PromotionPosition]](){} )
    if(response.code == 200) {
      println(response.data.total)
      val list = response.data.result.map(_.id.toString).toList
      val nextData = data ::: list
      println("total data:",response.data.total)
      if(nextData.size >= response.data.total){
        nextData
      }else{
        loopGetAllPid(page+1,pageSize,nextData)
      }
    }else{
      throw new RuntimeException("query pid excepiton,msg:"+response.msg)
    }
  }
  def getPositions: PromotionPosition ={
    var url = "http://api.web.21ds.cn/jingdong/getUnionPosition?"
    url += buildParameters(Map(
      "apkey" -> MYQ_AP_KEY,
      "key_id"->MYQ_JD_CONNECT_KEY,
      "unionType"->1,
      "pageIndex"->1,
      "pageSize"->100
    ))

    val json = restTemplate.getForObject(url,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[PromotionPosition]](){} )
    if(response.code == 200) {
      response.data
    }
    else throw new RuntimeException(response.msg)
  }
  private def buildParameters(parameters:Map[String,Any]): String={
    parameters.map{case (k,v)=>{
       k+"="+v
    }}.mkString("&")
  }

  def promotionUrl2(): Unit ={
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
  def copyProperties(jdOrder: JdOrder,order:OrderResp,originOrder: SkuInfo):JdOrder = {
    jdOrder.orderId=order.getOrderId
    jdOrder.skuId = originOrder.getSkuId
    if(order.getFinishTime > 0)
      jdOrder.finishTime=new DateTime(order.getFinishTime)
    if(order.getOrderTime > 0)
    jdOrder.orderTime=new DateTime(order.getOrderTime)
    jdOrder.actualCosPrice =(originOrder.getActualCosPrice * 100).intValue()
    jdOrder.actualFee =(originOrder.getActualFee*100).intValue()
    jdOrder.commissionRate=(originOrder.getCommissionRate*100).intValue()
    jdOrder.estimateCosPrice =(originOrder.getEstimateCosPrice*100).intValue()
    jdOrder.estimateFee =(originOrder.getEstimateFee * 100).intValue()
    jdOrder.finalRate =(originOrder.getFinalRate*100).intValue()
    jdOrder.cid1=originOrder.getCid1
    jdOrder.frozenSkuNum=originOrder.getFrozenSkuNum
    jdOrder.pid =originOrder.getPid
    jdOrder.positionId =originOrder.getPositionId
    jdOrder.price =(originOrder.getPrice*100).intValue()
    jdOrder.cid2 =originOrder.getCid2
    jdOrder.siteId =originOrder.getSiteId
    jdOrder.skuName =originOrder.getSkuName
    jdOrder.skuNum =originOrder.getSkuNum
    jdOrder.skuReturnNum =originOrder.getSkuReturnNum
    jdOrder.subSideRate =originOrder.getSubSideRate.intValue()
    jdOrder.subsidyRate =originOrder.getSubsidyRate.intValue()
    jdOrder.cid3 =originOrder.getCid3
    jdOrder.unionAlias =originOrder.getUnionAlias
    jdOrder.unionTag =originOrder.getUnionTag
    jdOrder.unionTrafficGroup =(originOrder.getUnionTrafficGroup).intValue()
    jdOrder.validCode =originOrder.getValidCode
    jdOrder.subUnionId =originOrder.getSubUnionId
    jdOrder.traceType =(originOrder.getTraceType).intValue()
    jdOrder.payMonth =originOrder.getPayMonth.toString
    jdOrder.popId =originOrder.getPopId
    jdOrder.ext1 =originOrder.getExt1
    jdOrder.cpActId =originOrder.getCpActId
    jdOrder.unionRole =originOrder.getUnionRole
    jdOrder.giftCouponKey =originOrder.getGiftCouponKey
    jdOrder.giftCouponOcsAmount =(originOrder.getGiftCouponOcsAmount * 100).intValue()
//    jdOrder.updatedAt=DateTime.now

    if(StringUtils.isEmpty(jdOrder.itemImg)) {
      val request = new UnionOpenGoodsPromotiongoodsinfoQueryRequest
      request.setSkuIds(jdOrder.skuId.toString)
      val response = client.execute(request)
      if (response.getCode != 200) {
        throw new RuntimeException(response.getMessage)
      }
      jdOrder.itemImg = response.getData()(0).getImgUrl
    }


    jdOrder
  }

  @Transactional
  override def createOrUpdateOrder(order: OrderResp, originOrder:SkuInfo): Unit = {
    val newStatus = JdOrder.convertAsCommerceOrderStatus(originOrder.getValidCode)
    var oldStatus = newStatus
    val jdEntityOrderOpt = JdOrder.find_by_orderId_and_skuId(order.getOrderId,originOrder.getSkuId) headOption
    val jdEntityOrder = jdEntityOrderOpt match{
      case Some(pddOrderEntity) =>
        oldStatus = pddOrderEntity.getCommerceOrderStatus
        copyProperties(pddOrderEntity,order,originOrder)
      case _ =>
        copyProperties(new JdOrder,order,originOrder)
    }
    jdEntityOrder.save()

    val tradeId = jdEntityOrder.id
    val userOrders = UserOrder.find_by_tradeOrder(new CommerceOrder(tradeId,CommerceType.JD))
    val appConfigOpt = AppConfig.find_by_key(RewardConstants.COMMISSION_CONFIG_KEY).headOption
    val commissionConfig = appConfigOpt.map(_.readAsCommissionConfig(objectMapper)).getOrElse(new CommissionConfig)
    if(userOrders.nonEmpty) { //已经有订单匹配
      if(newStatus != oldStatus &&
        (newStatus == CommerceOrderStatus.SETTLED ||
          newStatus == CommerceOrderStatus.FAIL
          )
      ) { //状态发生变化才进行处理
        userOrders.foreach(uo => {
          //如果佣金已经被支付，则需要调整提现状态
          if (newStatus == CommerceOrderStatus.SETTLED) {
            //新状态发生变化
            //收到佣金
            uo.withdrawStatus = WithdrawResult.CAN_APPLY
            uo.fee = (commissionConfig.findCommissionRate(uo.level) * jdEntityOrder.actualFee/100).intValue()
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
        * 通过jd的pid commerceType来进行定位
        */

      val coll = TraceOrder where
        TraceOrder.pid === originOrder.getPositionId.toString and
        TraceOrder.item === new CommerceItem(originOrder.getSkuId,CommerceType.JD) and
        TraceOrder.status[TraceOrderStatus.Type] === TraceOrderStatus.NEW orderBy
        //        TraceOrder.createdAt[DateTime] < taobaoOrder.clickTime orderBy
        TraceOrder.createdAt[DateTime].desc limit 1

      coll.headOption match {
        case Some(traceOrder) =>
          //          val minutesDiff = Minutes.minutesBetween(taobaoOrder.clickTime,traceOrder.createdAt).getMinutes
          //          if(minutesDiff < 10) { //当拷贝二维码后，十分钟还未进入手机淘宝的，则忽略
          val userOrder = new UserOrder
          userOrder.clickTime = jdEntityOrder.orderTime
          userOrder.traceTime = traceOrder.createdAt
          userOrder.userId = traceOrder.userId
          userOrder.tradeOrder= new CommerceOrder(tradeId,CommerceType.JD)
          userOrder.level = 0
          userOrder.withdrawStatus =
            if (newStatus == CommerceOrderStatus.SETTLED){
              userOrder.fee=(commissionConfig.findCommissionRate(userOrder.level) * jdEntityOrder.actualFee  /100).intValue()
              WithdrawResult.CAN_APPLY
            }else if(newStatus == CommerceOrderStatus.FAIL) {
              WithdrawResult.UNAPPLY
            }else {
              userOrder.preFee = (commissionConfig.findCommissionRate(userOrder.level) * jdEntityOrder.estimateFee /100).intValue()
              WithdrawResult.PRE_APPLY
            }

          userOrder.save()

          saveUserStatisticFromNewOrder(userOrder)

        { //增加父以及爷订单
          UserRelation.find_by_userId(traceOrder.userId).foreach(ur=>{
            val order = new UserOrder
            order.clickTime = jdEntityOrder.orderTime
            order.traceTime = traceOrder.createdAt
            order.userId = ur.parentId
            order.tradeOrder = new CommerceOrder(tradeId,CommerceType.JD)
            order.level = ur.level
            order.withdrawStatus = userOrder.withdrawStatus
            if(order.withdrawStatus == WithdrawResult.CAN_APPLY)
              order.fee = (commissionConfig.findCommissionRate(order.level) * jdEntityOrder.actualFee/100).intValue()
            else if(order.withdrawStatus != WithdrawResult.UNAPPLY){
              order.preFee = (commissionConfig.findCommissionRate(order.level) * jdEntityOrder.estimateFee/100).intValue()
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
          warn("pdd order[{}] not traced,goodsId:{}",order.getOrderId,originOrder.getSkuId)
      }
    }
  }
}

object JdServiceImpl{


  def main(args: Array[String]): Unit = {
    val service = new JdServiceImpl
    service.queryAllPids()
//    val positions = service.getPositions
//    positions.result.foreach(x=>println(x.id))
//    println(service.promotionUrl(47277785579L))
  }
}
