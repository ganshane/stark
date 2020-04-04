package reward.services

import reward.entities.UserAmount

/**
  * 和微信相关的操作
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-04
  */
trait WxService {
  def sendConsumptionMessage(openId:String,userAmount: UserAmount,amount:Int): Unit
  def sendRechargeMessage(openId:String,userAmount: UserAmount,amount:Int): Unit
}
