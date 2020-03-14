package reward.internal

import java.util.Collections

import org.joda.time.DateTime
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.DigestUtils
import org.springframework.web.client.RestTemplate
import reward.entities._
import reward.services.UserService
import stark.activerecord.services.DSL._
import stark.utils.services.LoggerSupport

import scala.collection.JavaConversions._

/**
  * 用户服务类
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
@Service
class UserServiceImpl extends LoggerSupport with UserService {
  private val restTemplate = new RestTemplate()
  private val LC_HEADERS=createHeaders
  private val LC_API_BASE_URL="https://leancloud.cn/1.1/"

  @Transactional
  override def recharge(cardNo:String,cardSecret:String,user:User): Recharge ={
    val cardOpt = Recharge.find_by_cardNo(cardNo).headOption
    val cardInDb = cardOpt match{
      case Some(card) =>
        if(card.cardSecret != cardSecret) throw new IllegalArgumentException("错误的卡密")
        else if(card.ownerId != null) throw new IllegalStateException("卡已经被注册")
        else {
          card.ownerId=user.id
          card.activatedAt = DateTime.now
          card.save()
        }
      case _ =>
        throw new UnsupportedOperationException("卡未找到")
    }

    val userAmountOpt = UserAmount.findOption(user.id)
    userAmountOpt match{
      case Some(ua) =>
        ua.rechargeAmount += cardInDb.amount
        ua.lastRecharge = cardInDb.activatedAt
        ua.save
      case _ =>
        val ua = new UserAmount
        ua.id = user.id
        ua.rechargeAmount += cardInDb.amount
        ua.lastRecharge = cardInDb.activatedAt
        ua.save()
    }

    cardInDb
  }


  @Transactional
  override def consume(amount: Int, itemId: String, itemImg: String, itemLink: String, user: User): Consumption = {
    val consumption = new Consumption
    consumption.userId = user.id
    consumption.amount = amount
    consumption.itemId = itemId
    consumption.itemImg = itemImg
    consumption.itemLink = itemLink
    consumption.createdAt = DateTime.now

    val userAmountOpt = UserAmount.findOption(user.id)
    userAmountOpt match{
      case Some(ua) =>
        val balance=ua.rechargeAmount-ua.consumptionAmount
        if(consumption.amount > balance)
          throw new IllegalStateException("用户余额不足，余额为%s,需要金额为:%s".format(balance,consumption.amount))
        ua.consumptionAmount += consumption.amount
        ua.lastConsume = DateTime.now()
        ua.save
        consumption.createdAt = ua.lastConsume
        consumption.save
      case _ =>
        throw new IllegalStateException("用户余额不足，余额为0")
    }
  }

  override def verifySmsCode(phone:String,code: String)= {
    val json= Map("mobilePhoneNumber"->phone)
    val requestEntity = new HttpEntity[java.util.Map[String,Any]](json, LC_HEADERS)
    restTemplate.postForEntity(LC_API_BASE_URL + "verifySmsCode/" + code, requestEntity, classOf[Void])
  }
  override def sendSmsCode(phone:String): Unit ={
    val json= Map("template"->"gofanli","mobilePhoneNumber"->phone)
    val requestEntity = new HttpEntity[java.util.Map[String,Any]](json, LC_HEADERS)
    restTemplate.postForEntity(LC_API_BASE_URL+"requestSmsCode",requestEntity,classOf[Void])
  }
  override def generateToken(user:User):String={
    val keySource=user.phone+user.id+System.currentTimeMillis()
    DigestUtils.md5DigestAsHex(keySource.getBytes)
  }
  private def createHeaders={
    val requestHeaders = new HttpHeaders()
    requestHeaders.add("X-LC-Id","2s0KOFLTsO3t4NTt4zygN8tj-gzGzoHsz")
    requestHeaders.add("X-LC-Key", "3IBcP0QFKETbWiySCHKNHDNt")
    requestHeaders.setContentType(MediaType.APPLICATION_JSON)
    requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
    requestHeaders
  }

  /**
    * 定时删除过期用户
    */
  @Scheduled(fixedDelay = 300000L) //5 * 60 * 1000
  override def deleteExpiredUser(): Unit = {
    logger.info("delete expired user")

    val num = delete[OnlineUser] where OnlineUser.expiredAt[DateTime] < DateTime.now() execute

    logger.info("finish to delete {} expired user",num)
  }


}
