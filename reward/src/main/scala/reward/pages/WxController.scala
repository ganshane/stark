package reward.pages

import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import io.swagger.annotations.{Api, ApiParam}
import me.chanjar.weixin.common.bean.WxAccessToken
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.config.RewardConfig
import reward.entities.{OnlineUser, User}
import reward.services.UserService

/**
  * 操作微信之类的API
 *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-08
  */
@RestController
@RequestMapping(Array("/wx"))
@Api(value="微信相关接口",description="微信相关接口")
@Validated
class WxController {

  @Autowired
  private val userService:UserService = null
  @Autowired
  private var rewardConfig:RewardConfig= _
  private lazy val weixinPopular = {
    val config= new WxMaDefaultConfigImpl()
    config.setAppid(rewardConfig.wechat.id)
    config.setSecret(rewardConfig.wechat.secret)
//    config.setAppid("wx72d1f13b506075a9")
//    config.setSecret("264a825790d4ba9cedf23e87725112ff")
    val s = new WxMaServiceImpl()
    s.setWxMaConfig(config)
    s
  }

  @PostMapping(value=Array("/login"),consumes = Array(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
  def login(@RequestParam @ApiParam(name="code",required = true)code: String,
            @RequestParam @ApiParam(name="signature",required=true) signature: String,
            @RequestParam(name="raw_data") @ApiParam(required=true) rawData: String,
            @RequestParam(name="encrypted_data") @ApiParam(required=true) encryptedData: String,
            @RequestParam @ApiParam(name="iv",required=true) iv: String)= {
    val result = weixinPopular.jsCode2SessionInfo(code)
    val sessionKey = result.getSessionKey
    // 用户信息校验
    if (!weixinPopular.getUserService.checkUserInfo(sessionKey, rawData, signature)) {
      throw new AccountExpiredException("invalid session key")
    }
    // 解密用户信息
    val wxUser = weixinPopular.getUserService.getUserInfo(sessionKey, encryptedData, iv)
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
      }
    }

    val onlineUserOpt = OnlineUser.find_by_userId(user.id).headOption
    onlineUserOpt match{
      case Some(ou) =>
        ou.updatedAt= DateTime.now
        ou.expiredAt = DateTime.now.plusMinutes(30)
        ou.save()
      case _ =>
        val onlineUser = new OnlineUser
        onlineUser.token = userService.generateToken(user)
        onlineUser.userId = user.id
        onlineUser.createdAt = DateTime.now
        onlineUser.expiredAt = DateTime.now.plusMinutes(30)
        onlineUser.save()
    }
  }

  /**
    * <pre>
    * 获取用户绑定手机号信息
    * </pre>
    */
  @PostMapping(Array("/phone"))
  def phone(@RequestParam @ApiParam(name="session_key",required=true)sessionKey: String,
            @RequestParam @ApiParam(required=true) signature: String,
            @RequestParam @ApiParam(name="raw_data",required=true) rawData: String,
            @RequestParam @ApiParam(name="encrypted_data",required=true) encryptedData: String,
            @RequestParam @ApiParam(name="iv",required=true) iv: String): WxMaPhoneNumberInfo = {
    // 用户信息校验
    if (!weixinPopular.getUserService.checkUserInfo(sessionKey, rawData, signature)) {
      throw new AccountExpiredException("invalid session key")
    }
    // 解密
    weixinPopular.getUserService.getPhoneNoInfo(sessionKey, encryptedData, iv)
  }
  @GetMapping(Array("/access_token"))
  def accessToken(): WxAccessToken= {
    val accessToken = weixinPopular.getAccessToken
    val token=new WxAccessToken
    token.setAccessToken(accessToken)
    token
  }
}
