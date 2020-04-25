package reward.pages

import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.awt.{AlphaComposite, BasicStroke, Color, Graphics2D}
import java.net.URL
import java.util

import com.google.zxing.client.j2se.{MatrixToImageConfig, MatrixToImageWriter}
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.{BarcodeFormat, EncodeHintType}
import com.taobao.api.ApiException
import com.taobao.api.request.TbkTpwdCreateRequest
import com.taobao.api.response.TbkTpwdCreateResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import javax.imageio.ImageIO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import org.springframework.web.client.RestTemplate
import reward.RewardConstants
import reward.entities.TraceOrder.CommerceType
import reward.entities._
import reward.services.{TaobaoService, TraceOrderService}
import stark.activerecord.services.DSL._

import scala.collection.JavaConversions._
/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@RestController
@RequestMapping(Array("/public"))
@Api(value="公共相关接口",description="公共相关接口",tags=Array("public"))
@Validated
class PublicController {
  @Autowired
  private val taobaoService:TaobaoService = null
  private val restTemplate:RestTemplate=new RestTemplate()
  @Autowired
  private val traceOrderService:TraceOrderService= null
  @ApiOperation(value="配置列表",nickname = "config")
  @GetMapping(Array("/config"))
  def config():java.util.List[AppConfig]={
    AppConfig.all.toList
  }
  @ApiOperation(value="预抓取")
  @GetMapping(Array("/pre"))
  def pre(
           @RequestParam(required=false)
           @ApiParam(value="code",required = false)
           code:String
         )={
    val classifies=restTemplate.getForObject("https://v2.api.haodanku.com/super_classify/apikey/gofanli",classOf[String])
    Map("config"->AppConfig.all.toList,"classifies"->classifies)
  }
  @ApiOperation(value="消息列表",nickname = "annouce")
  @GetMapping(Array("/announces"))
  def announces():java.util.List[Announce]={
    Announce.all.toList
  }
  @GetMapping(Array("/slides"))
  @ApiOperation(value="轮播图列表")
  def listSlide():java.util.List[Slide]={
    val coll = select[Slide] where Slide.status[Int] > 0
    coll.toList
  }

  @PostMapping(value = Array("/qr"),produces = Array(MediaType.IMAGE_JPEG_VALUE))
  @ResponseBody
  def qr(
          @RequestParam(required=true)
          @ApiParam(value="二维码内容",required = true)
          content:String,
          @RequestParam(required=false)
          @ApiParam(value="logo_url",required = false)
          logo_url:String,
          @RequestParam(required=false,defaultValue="300")
          @ApiParam(value="宽度",required = false,defaultValue = "300",example = "300")
          width:Int,
          @RequestParam(required=false,defaultValue="300")
          @ApiParam(value="高度",required = false,defaultValue = "300",example = "300")
          height:Int,
          @RequestParam(name="fg_color",required=false,defaultValue="-1")
          @ApiParam(name="fg_color",value="前景色",required = false,defaultValue = "-1",example = "-1")
          fgColor:Int,
          @RequestParam(name="bg_color",required=false,defaultValue="-1500093")
          @ApiParam(name="bg_color",value="背景色",required = false,defaultValue = "-1500093",example = "-1500093")
          bgColor:Int
        ): BufferedImage = {
    val writer = new QRCodeWriter
    val hints = new util.Hashtable[EncodeHintType, Any]()
    // 指定纠错等级
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
    // 指定编码格式
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8")
    hints.put(EncodeHintType.MARGIN, 1); //设置白边
    val config = new MatrixToImageConfig(fgColor, bgColor)
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

    val image = MatrixToImageWriter.toBufferedImage(bitMatrix, config)
    // Initialize combined image// Initialize combined image

    val combined = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_RGB)
    val g = combined.getGraphics.asInstanceOf[Graphics2D]

    // Write QR code to new image at position 0/0
    g.drawImage(image, 0, 0, null)
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f))

    // Write logo into combine image at position (deltaWidth / 2) and
    // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
    // the same space for the logo to be centered
    val logo = if (logo_url != null){
      // https://wx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTIfwZrE4wgoicuicbcx77CjEEpjHmUNnmwl07M7K6bzTjBqa2jxqaEicnviapwiafKbpfjPibw6icicrJiaBlw/132
      val logoUrl = new URL(logo_url)
      ImageIO.read(logoUrl)
    }else {
      ImageIO.read(getClass.getResourceAsStream("/hutao.png"))
    }
    // Calculate the delta height and width between QR code and logo// Calculate the delta height and width between QR code and logo
    val ratioWidth = image.getWidth * 2 / 10
    val ratioHeight = image.getHeight * 2 / 10
    val logoWidth = if (logo.getWidth(null) > ratioWidth) ratioWidth else logo.getWidth(null)
    val logoHeight = if (logo.getHeight(null) > ratioHeight) ratioHeight else logo.getHeight(null)

    val x = (image.getWidth - logoWidth) / 2
    val y = (image.getHeight - logoHeight) / 2
    g.drawImage(logo, Math.round(x), Math.round(y), logoWidth,logoHeight,null)


    //开始绘制图片
    val stroke = new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
    g.setStroke(stroke);// 设置笔画对象
    //指定弧度的圆角矩形
    val round = new RoundRectangle2D.Float(x,y, logoWidth, logoHeight,20,20);
    g.setColor(Color.white);
    g.draw(round);// 绘制圆弧矩形

    //设置logo 有一道灰色边框
    val stroke2 = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
    g.setStroke(stroke2);// 设置笔画对象
    val  round2 = new RoundRectangle2D.Float(x+2, y+2, logoWidth-4, logoHeight-4,20,20);
    g.setColor(new Color(128,128,128));
    g.draw(round2);// 绘制圆弧矩形


    g.dispose()
    combined.flush()
    combined
  }
  @GetMapping(value=Array("/pid"))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="得到随机PID",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def getPid(
             @AuthenticationPrincipal user:User,
             @RequestParam(required = false,defaultValue = "100")
             @ApiParam(value="优惠券金额,单位为分",required = false,example="100",defaultValue = "100")
             coupon_amount:Int,
             @RequestParam(required = true)
             @ApiParam(value="对应的商品ID",required = true)
             itemid:Long
            ):Map[String,String]={
    Map[String,String]("pid"->traceOrderService.getPid(user,coupon_amount,itemid,CommerceType.TAOBAO))
  }
  @PostMapping(value=Array("/tpwd"))
  @ApiOperation(value="转链")
  @throws(classOf[ApiException])
  def createTpwd(
                  @RequestParam(required = true)
                  @ApiParam(value="内容",required = true)
                  text:String,
                  @RequestParam(required = true)
                  @ApiParam(value="目标页",required = true)
                  url:String,
                  @RequestParam
                  @ApiParam(value="logoURL")
                  logo:String
                ):TbkTpwdCreateResponse.MapData= {
    val req = new TbkTpwdCreateRequest
    req.setText(text)
    req.setUrl(url)
    req.setLogo(logo)
    val response = taobaoService.getOrCreateTaobaoClient().execute(req)
    if(response.isSuccess) response.getData
    else throw new ApiException(response.getMsg+" "+response.getSubMsg)
  }
}
