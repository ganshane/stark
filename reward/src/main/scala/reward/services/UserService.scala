package reward.services

import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import reward.entities.User

/**
  * 用户服务类
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
@Service
class UserService {
  def verifySmsCode(code: String): Boolean = {
    true
  }
  def generateToken(user:User):String={
    val keySource=user.phone+user.id+System.currentTimeMillis()
    Base64Utils.encodeToString(keySource.getBytes)
  }
}
