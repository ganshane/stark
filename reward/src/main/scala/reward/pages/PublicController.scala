package reward.pages

import com.taobao.api.ApiException
import com.taobao.api.request.TbkTpwdCreateRequest
import com.taobao.api.response.TbkTpwdCreateResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.entities.{Announce, AppConfig, Slide}
import reward.services.TaobaoService
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
  @ApiOperation(value="配置列表",nickname = "config")
  @GetMapping(Array("/config"))
  def config():java.util.List[AppConfig]={
    AppConfig.all.toList
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

  @PostMapping(value=Array("/tpwd"))
  @ApiOperation(value="转链")
  @throws(classOf[ApiException])
  def createTwpd(
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
