package reward.internal

import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reward.entities.UserAmount
import reward.services.WxService
import stark.utils.services.LoggerSupport

import scala.collection.JavaConversions._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-04
  */
@Service
class WxServiceImpl extends WxService with LoggerSupport{
  @Autowired
  private val weixinPopular:WxMaService = null
  def sendConsumptionMessage(openId:String,userAmount: UserAmount,amount:Int): Unit ={
    try {
      //发送消息
      val subscribeMessage = WxMaSubscribeMessage.builder()
        .templateId("G_qb5ozVYiGv632EnIYe_S42xKR7vT9ATSfvYoWHujQ")
        .data(List(
          new WxMaSubscribeMessage.Data("phrase1", "消费"),
          new WxMaSubscribeMessage.Data("amount2", String.valueOf(amount/100.0)),
          new WxMaSubscribeMessage.Data("time3", DateTime.now().toString("YYYY-MM-dd HH:mm")),
          new WxMaSubscribeMessage.Data("amount4.", String.valueOf((userAmount.rechargeAmount - userAmount.consumptionAmount) / 100.0)),
          new WxMaSubscribeMessage.Data("thing5", "购物消费")
        ))
        .toUser(openId)
        .page("pages/discount/index")
      weixinPopular.getMsgService.sendSubscribeMsg(subscribeMessage.build())
    }catch{
      case e: Throwable =>
        logger.error(e.getMessage,e)
    }
  }
  def sendRechargeMessage(openId:String,userAmount: UserAmount,amount:Int): Unit ={
    try {
      //发送消息
      val subscribeMessage = WxMaSubscribeMessage.builder()
        .templateId("G_qb5ozVYiGv632EnIYe_S42xKR7vT9ATSfvYoWHujQ")
        .data(List(
          new WxMaSubscribeMessage.Data("phrase1", "充值"),
          new WxMaSubscribeMessage.Data("amount2", String.valueOf(amount/100.0)),
          new WxMaSubscribeMessage.Data("time3", DateTime.now().toString("YYYY-MM-dd HH:mm")),
          new WxMaSubscribeMessage.Data("amount4.", String.valueOf((userAmount.rechargeAmount - userAmount.consumptionAmount) / 100.0)),
          new WxMaSubscribeMessage.Data("thing5", "充值")
        ))
        .toUser(openId)
        .page("pages/discount/index")
      weixinPopular.getMsgService.sendSubscribeMsg(subscribeMessage.build())
    }catch{
      case e: Throwable =>
        logger.error(e.getMessage,e)
    }

  }
}
