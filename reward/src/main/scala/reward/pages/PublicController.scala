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
      tr.userId = user.id
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
  private val pids= {

    val queue = new ConcurrentLinkedQueue[String]()
    queue.add("mm_19052242_1448550179_110185950001")
    queue.add("mm_19052242_1448550179_110185900001")
    queue.add("mm_19052242_1448550179_110185750024")
    queue.add("mm_19052242_1448550179_110185700012")
    queue.add("mm_19052242_1448550179_110185650030")
    queue.add("mm_19052242_1448550179_110185600033")
    queue.add("mm_19052242_1448550179_110185350057")
    queue.add("mm_19052242_1448550179_110185350054")
    queue.add("mm_19052242_1448550179_110185250065")
    queue.add("mm_19052242_1448550179_110185250062")
    queue.add("mm_19052242_1448550179_110185150081")
    queue.add("mm_19052242_1448550179_110185050077")
    queue.add("mm_19052242_1448550179_110185050076")
    queue.add("mm_19052242_1448550179_110185000077")
    queue.add("mm_19052242_1448550179_110185000073")
    queue.add("mm_19052242_1448550179_110184850091")
    queue.add("mm_19052242_1448550179_110184850089")
    queue.add("mm_19052242_1448550179_110184800079")
    queue.add("mm_19052242_1448550179_110184800078")
    queue.add("mm_19052242_1448550179_110184750122")
    queue.add("mm_19052242_1448550179_110184750118")
    queue.add("mm_19052242_1448550179_110184650111")
    queue.add("mm_19052242_1448550179_110184500115")
    queue.add("mm_19052242_1448550179_110184450116")
    queue.add("mm_19052242_1448550179_110184400112")
    queue.add("mm_19052242_1448550179_110184350110")
    queue.add("mm_19052242_1448550179_110184300132")
    queue.add("mm_19052242_1448550179_110184150113")
    queue.add("mm_19052242_1448550179_110184000127")
    queue.add("mm_19052242_1448550179_110184000125")
    queue.add("mm_19052242_1448550179_110183950136")
    queue.add("mm_19052242_1448550179_110183800123")
    queue.add("mm_19052242_1448550179_110183700135")
    queue.add("mm_19052242_1448550179_110183700133")
    queue.add("mm_19052242_1448550179_110183700131")
    queue.add("mm_19052242_1448550179_110183700129")
    queue.add("mm_19052242_1448550179_110183700127")
    queue.add("mm_19052242_1448550179_110183650155")
    queue.add("mm_19052242_1448550179_110183650154")
    queue.add("mm_19052242_1448550179_110183600145")
    queue.add("mm_19052242_1448550179_110183350162")
    queue.add("mm_19052242_1448550179_110183350161")
    queue.add("mm_19052242_1448550179_110183300157")
    queue.add("mm_19052242_1448550179_110183200151")
    queue.add("mm_19052242_1448550179_110183200148")
    queue.add("mm_19052242_1448550179_110183150200")
    queue.add("mm_19052242_1448550179_110183150197")
    queue.add("mm_19052242_1448550179_110183100144")
    queue.add("mm_19052242_1448550179_110183050167")
    queue.add("mm_19052242_1448550179_110182950196")
    queue.add("mm_19052242_1448550179_110182900201")
    queue.add("mm_19052242_1448550179_110182900200")
    queue.add("mm_19052242_1448550179_110182900162")
    queue.add("mm_19052242_1448550179_110182850181")
    queue.add("mm_19052242_1448550179_110182750189")
    queue.add("mm_19052242_1448550179_110182750187")
    queue.add("mm_19052242_1448550179_110182700210")
    queue.add("mm_19052242_1448550179_110182600199")
    queue.add("mm_19052242_1448550179_110182600194")
    queue.add("mm_19052242_1448550179_110182550140")
    queue.add("mm_19052242_1448550179_110182400190")
    queue.add("mm_19052242_1448550179_110182400189")
    queue.add("mm_19052242_1448550179_110182300199")
    queue.add("mm_19052242_1448550179_110182250206")
    queue.add("mm_19052242_1448550179_110182200214")
    queue.add("mm_19052242_1448550179_110182150208")
    queue.add("mm_19052242_1448550179_110182100189")
    queue.add("mm_19052242_1448550179_110181950240")
    queue.add("mm_19052242_1448550179_110181950237")
    queue.add("mm_19052242_1448550179_110181900219")
    queue.add("mm_19052242_1448550179_110181850183")
    queue.add("mm_19052242_1448550179_110181850180")
    queue.add("mm_19052242_1448550179_110181800217")
    queue.add("mm_19052242_1448550179_110181800216")
    queue.add("mm_19052242_1448550179_110181700218")
    queue.add("mm_19052242_1448550179_110181700215")
    queue.add("mm_19052242_1448550179_110181700178")
    queue.add("mm_19052242_1448550179_110181650244")
    queue.add("mm_19052242_1448550179_110181550249")
    queue.add("mm_19052242_1448550179_110181500222")
    queue.add("mm_19052242_1448550179_110181500218")
    queue.add("mm_19052242_1448550179_110181300266")
    queue.add("mm_19052242_1448550179_110181250255")
    queue.add("mm_19052242_1448550179_110181250251")
    queue.add("mm_19052242_1448550179_110181100253")
    queue.add("mm_19052242_1448550179_110181000250")
    queue.add("mm_19052242_1448550179_110180950268")
    queue.add("mm_19052242_1448550179_110180850300")
    queue.add("mm_19052242_1448550179_110180850299")
    queue.add("mm_19052242_1448550179_110180800293")
    queue.add("mm_19052242_1448550179_110180800292")
    queue.add("mm_19052242_1448550179_110180750278")
    queue.add("mm_19052242_1448550179_110180700297")
    queue.add("mm_19052242_1448550179_110180650268")
    queue.add("mm_19052242_1448550179_110180600256")
    queue.add("mm_19052242_1448550179_110180550276")
    queue.add("mm_19052242_1448550179_110180550239")
    queue.add("mm_19052242_1448550179_110180500258")
    queue.add("mm_19052242_1448550179_110180450248")
    queue.add("mm_19052242_1448550179_110180450246")
    queue.add("mm_19052242_1448550179_110180450242")
    queue.add("mm_19052242_1448550179_110180350281")
    queue.add("mm_19052242_1448550179_110180300322")
    queue.add("mm_19052242_1448550179_110180150268")
    queue.add("mm_19052242_1448550179_110180100273")
    queue.add("mm_19052242_1448550179_110180000309")
    queue.add("mm_19052242_1448550179_110179900294")
    queue.add("mm_19052242_1448550179_110179850308")
    queue.add("mm_19052242_1448550179_110179750298")
    queue.add("mm_19052242_1448550179_110179750295")
    queue.add("mm_19052242_1448550179_110179700337")
    queue.add("mm_19052242_1448550179_110179600324")
    queue.add("mm_19052242_1448550179_110179600322")
    queue.add("mm_19052242_1448550179_110179550313")
    queue.add("mm_19052242_1448550179_110179550312")
    queue.add("mm_19052242_1448550179_110179550309")
    queue.add("mm_19052242_1448550179_110179400322")
    queue.add("mm_19052242_1448550179_110179350305")
    queue.add("mm_19052242_1448550179_110179350302")
    queue.add("mm_19052242_1448550179_110179250341")
    queue.add("mm_19052242_1448550179_110179250340")
    queue.add("mm_19052242_1448550179_110179200317")
    queue.add("mm_19052242_1448550179_110179200315")
    queue.add("mm_19052242_1448550179_110179150317")
    queue.add("mm_19052242_1448550179_110179100344")
    queue.add("mm_19052242_1448550179_110179100342")
    queue.add("mm_19052242_1448550179_110179000335")
    queue.add("mm_19052242_1448550179_110179000333")
    queue.add("mm_19052242_1448550179_110178850349")
    queue.add("mm_19052242_1448550179_110178800359")
    queue.add("mm_19052242_1448550179_110178750361")
    queue.add("mm_19052242_1448550179_110178600343")
    queue.add("mm_19052242_1448550179_110178550369")
    queue.add("mm_19052242_1448550179_110178500397")
    queue.add("mm_19052242_1448550179_110178500394")
    queue.add("mm_19052242_1448550179_110178450338")
    queue.add("mm_19052242_1448550179_110178400388")
    queue.add("mm_19052242_1448550179_110178400385")
    queue.add("mm_19052242_1448550179_110178300366")
    queue.add("mm_19052242_1448550179_110178200319")
    queue.add("mm_19052242_1448550179_110178150361")
    queue.add("mm_19052242_1448550179_110178150358")
    queue.add("mm_19052242_1448550179_110178100358")
    queue.add("mm_19052242_1448550179_110178100357")
    queue.add("mm_19052242_1448550179_110178000378")
    queue.add("mm_19052242_1448550179_110178000377")
    queue.add("mm_19052242_1448550179_110177900382")
    queue.add("mm_19052242_1448550179_110177800394")
    queue.add("mm_19052242_1448550179_110177800391")
    queue.add("mm_19052242_1448550179_110177650383")
    queue.add("mm_19052242_1448550179_110177650382")
    queue.add("mm_19052242_1448550179_110177600437")
    queue.add("mm_19052242_1448550179_110177600434")
    queue.add("mm_19052242_1448550179_110177550396")
    queue.add("mm_19052242_1448550179_110177550353")
    queue.add("mm_19052242_1448550179_110177500404")
    queue.add("mm_19052242_1448550179_110177350407")
    queue.add("mm_19052242_1448550179_110177250440")
    queue.add("mm_19052242_1448550179_110177250434")
    queue.add("mm_19052242_1448550179_110177100440")
    queue.add("mm_19052242_1448550179_110177100439")
    queue.add("mm_19052242_1448550179_110177100438")
    queue.add("mm_19052242_1448550179_110176950426")
    queue.add("mm_19052242_1448550179_110176950424")
    queue.add("mm_19052242_1448550179_110176950421")
    queue.add("mm_19052242_1448550179_110176800444")
    queue.add("mm_19052242_1448550179_110176750447")
    queue.add("mm_19052242_1448550179_110176650402")
    queue.add("mm_19052242_1448550179_110176600402")
    queue.add("mm_19052242_1448550179_110176550410")
    queue.add("mm_19052242_1448550179_110176550409")
    queue.add("mm_19052242_1448550179_110176500437")
    queue.add("mm_19052242_1448550179_110176500436")
    queue.add("mm_19052242_1448550179_110176500435")
    queue.add("mm_19052242_1448550179_110176500433")
    queue.add("mm_19052242_1448550179_110176400415")
    queue.add("mm_19052242_1448550179_110176400414")
    queue.add("mm_19052242_1448550179_110176400412")
    queue.add("mm_19052242_1448550179_110176350450")
    queue.add("mm_19052242_1448550179_110176350446")
    queue.add("mm_19052242_1448550179_110176250499")
    queue.add("mm_19052242_1448550179_110176200436")
    queue.add("mm_19052242_1448550179_110176150437")
    queue.add("mm_19052242_1448550179_110176100444")
    queue.add("mm_19052242_1448550179_110176100443")
    queue.add("mm_19052242_1448550179_110176050429")
    queue.add("mm_19052242_1448550179_110176000451")
    queue.add("mm_19052242_1448550179_110175900490")
    queue.add("mm_19052242_1448550179_110175900488")
    queue.add("mm_19052242_1448550179_110175900485")
    queue.add("mm_19052242_1448550179_110175800461")
    queue.add("mm_19052242_1448550179_110175750498")
    queue.add("mm_19052242_1448550179_110175550479")
    queue.add("mm_19052242_1448550179_110175300483")
    queue.add("mm_19052242_1448550179_110175250462")
    queue.add("mm_19052242_1448550179_110174750498")
    queue.add("mm_19052242_1281300405_110017350452")
    queue.add("mm_19052242_1281300405_110000500297")
    queue.add("mm_19052242_46684975_1462922473")
    queue.add("mm_19052242_41320452_173980872")

    queue
  }
}