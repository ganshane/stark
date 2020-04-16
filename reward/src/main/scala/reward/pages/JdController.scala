package reward.pages

import java.{lang, util}

import com.fasterxml.jackson.annotation.{JsonAlias, JsonIgnore, JsonIgnoreProperties, JsonProperty}
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.client.RestTemplate

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-12
  */
@RestController
@RequestMapping(Array("/jd"))
@Api(value="京东",description="京东",tags=Array("jd"))
@Validated
class JdController {
  val restTemplate = new RestTemplate()
  private val  apiKey = "0309abc560348702"
  private val categoryMapping:Map[Int,Int]=Map(
    11  ->  1320,
    1 -> 1315,//? 1315
    2 -> 1315,//? 1315
    9 -> 1319, //
    4 -> 1316,
    3 -> 1315, //?
    5 ->  1315, //? 配饰
    6 -> 11729,
    7 -> 17329,
    8 -> 6233,
    10 -> 1620,
    12 -> 652,
    13 -> 737,
    15 -> 6728,
    16 -> 4053,
    17 -> 6994
  )
  def categories(): Unit ={
    val url = "http://api-gw.haojingke.com/index.php/v1/api/jd/getcategory?apikey={apikey}&parentId=0&grade=0"
    val result = restTemplate.getForObject(url,classOf[util.HashMap[String,String]],apiKey)
    val it = result.get("data").asInstanceOf[util.ArrayList[util.HashMap[String,_]]].iterator()
    while(it.hasNext){
      println(it.next)
    }
  }
  def list():Unit={
    val url="http://api-gw.haojingke.com/index.php/v1/api/jd/goodslist"
  }
  @GetMapping(Array("/promotion"))
  @ApiOperation(value="多多进宝推广链接生成")
  def promotion(
                 @RequestParam(defaultValue = "0",required = true)
                 @ApiParam(value="商品ID",required = true)
                 itemid:String,
                 @RequestParam(required = false)
                 @ApiParam(value="优惠券链接地址",required =false)
                 couponurl:String
               )  ={

    var url="http://api-gw.haojingke.com/index.php/v1/api/jd/getunionurl"
    url += "?apikey="+apiKey
    url+="&goods_id="+itemid
    url+="&positionid=3000688743"
    url+= "&type=1"
    if(couponurl != null)
      url+="&couponurl="+couponurl
    url+="&owner=g"


    val response = restTemplate.getForObject(url,classOf[util.HashMap[String,String]])
    val haodankuResponse = new MockHandankuResponse[String]
    haodankuResponse.code = 1
    haodankuResponse.data = response.get("data")

    haodankuResponse
  }

  @GetMapping(Array("/search"))
  @ApiOperation(value="搜索jd")
  def search(
              @RequestParam(required = false)
              @ApiParam(value="关键词",required = false)
              keyword:String,
              @RequestParam(defaultValue = "0")
              @ApiParam(value="分类",defaultValue = "0",example = "0")
              cid:Int,
              @RequestParam(defaultValue = "1")
              @ApiParam(value="分页使用",defaultValue = "1",example = "1")
              min_id:Int,
              @RequestParam(defaultValue = "4")
              @ApiParam(value="排序",defaultValue = "4",example = "0")
              sort_type:Int
            ): MockHandankuResponse[Array[HaojingkeItem]] ={

    var url="http://api-gw.haojingke.com/index.php/v1/api/jd/goodslist?apikey="+apiKey
    val cidMapped=categoryMapping.getOrElse(cid,0).toLong
    if(cidMapped>0)
      url +="&cid1="+cidMapped
    if(keyword != null)
    url+="&keyword="+keyword //URLEncoder.encode(keyword,"UTF-8")
    if(sort_type > 0)
      url += "&sortname="+sort_type
    else
      url += "&sortname=4"
    url +="&ispg=0"
    url+="&iscoupon=1"
    url+="&pageindex="+min_id
    url+="&pagesize=50"
    println(url)

    println(restTemplate.getForObject(url,classOf[String]))
    val response = restTemplate.getForObject(url,classOf[HaojingkeResponse])
    val haodankuResponse = new MockHandankuResponse[Array[HaojingkeItem]]
    haodankuResponse.data = response.data.data
    haodankuResponse.min_id = (min_id+1).toString

    haodankuResponse
  }
  @GetMapping(Array("/detail"))
  @ApiOperation(value="查询商品详情")
  def detail(
              @RequestParam(defaultValue = "0",required = true)
              @ApiParam(value="商品ID",defaultValue = "0",example = "0",required = true)
              itemid:String
            ): MockHandankuResponse[HaojingkeItem] ={
    var url = "http://api-gw.haojingke.com/index.php/v1/api/jd/goodsdetail?apikey="+apiKey
    url += "&goods_id="+itemid

//    println(restTemplate.getForObject(url,classOf[String]))
    val haoJingkeDetailResponseData=restTemplate.getForObject(url,classOf[HaoJingkeDetailResponseData])
    val haodankuResponse = new MockHandankuResponse[HaojingkeItem]
    haodankuResponse.data = haoJingkeDetailResponseData.data

    haodankuResponse
  }

}
object JdController {
  def main(args: Array[String]): Unit = {
    val controller = new JdController
    controller.categories()
    val response = controller.search(null,11,1,1)
    println(response.data.head.itemid)
    val head = response.data.head

    controller.detail(head.itemid.toString)
    val promotionResponse = controller.promotion(head.itemid.toString,head.couponurl)
    println(promotionResponse.data)


//    val client = new DefaultJdClient("https://router.jd.com/api","", "190562315b06c899931ed8d341aafc47", "0415c3ed82df420f8c700732e7ae0c1b")
//    client.


  }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class HaojingkeResponse{
  @JsonProperty("data")
  var data:HaoJingkeResponseData = _
}
@JsonIgnoreProperties(ignoreUnknown = true)
class HaoJingkeResponseData{
  @JsonProperty("data")
  var data:Array[HaojingkeItem] = _
}
@JsonIgnoreProperties(ignoreUnknown = true)
class HaoJingkeDetailResponseData{
  @JsonProperty("data")
  var data:HaojingkeItem = _
}
@JsonIgnoreProperties(ignoreUnknown = true)
class HaojingkeItem {
  @JsonProperty
  @JsonAlias(Array("goods_name"))
  var itemtitle:String = _  //goods_name
  @JsonProperty
  @JsonAlias(Array("discount"))
  var couponmoney:String = _ //coupon_discount
  @JsonProperty
  @JsonAlias(Array("sales"))
  var itemsale:String = _  //sales_tip
  @JsonProperty
  @JsonAlias(Array("price"))
  var itemprice:String = _  //min_group_price
  @JsonProperty
  @JsonAlias(Array("shopname"))
  var shopname:String = _  // mall_name
  @JsonProperty
  var shoptype:String="jd"
  @JsonProperty
  @JsonAlias(Array("price_after"))
  var itemendprice:String = _  // 需要通过优惠券进行计算
  @JsonProperty
  @JsonAlias(Array("picurl"))
  var itempic:String = _ //goods_image_url
  @JsonProperty
  @JsonAlias(Array("goods_id"))
  var itemid:lang.Long = _  // goods_id

  @JsonAlias(Array("start_time"))
  @JsonProperty
  var start_time:Long = _ //coupon_start_time
  @JsonProperty
  @JsonAlias(Array("end_time"))
  var end_time:Long = _ //coupon_end_time
  @JsonProperty
  @JsonAlias(Array("goods_desc"))
  var itemdesc:String = _  // goods_desc
  @JsonProperty
  var couponurl:String = _
  @JsonProperty
  @JsonAlias(Array("picurls"))
  def setTaobaoImageString(picurls:String): Unit ={
    this.taobao_image_str = picurls
  }

  @JsonProperty("taobao_image")
  def taobao_image:Array[String] = taobao_image_str.split(",")  // goods_gallery_urls
  @JsonIgnore
  private var taobao_image_str=""
}
