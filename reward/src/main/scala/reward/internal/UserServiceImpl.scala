package reward.internal

import java.util.Collections

import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo
import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpEntity, HttpHeaders, HttpStatus, MediaType}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.{Propagation, Transactional}
import org.springframework.util.DigestUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import reward.entities.TraceOrder.CommerceType
import reward.entities._
import reward.services.{UserService, WxService}
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
  @Autowired
  private val wxService:WxService= null
  @Autowired
  private val objectMapper:ObjectMapper = null



  @Transactional(propagation = Propagation.MANDATORY)
  override def getOrCreateUserStatistic(userId: Long): UserStatistic = {
    UserStatistic.findOption(userId) match{
      case Some(us) => us
      case _ =>
        val us = new UserStatistic
        us.userId = userId
        us.save()
    }
  }

  @Transactional
  override def doWithdraw(id: Long): UserWithdraw = {
    val uw = UserWithdraw.find(id)
    uw.successTime = DateTime.now()
    uw.sendResult = UserWithdraw.WithdrawResult.SUCCESS
    uw.save()

    //提现成功后更新UserOrder提现状态
    UserOrder.findOption(uw.userOrderId) match{
      case Some(uo) =>
        uo.withdrawStatus = uw.sendResult
        uo.save()
      case _ =>
        logger.warn("UserOrder not found,id:"+uw.userOrderId)
    }

    UserStatistic.findOption(uw.userId) match{
      case Some(us) =>
        us.totalWithdrawAmount += uw.amount
        us.save()
      case _ =>
        logger.warn("user not found,id:"+uw.userId)
    }

    uw
  }
  @Transactional
  override def withdraw(userOrderId:Long,currentUser: User): UserWithdraw ={
    //1.检测是否已经存在提现记录
    val userWithdrawOpt = UserWithdraw.find_by_userOrderId_and_userId(userOrderId,currentUser.id).headOption
    if(userWithdrawOpt.isDefined)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"已经提现过，请勿重复操作")
    //2.检测订单是否存在
    val userOrderOpt = UserOrder.findOption(userOrderId)
    val userOrder = userOrderOpt match{
      case Some(ua) => ua
      case _ => throw new ResponseStatusException(HttpStatus.NOT_FOUND,"订单未找到")
    }
    //3.检测原始订单状态
    val commerceOrderOpt = {
      userOrder.tradeOrder.commerceType match{
        case CommerceType.TAOBAO =>
          TaobaoPublisherOrder.findOption(userOrder.tradeOrder.tradeId)
        case CommerceType.JD=>
          JdOrder.findOption(userOrder.tradeOrder.tradeId)
        case CommerceType.PDD=>
          PddOrder.findOption(userOrder.tradeOrder.tradeId)
      }
    }
    commerceOrderOpt match{
      case Some(o) =>
        if(o.getCommerceOrderStatus != CommerceOrderStatus.SETTLED){
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"订单尚不能提现")
        }
      case _ => throw new ResponseStatusException(HttpStatus.NOT_FOUND,"原始订单未找到")
    }
    //4.检测是否存在对应佣金配置
    /*
    val commissionConfigOpt= AppConfig.find_by_key(RewardConstants.COMMISSION_CONFIG_KEY).headOption
    val commissionConfig = commissionConfigOpt match{
      case Some(cc) =>
        cc.readAsCommissionConfig(objectMapper)
      case _ => throw new ResponseStatusException(HttpStatus.NOT_FOUND,"佣金配置未找到")
    }
    val rate = userOrder.level match {
      case 0 => commissionConfig.level_0
      case 1=> commissionConfig.level_1
      case 2=> commissionConfig.level_2
      case _ =>throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"错误的用户等级")
    }
    */
    //5.申请提现
    val userWithdraw = new UserWithdraw
    userWithdraw.amount = userOrder.fee //(rate * (taobaoOrder.pubShareFee.toDouble*100)).intValue()
    userWithdraw.level = userOrder.level
    //红包订单ID,原始订单ID和当前用户的id
    userWithdraw.redPackId="%s%010d".format(userOrder.tradeOrder.tradeId,currentUser.id)
    userWithdraw.sendResult = UserWithdraw.WithdrawResult.APPLY
    userWithdraw.userId = currentUser.id
    userWithdraw.userOrderId = userOrderId
    userWithdraw.applyTime = DateTime.now()
    userWithdraw.save()
    //6.更新UserOrder的体现状态
    userOrder.withdrawStatus = userWithdraw.sendResult
    userOrder.save()

    userWithdraw
  }

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
              .find_by_userId_and_level(parentId,1)
              .headOption
            //保存爷节点关系
            grandpaOpt match {
              case Some(grandpa) =>
                val ur = new UserRelation
                ur.level = 2
                ur.userId = user.id
                ur.parentId = grandpa.parentId
                ur.createdAt = DateTime.now
                ur.save()
                //爷节点的团队统计数据
                val grandpaUserStatistic = getOrCreateUserStatistic(ur.parentId)
                grandpaUserStatistic.level2TeamNum += 1
                grandpaUserStatistic.save()
              case _ =>
            }
            //保存父节点关系
            val ur = new UserRelation
            ur.level = 1
            ur.userId = user.id
            ur.parentId = parentId
            ur.createdAt = DateTime.now
            ur.save()
            //父节点的团队统计数据
            val parentUserStatistic = getOrCreateUserStatistic(ur.parentId)
            parentUserStatistic.level1TeamNum += 1
            parentUserStatistic.save()
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

    val userAmountOpt = UserStatistic.findOption(user.id)
    val userAmount = userAmountOpt match{
      case Some(ua) =>
        ua.rechargeAmount += cardInDb.amount
        ua.lastRecharge = cardInDb.activatedAt
        ua.save()
      case _ =>
        val ua = new UserStatistic
        ua.userId = user.id
        ua.rechargeAmount += cardInDb.amount
        ua.lastRecharge = cardInDb.activatedAt
        ua.save()
    }

    wxService.sendRechargeMessage(user.openId,userAmount,cardInDb.amount)

    cardInDb
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
