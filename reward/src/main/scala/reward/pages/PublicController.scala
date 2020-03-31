package reward.pages

import java.util.concurrent.ConcurrentLinkedQueue

import com.taobao.api.ApiException
import com.taobao.api.request.TbkTpwdCreateRequest
import com.taobao.api.response.TbkTpwdCreateResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities._
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
  private val pids={
    val queue = new ConcurrentLinkedQueue[String]()
    queue.add("mm_19052242_1281300405_110017350452")
    queue.add("mm_19052242_1281300405_110000500297")
    queue.add("mm_19052242_46684975_1462922473")
    queue.add("mm_19052242_41320452_173980872")
    queue.add("mm_19052242_1448550179_110174750498")
    queue.add("mm_19052242_1448550179_110175550479")
    queue.add("mm_19052242_1448550179_110176650402")
    queue.add("mm_19052242_1448550179_110177550353")
    queue.add("mm_19052242_1448550179_110178450338")
    queue.add("mm_19052242_1448550179_110178600343")
    queue.add("mm_19052242_1448550179_110180550239")
    queue.add("mm_19052242_1448550179_110181700178")
    queue.add("mm_19052242_1448550179_110182550140")
    queue.add("mm_19052242_1448550179_110182900162")

    queue
  }
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
  @GetMapping(value=Array("/pid"))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="得到随机PID",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def getPid(@AuthenticationPrincipal user:User):Map[String,String]={
    val pid = pids.poll()
    try{
      val tr=new TraceOrder
      tr.pid = pid
      tr.user_id = user.id
      tr.createdAt= DateTime.now
      tr.save()
      Map[String,String]("pid"->pid)
    }finally{
      pids.offer(pid)
    }
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
