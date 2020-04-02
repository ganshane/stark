package reward.pages

import java.awt.image.BufferedImage
import java.io.FileInputStream

import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import javax.imageio.ImageIO
import me.chanjar.weixin.common.bean.WxAccessToken
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
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
  @GetMapping(value=Array("/web_login"))
  def webLogin(): Map[String,String]={
    //先保存临时token
    val onlineUser = new OnlineUser
    onlineUser.token = userService.generateToken(null)
    onlineUser.createdAt = DateTime.now
    onlineUser.expiredAt = DateTime.now.plusMinutes(30)
    onlineUser.save()
    Map("code"->onlineUser.token)
  }
  @GetMapping(value = Array("/qr"),produces = Array(MediaType.IMAGE_JPEG_VALUE))
  @ResponseBody
  def qr(@RequestParam @ApiParam(name="code",required = true)code: String):BufferedImage={
    val file = weixinPopular.getQrcodeService.createWxaCodeUnlimit(code,"pages/search")
    val fis = new FileInputStream(file)
    try {
      ImageIO.read(fis)
    }finally{
      IOUtils.closeQuietly(fis)
    }
  }
  @GetMapping(value = Array("/qr/info"))
  @throws(classOf[IllegalStateException])
  def qrInfo(@RequestParam @ApiParam(name="code",required = true)code: String)={
    val onlineUserOpt = OnlineUser.find_by_token(code).headOption
    onlineUserOpt match{
      case Some(ou) => ou
      case _ => throw new IllegalStateException("not found")
    }
  }
  @GetMapping(value = Array("/promotion/qr"),produces = Array(MediaType.IMAGE_JPEG_VALUE))
  @ResponseBody
  @ApiOperation(value="得到自己的推广码",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  def promotionQr(@AuthenticationPrincipal user:User): BufferedImage ={
    val file = weixinPopular.getQrcodeService.createWxaCodeUnlimit(user.id.toString,"pages/me/promotion")
    val fis = new FileInputStream(file)
    try {
      ImageIO.read(fis)
    }finally{
      IOUtils.closeQuietly(fis)
    }
  }

  @PostMapping(value=Array("/login"),consumes = Array(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
  @throws(classOf[Throwable])
  def login(@RequestParam @ApiParam(name="code",required = true)code: String,
            @RequestParam @ApiParam(name="signature",required=true) signature: String,
            @RequestParam(name="raw_data") @ApiParam(required=true) rawData: String,
            @RequestParam(name="encrypted_data") @ApiParam(required=true) encryptedData: String,
            @RequestParam @ApiParam(name="iv",required=true) iv: String,
            @RequestParam(required = false) @ApiParam(name="token",required=false) token: String,
            @RequestParam(name="parent_id",required = false,defaultValue = "0") @ApiParam(name="parent_id",required=false,example = "1",defaultValue = "0") parentId:Long
           )= {
    val result = weixinPopular.jsCode2SessionInfo(code)
    val sessionKey = result.getSessionKey
    // 用户信息校验
    if (!weixinPopular.getUserService.checkUserInfo(sessionKey, rawData, signature)) {
      throw new AccountExpiredException("invalid session key")
    }
    // 解密用户信息
    val wxUser = weixinPopular.getUserService.getUserInfo(sessionKey, encryptedData, iv)
    userService.loginUser(wxUser,parentId,token)
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
