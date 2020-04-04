package reward.internal

import java.util.Collections

import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.bean.{WxMaSubscribeMessage, WxMaUserInfo}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
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
import scala.util.Random

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
  @Autowired
  private val weixinPopular:WxMaService = null


  @Transactional
  override def loginUser(wxUser: WxMaUserInfo, parentId: Long, token: String): OnlineUser = {
    //通过微信用户来找OpenId
    lazy val user = {
      val headOpt = User.find_by_openId(wxUser.getOpenId).headOption
      headOpt match {
        case Some(user) => {
          //update user info
          user.nickName= wxUser.getNickName
          user.avatar = wxUser.getAvatarUrl
          user.unionId = wxUser.getUnionId
          user.save()
        }
        case _ => //没找到则进行注册
          val user= new User
          user.openId= wxUser.getOpenId
          user.unionId = wxUser.getUnionId
          user.nickName= wxUser.getNickName
          user.avatar = wxUser.getAvatarUrl
          user.createdAt = DateTime.now
          user.save()
          if(parentId > 0) { //大于0的合理用户
            //查询爷节点
            val grandpaOpt = UserRelation
              .find_by_userId(parentId)
              .orderBy(UserRelation.level[Int].asc)
              .limit(1).headOption
            //保存爷节点关系
            grandpaOpt match {
              case Some(grandpa) =>
                val ur = new UserRelation
                ur.level = 2
                ur.userId = user.id
                ur.parentId = grandpa.parentId
                ur.createdAt = DateTime.now
                ur.save()
              case _ =>
            }
            //保存父节点关系
            val ur = new UserRelation
            ur.level = 1
            ur.userId = user.id
            ur.parentId = parentId
            ur.createdAt = DateTime.now
            ur.save()
          }

          user
      }
    }

    if(token == null) { //正常用户登录
      val onlineUserOpt = OnlineUser.find_by_userId(user.id).headOption
      onlineUserOpt match {
        case Some(ou) =>
          ou.updatedAt = DateTime.now
          ou.expiredAt = DateTime.now.plusMinutes(30)
          ou.save()
        case _ =>
          val onlineUser = new OnlineUser
          onlineUser.token = generateToken(user)
          onlineUser.userId = user.id
          onlineUser.createdAt = DateTime.now
          onlineUser.expiredAt = DateTime.now.plusMinutes(30)
          onlineUser.save()
      }
    }else {//网站用户登录
      val onlineUserOpt = OnlineUser.find_by_token(token).headOption
      onlineUserOpt match {
        case Some(ou) =>
          //删除之前登录的用户
          delete[OnlineUser] where OnlineUser.userId === user.id execute

          ou.userId = user.id
          ou.updatedAt = DateTime.now
          ou.expiredAt = DateTime.now.plusMinutes(30)
          ou.save()
        case _ =>
          throw new IllegalStateException("token not found")
      }

    }

  }

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
    val userAmount = userAmountOpt match{
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
    try {
      //发送消息
      val subscribeMessage = WxMaSubscribeMessage.builder()
        .templateId("G_qb5ozVYiGv632EnIYe_S42xKR7vT9ATSfvYoWHujQ")
        .data(List(
          new WxMaSubscribeMessage.Data("phrase1", "充值"),
          new WxMaSubscribeMessage.Data("amount2", String.valueOf(cardInDb.amount)),
          new WxMaSubscribeMessage.Data("time3", DateTime.now().toString("YYYY-MM-dd HH:mm")),
          new WxMaSubscribeMessage.Data("amount4.", String.valueOf((userAmount.rechargeAmount - userAmount.consumptionAmount) / 100.0)),
          new WxMaSubscribeMessage.Data("thing5", "充值")
        ))
        .toUser(user.openId)
        .page("pages/discount/discountCard")
      weixinPopular.getMsgService.sendSubscribeMsg(subscribeMessage.build())
    }catch{
      case e: Throwable =>
        logger.error(e.getMessage,e)
    }

    cardInDb
  }


  @Transactional
  override def consume(amount: Int, itemId: String, itemImg: String, itemLink: String, user: User): Consumption = {
    val consumption = new Consumption
    consumption.userId = user.id
    consumption.amount = amount
    consumption.createdAt = DateTime.now

    val userAmountOpt = UserAmount.findOption(user.id)
    val userAmount = userAmountOpt match{
      case Some(ua) =>
        val balance=ua.rechargeAmount-ua.consumptionAmount
        if(consumption.amount > balance)
          throw new IllegalStateException("用户余额不足，余额为%s,需要金额为:%s".format(balance,consumption.amount))
        consumption.createdAt = ua.lastConsume
        consumption.save

        ua.consumptionAmount += consumption.amount
        ua.lastConsume = DateTime.now()
        ua.save
      case _ =>
        throw new IllegalStateException("用户余额不足，余额为0")
    }
    try {
      //发送消息
      val subscribeMessage = WxMaSubscribeMessage.builder()
        .templateId("G_qb5ozVYiGv632EnIYe_S42xKR7vT9ATSfvYoWHujQ")
        .data(List(
          new WxMaSubscribeMessage.Data("phrase1", "消费"),
          new WxMaSubscribeMessage.Data("amount2", String.valueOf(amount/100.0)),
          new WxMaSubscribeMessage.Data("time3", DateTime.now().toString("YYYY-MM-dd HH:mm")),
          new WxMaSubscribeMessage.Data("amount4.", String.valueOf((userAmount.rechargeAmount - userAmount.consumptionAmount) / 100.0)),
          new WxMaSubscribeMessage.Data("thing5", "消费")
        ))
        .toUser(user.openId)
        .page("pages/discount/discountCard")
      weixinPopular.getMsgService.sendSubscribeMsg(subscribeMessage.build())
    }catch{
      case e: Throwable =>
        logger.error(e.getMessage,e)
    }

    consumption
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
    val keySource =
      if(user != null) user.nickName+user.id+System.currentTimeMillis()
      else System.currentTimeMillis()+""+Random.nextLong()
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
