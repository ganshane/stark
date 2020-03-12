package reward.services
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import reward.entities.{Consumption, Recharge, User}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-12
  */
trait UserService {

  @Transactional
  def recharge(cardNo: String, cardSecret: String, user: User): Recharge
  @Transactional
  def consume(amount:Int, itemId:String, itemImg:String, itemLink:String,user: User): Consumption

  def verifySmsCode(phone: String, code: String)

  def sendSmsCode(phone: String): Unit

  def generateToken(user: User): String

  /**
    * 定时删除过期用户
    */
  @Scheduled(fixedDelay = 300000L) //5 * 60 * 1000
  def deleteExpiredUser(): Unit
}
