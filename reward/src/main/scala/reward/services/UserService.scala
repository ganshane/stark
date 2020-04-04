package reward.services
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import reward.entities.{OnlineUser, Recharge, User}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-12
  */
trait UserService {

  @Transactional
  def recharge(cardNo: String, cardSecret: String, user: User): Recharge

  def verifySmsCode(phone: String, code: String)

  def sendSmsCode(phone: String): Unit

  def generateToken(user: User): String

  def loginUser(wxUser: WxMaUserInfo, parentId:Long,token:String):OnlineUser

  /**
    * 定时删除过期用户
    */
  @Scheduled(fixedDelay = 300000L) //5 * 60 * 1000
  def deleteExpiredUser(): Unit
}
