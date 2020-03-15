package reward.pages

import com.taobao.api.ApiException
import com.taobao.api.request.TbkTpwdCreateRequest
import com.taobao.api.response.TbkTpwdCreateResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.entities.{Announce, Slide}
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
@Api(value="公共相关接口",description="公共相关接口")
@Validated
class PublicController {
  @Autowired
  private val taobaoService:TaobaoService = null
  @ApiOperation(value="消息列表")
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
  @ApiOperation(value="获得淘口令,API见:https://open.taobao.com/api.htm?spm=a219a.7386797.0.0.32e2669a2mpupt&source=search&docId=31127&docType=2")
  @throws(classOf[ApiException])
  def createTwpd(
               @RequestParam(required = true)
               @ApiParam(value="口令弹框内容",required = true)
               text:String,
               @RequestParam(required = true)
               @ApiParam(value="口令跳转目标页",required = true)
               url:String,
               @RequestParam
               @ApiParam(value="口令弹框logoURL")
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
