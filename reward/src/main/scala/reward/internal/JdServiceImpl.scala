package reward.internal

import java.util
import java.util.concurrent.ConcurrentLinkedQueue

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.jd.open.api.sdk.{DefaultJdClient, JdClient}
import jd.union.open.promotion.common.get.request.{PromotionCodeReq, UnionOpenPromotionCommonGetRequest}
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reward.services.JdService
import reward.services.JdService.{MiaoYouQuanResponse, PidData, PromotionPosition}

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
@Service
class JdServiceImpl extends JdService{
  //jos https://api.jd.com/routerjson
  private val SERVER_URL="https://router.jd.com/api"
  private val accessToken="b57733a7028b09011e441a2ec416d0da68f30ba9872b378c85f288ebea29b650f583d9e209681189"
  private val appKey="190562315b06c899931ed8d341aafc47"
  private val appSecret="0415c3ed82df420f8c700732e7ae0c1b"
  private val client = new DefaultJdClient(SERVER_URL, accessToken, appKey, appSecret)

  private val MYQ_AP_KEY:String ="a781b1d1-60d0-f570-dfea-64f02c7f834b"
  private val MYQ_JD_CONNECT_KEY:String ="J4672635259984466"
  private val restTemplate:RestTemplate = {
    val rt = new RestTemplate()
    rt.getMessageConverters.foreach{
      case converter: MappingJackson2HttpMessageConverter => converter.getObjectMapper.registerModule(DefaultScalaModule)
      case _ =>
    }
    rt
  }
  private var objectMapper:ObjectMapper = {
    new ObjectMapper().registerModule(DefaultScalaModule)
  }

  def getClient(): JdClient = client
  def createPidByUserId(userId:Long):String={
    val url = "http://api.web.21ds.cn/jingdong/createUnionPosition"
    val builder = UriComponentsBuilder.fromUriString(url)
    builder.queryParam("apkey",MYQ_AP_KEY)
    builder.queryParam("key_id",MYQ_JD_CONNECT_KEY)
    builder.queryParam("unionType",1.toString)
    builder.queryParam("type",3.toString)
    builder.queryParam("siteId",223314.toString)
    builder.queryParam("spaceNameList",userId.toString)

    val json = restTemplate.getForObject(builder.build().toUri,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[PidData]](){} )
    if(response.code == 200){
      response.data.resultList.head._2
    }else {
      throw new RuntimeException(response.msg)
    }
  }
  override def queryAllPids(): util.Queue[String]={
    val queue = new ConcurrentLinkedQueue[String]()
    loopGetAllPid().foreach(queue.offer)
    queue
  }

  @tailrec
  private def loopGetAllPid(page:Int=1,pageSize:Int=100,data:List[String]=List()): List[String]={
    var url = "http://api.web.21ds.cn/jingdong/getUnionPosition?"
    url += buildParameters(Map(
      "apkey" -> MYQ_AP_KEY,
      "key_id"->MYQ_JD_CONNECT_KEY,
      "unionType"->1,
      "pageIndex"->page,
      "pageSize"->pageSize
    ))

    val json = restTemplate.getForObject(url,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[PromotionPosition]](){} )
    if(response.code == 200) {
      println(response.data.total)
      val list = response.data.result.map(_.id.toString).toList
      val nextData = data ::: list
      println("total data:",response.data.total)
      if(nextData.size >= response.data.total){
        nextData
      }else{
        loopGetAllPid(page+1,pageSize,nextData)
      }
    }else{
      throw new RuntimeException("query pid excepiton,msg:"+response.msg)
    }
  }
  def getPositions: PromotionPosition ={
    var url = "http://api.web.21ds.cn/jingdong/getUnionPosition?"
    url += buildParameters(Map(
      "apkey" -> MYQ_AP_KEY,
      "key_id"->MYQ_JD_CONNECT_KEY,
      "unionType"->1,
      "pageIndex"->1,
      "pageSize"->100
    ))

    val json = restTemplate.getForObject(url,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[PromotionPosition]](){} )
    if(response.code == 200) {
      response.data
    }
    else throw new RuntimeException(response.msg)
  }
  private def buildParameters(parameters:Map[String,Any]): String={
    parameters.map{case (k,v)=>{
       k+"="+v
    }}.mkString("&")
  }

  def promotionUrl2(): Unit ={
    val request = new UnionOpenPromotionCommonGetRequest
    val req = new PromotionCodeReq
    val couponUrl="http://coupon.m.jd.com/coupons/show.action?key=312a3c55d7ff4145ac76e8e72ef51c78&roleId=29730492&to=item.jd.com/47277785579.html"
    req.setCouponUrl(couponUrl)
    req.setMaterialId("https://item.jd.com/"+47277785579L+".html")
//    req.setSiteId("223314")
//    req.setPid("3000688743")
    req.setPositionId("3000688743".toLong)
    request.setPromotionCodeReq(req)
    val response = client.execute(request)
    println(response.getCode)
    println(response.getMessage)
    println(response.getData)
  }
}

object JdServiceImpl{


  def main(args: Array[String]): Unit = {
    val service = new JdServiceImpl
    service.queryAllPids()
//    val positions = service.getPositions
//    positions.result.foreach(x=>println(x.id))
//    println(service.promotionUrl(47277785579L))
  }
}
