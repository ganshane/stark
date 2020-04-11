package reward.pages

import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.net.URLEncoder
import java.util.concurrent.Executors

import cn.binarywang.wx.miniapp.api.WxMaService
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl
import cn.binarywang.wx.miniapp.bean.{WxMaKefuMessage, WxMaMessage, WxMaPhoneNumberInfo}
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl
import cn.binarywang.wx.miniapp.util.xml.XStreamTransformer
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import javax.imageio.ImageIO
import me.chanjar.weixin.common.bean.WxAccessToken
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import org.springframework.web.client.RestTemplate
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
  private val userService: UserService = null
  @Autowired
  private var rewardConfig: RewardConfig = _
  @Autowired
  private var weixinPopular: WxMaService = _
  @Autowired
  private var objectMapper: ObjectMapper = _

  @GetMapping(value = Array("/web_login"))
  def webLogin(): Map[String, String] = {
    //先保存临时token
    val onlineUser = new OnlineUser
    onlineUser.token = userService.generateToken(null)
    onlineUser.createdAt = DateTime.now
    onlineUser.expiredAt = DateTime.now.plusMinutes(30)
    onlineUser.save()
    Map("code" -> onlineUser.token)
  }

  @GetMapping(value = Array("/qr/custom"), produces = Array(MediaType.IMAGE_JPEG_VALUE))
  @ResponseBody
  def qr(
          @RequestParam @ApiParam(name = "page", required = true) page: String,
          @RequestParam @ApiParam(name = "scene", required = true) scene: String
        ): BufferedImage = {
    val file = weixinPopular.getQrcodeService.createWxaCodeUnlimit(scene, page)
    val fis = new FileInputStream(file)
    try {
      ImageIO.read(fis)
    } finally {
      IOUtils.closeQuietly(fis)
    }
  }

  @GetMapping(value = Array("/qr"), produces = Array(MediaType.IMAGE_JPEG_VALUE))
  @ResponseBody
  def qr(@RequestParam @ApiParam(name = "code", required = true) code: String): BufferedImage = {
    val file = weixinPopular.getQrcodeService.createWxaCodeUnlimit(code, "pages/search")
    val fis = new FileInputStream(file)
    try {
      ImageIO.read(fis)
    } finally {
      IOUtils.closeQuietly(fis)
    }
  }

  @GetMapping(value = Array("/qr/info"))
  @throws(classOf[IllegalStateException])
  def qrInfo(@RequestParam @ApiParam(name = "code", required = true) code: String) = {
    val onlineUserOpt = OnlineUser.find_by_token(code).headOption
    onlineUserOpt match {
      case Some(ou) => ou
      case _ => throw new IllegalStateException("not found")
    }
  }

  @GetMapping(value = Array("/promotion/qr"), produces = Array(MediaType.IMAGE_JPEG_VALUE))
  @ResponseBody
  @ApiOperation(value = "得到自己的推广码", authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  def promotionQr(@AuthenticationPrincipal user: User): BufferedImage = {
    val file = weixinPopular.getQrcodeService.createWxaCodeUnlimit(user.id.toString, "pages/me/promotion")
    val fis = new FileInputStream(file)
    try {
      ImageIO.read(fis)
    } finally {
      IOUtils.closeQuietly(fis)
    }
  }

  @PostMapping(value = Array("/login"), consumes = Array(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
  @throws(classOf[Throwable])
  def login(@RequestParam @ApiParam(name = "code", required = true) code: String,
            @RequestParam @ApiParam(name = "signature", required = true) signature: String,
            @RequestParam(name = "raw_data") @ApiParam(required = true) rawData: String,
            @RequestParam(name = "encrypted_data") @ApiParam(required = true) encryptedData: String,
            @RequestParam @ApiParam(name = "iv", required = true) iv: String,
            @RequestParam(required = false) @ApiParam(name = "token", required = false) token: String,
            @RequestParam(name = "parent_id", required = false, defaultValue = "0") @ApiParam(name = "parent_id", required = false, example = "1", defaultValue = "0") parentId: Long
           ) = {
    val result = weixinPopular.jsCode2SessionInfo(code)
    val sessionKey = result.getSessionKey
    // 用户信息校验
    if (!weixinPopular.getUserService.checkUserInfo(sessionKey, rawData, signature)) {
      throw new AccountExpiredException("invalid session key")
    }
    // 解密用户信息
    val wxUser = weixinPopular.getUserService.getUserInfo(sessionKey, encryptedData, iv)
    userService.loginUser(wxUser, parentId, token)
  }

  /**
    * <pre>
    * 获取用户绑定手机号信息
    * </pre>
    */
  @PostMapping(Array("/phone"))
  def phone(@RequestParam @ApiParam(name = "session_key", required = true) sessionKey: String,
            @RequestParam @ApiParam(required = true) signature: String,
            @RequestParam @ApiParam(name = "raw_data", required = true) rawData: String,
            @RequestParam @ApiParam(name = "encrypted_data", required = true) encryptedData: String,
            @RequestParam @ApiParam(name = "iv", required = true) iv: String): WxMaPhoneNumberInfo = {
    // 用户信息校验
    if (!weixinPopular.getUserService.checkUserInfo(sessionKey, rawData, signature)) {
      throw new AccountExpiredException("invalid session key")
    }
    // 解密
    weixinPopular.getUserService.getPhoneNoInfo(sessionKey, encryptedData, iv)
  }

  @GetMapping(Array("/access_token"))
  def accessToken(): WxAccessToken = {
    val accessToken = weixinPopular.getAccessToken
    val token = new WxAccessToken
    token.setAccessToken(accessToken)
    token
  }

  @GetMapping(Array("/customer","/customer/{official}"))
  @ResponseBody
  def customer(
                @ApiParam(required = false)
                @PathVariable(value="official",required = false) official:String,
                @RequestParam @ApiParam(required = true) signature: String,
                @RequestParam @ApiParam(required = true) echostr: String,
                @RequestParam @ApiParam(required = true) timestamp: String,
                @RequestParam @ApiParam(required = true) nonce: String
              ): String = {

    if (weixinPopular.checkSignature(timestamp, nonce, signature))
      echostr
    else ""
  }

  @PostMapping(value=Array("/customer","/customer/{official}"))
  @ResponseBody
  def post_customer(
                     @ApiParam(required = false)
                     @PathVariable(value="official",required = false) official:String,
                     @RequestParam @ApiParam(required = true) signature: String,
                     @RequestParam @ApiParam(required = true) timestamp: String,
                     @RequestParam @ApiParam(required = true) nonce: String,
                     @RequestBody content: String
                   ): String = {
    val popular =
      if(official != null){
        officalPopular
      }else weixinPopular

    if (!popular.checkSignature(timestamp, nonce, signature))
      return ""
    else {
      def reply(): String={
        logger.info("content:{}",content)
        val textMessage = WxMaMessage.fromXml(content) //,weixinPopular.getWxMaConfig)
        val haodanku = "http://v2.api.haodanku.com/supersearch/apikey/gofanli/keyword/" + URLEncoder.encode(textMessage.getContent, "UTF-8") + "/back/1/min_id/1/back/2/sort/2"
        val resultString = restTemplate.postForObject(haodanku, null, classOf[String])
        val result = objectMapper.readValue(resultString, classOf[HaodankuSearchResult])
        if (result.data != null) {
          result.data.foreach { item =>
            if(official == null) {
              val resource = restTemplate.getForObject(item.itempic, classOf[Resource])

              val is = resource.getInputStream
              val mediaResponse = popular.getMediaService.uploadMedia("image", "jpg", is)
              IOUtils.closeQuietly(is)

              val builder = WxMaKefuMessage.newMaPageBuilder()
              builder.pagePath("/pages/detail?itemid=%s&pid=65".format(item.itemid)).thumbMediaId(mediaResponse.getMediaId)
                .title(item.itemtitle).toUser(textMessage.getFromUser)
              popular.getMsgService.sendKefuMsg(builder.build())
            }else {
              val message = new WxMaMessage
              message.setToUser(textMessage.getFromUser)
              message.setFromUser(textMessage.getToUser)
              message.setCreateTime((System.currentTimeMillis()/1000).intValue())
              message.setMsgType("text")
              message.setContent("<a data-miniprogram-appid=\""+rewardConfig.wechat.id+"\"\n    data-miniprogram-path=\"/pages/detail?itemid="+item.itemid+"&pid=65"+"\">"+item.itemtitle+"</a>")
//              message.setMsgType("image")
//              message.setMediaId(mediaResponse.getMediaId)
              return XStreamTransformer.toXml(classOf[WxMaMessage], message)
            }
          }
        }
        return ""
      }
      if(official == null) {
        executors.execute(new Runnable {
          override def run(): Unit = {
            reply()
          }
        })
        return "success"
      }else{
        val officialResult = reply()
        logger.info("official result:{}",officialResult)
        return officialResult
      }
    }
  }
  private val restTemplate = new RestTemplate()
  private val executors = Executors.newCachedThreadPool()
  private val logger = LoggerFactory getLogger getClass
  private val officalPopular = {
    val config= new WxMaDefaultConfigImpl()
    config.setAppid("wx8f1d54cd81112c21")
    config.setSecret("2b89346df8ff6b3b6254ed581117809e")
    config.setToken("asdf")
    config.setAesKey("3KkVxHz9JuGYQJ2YRTnXccI6CHaVa0MLsjlfQrxxBUc")
    //    config.setAppid("wx72d1f13b506075a9")
    //    config.setSecret("264a825790d4ba9cedf23e87725112ff")
    val s = new WxMaServiceImpl()
    s.setWxMaConfig(config)
    s
  }
}
object TestMain{
  private val restTemplate = new RestTemplate()
  def main(args: Array[String]): Unit = {
    val headers = new HttpHeaders

    headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36")

    val request= new HttpEntity[String](headers)
    val textMessage = WxMaKefuMessage.newTextBuilder().content("测试").build()
    val haodanku = "http://v2.api.haodanku.com/supersearch/apikey/gofanli/keyword/" + URLEncoder.encode(textMessage.getText.getContent, "UTF-8") + "/back/1/min_id/1/back/2/sort/2"
    val resultString = restTemplate.postForObject(haodanku,request,classOf[String])
    val result = objectMapper.readValue(resultString,classOf[HaodankuSearchResult])
    if(result.data != null) {
      val item = result.data.head
      println("pic url",item.itempic)
      val resource = restTemplate.getForObject(item.itempic, classOf[Resource])
      //    println(util.Arrays.asList(result.data))
    }
  }
  private val objectMapper = new ObjectMapper()
}

@JsonIgnoreProperties(ignoreUnknown = true)
class HaodankuSearchResult{
  @JsonProperty
  var data:Array[Item] = _
}
@JsonIgnoreProperties(ignoreUnknown = true)
class Item{
  @JsonProperty
  var itemtitle:String = ""
  @JsonProperty
  var itemid:String=""
  @JsonProperty
  var itempic:String=""
}
