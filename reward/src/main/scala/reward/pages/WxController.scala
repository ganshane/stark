package reward.pages

import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.bean.{WxMaJscode2SessionResult, WxMaPhoneNumberInfo}
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import io.swagger.annotations.{Api, ApiParam}
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._

/**
  * 操作微信之类的API
 *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-08
  */
@RestController
@RequestMapping(Array("/user"))
@Api(value="微信用户相关接口",description="用户相关接口")
@Validated
class WxController {

  private val weixinPopular = {
    val config= new WxMaDefaultConfigImpl()
    config.setAppid("wx72d1f13b506075a9")
    config.setSecret("264a825790d4ba9cedf23e87725112ff")
    val s = new WxMaServiceImpl()
    s.setWxMaConfig(config)
    s
  }

  @GetMapping(Array("/login"))
  def login(@RequestParam @ApiParam(name="code",required = true)code: String): WxMaJscode2SessionResult= {
      weixinPopular.jsCode2SessionInfo(code)
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
}
