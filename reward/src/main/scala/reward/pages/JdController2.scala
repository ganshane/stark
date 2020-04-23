package reward.pages

import java.{lang, util}

import com.fasterxml.jackson.annotation.{JsonAlias, JsonIgnore, JsonProperty}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reward.entities.TraceOrder.CommerceType
import reward.entities.User
import reward.services.JdService.MiaoYouQuanResponse
import reward.services.TraceOrderService

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-12
  */
@RestController
@RequestMapping(Array("/jd2"))
@Api(value="京东",description="京东",tags=Array("jd"))
@Validated
class JdController2 {
  private val MYQ_AP_KEY:String ="a781b1d1-60d0-f570-dfea-64f02c7f834b"
  private val MYQ_JD_CONNECT_KEY:String ="J4672635259984466"

  private val restTemplate = new RestTemplate()
  @Autowired
  private val traceOrderService:TraceOrderService = null
  @Autowired
  private val objectMapper:ObjectMapper= null
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
                 @AuthenticationPrincipal user:User,
                 @RequestParam(required = false,defaultValue = "100")
                 @ApiParam(value="优惠券金额,单位为分",required = false,example="100",defaultValue = "100")
                 coupon_amount:Int,
                 @RequestParam(required = true)
                 @ApiParam(value="对应的商品ID",required = true)
                 itemid:Long,
                 @RequestParam(required = false)
                 @ApiParam(value="优惠券链接地址",required =false)
                 couponurl:String
               )  ={

    val pid = traceOrderService.getPid(user,coupon_amount,itemid,CommerceType.JD)
    val url = "http://api.web.21ds.cn/jingdong/doItemCpsUrl?"
    val material="https://item.jd.com/"+itemid+".html"
    val parameters = Map(
      "apkey" -> MYQ_AP_KEY,
      "key_id"->MYQ_JD_CONNECT_KEY,
      "materialId"->material,
      "positionId"->pid,
      "couponUrl"->couponurl
    )

    // 准备参数
    val uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    parameters.foreach{
      case (k,v) => uriComponentsBuilder.queryParam(k, v)
      case _ =>
    }
    val uri = uriComponentsBuilder.build().encode().toUri
    val json = restTemplate.getForObject(uri,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[Map[String,String]]](){} )
    if(response.code == 200) {
      val haodankuResponse = new MockHandankuResponse[String]
      haodankuResponse.code = 1
      haodankuResponse.data = response.data("shortURL")
      haodankuResponse
    }
    else throw new RuntimeException(response.msg)
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

    val url="http://api.web.21ds.cn/jingdong/getJdUnionItems"
    val uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    uriComponentsBuilder.queryParam("apKey",MYQ_AP_KEY)
    uriComponentsBuilder.queryParam("key_id",MYQ_JD_CONNECT_KEY)

    val cidMapped=categoryMapping.getOrElse(cid,0).toLong
    if(cidMapped>0){
      uriComponentsBuilder.queryParam("cid1",cidMapped.toString)
    }
    if(keyword != null)
      uriComponentsBuilder.queryParam("keyword",keyword)
    if(sort_type > 0)
      uriComponentsBuilder.queryParam("sortName",sort_type.toString)
    else
      uriComponentsBuilder.queryParam("sortName","4")

    uriComponentsBuilder.queryParam("isPg","0")
    uriComponentsBuilder.queryParam("isCoupon","1")
    uriComponentsBuilder.queryParam("pageIndex","1")
    uriComponentsBuilder.queryParam("pageSize","50")


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
object JdController2 {
  def main(args: Array[String]): Unit = {
    val controller = new JdController
    controller.categories()
    val response = controller.search(null,11,1,1)
    println(response.data.head.itemid)
    val head = response.data.head

    controller.detail(head.itemid.toString)
    //    val promotionResponse = controller.promotion(head.itemid.toString,head.couponurl)
    //    println(promotionResponse.data)


    //    val client = new DefaultJdClient("https://router.jd.com/api","", "190562315b06c899931ed8d341aafc47", "0415c3ed82df420f8c700732e7ae0c1b")
    //    client.


  }
}

object JdGoods{
  class CommissionInfo{
    var commission:String=_
    var commissionShare:String = _
  }
  class CouponInfo{
    var couponList:List[CouponInfoData] =_
  }
  class CouponInfoData{
    var bindType:String = _
    var discount:String = _
    var link:String = _
    var platformType:String = _
    var quota:String = _
    var getStartTime:String = _
    var getEndTime:String = _
    var useStartTime:String = _
    var useEndTime:String = _
    var isBest:String = _
  }
  class ImageInfo{
    var imageList:List[Map[String,String]] = _
  }
  class PriceInfo{
    var price:String = _
  }
  class ShopInfo{
    var shopName:String = _
    var shopId:String = _
  }
  class Goods{
    @JsonProperty
    @JsonAlias(Array("skuName"))
    var itemtitle:String = _  //goods_name
    @JsonProperty
    @JsonAlias(Array("discount"))
    var couponmoney:String = _ //coupon_discount
    @JsonProperty("couponInfo")
    private def unpackCoupon(couponInfo: CouponInfo): Unit = {
      if(couponInfo.couponList != null){
        val bestCouponOpt= couponInfo.couponList.find(_.isBest == "1")
        bestCouponOpt match{
          case Some(coupon) =>
            this.couponmoney = coupon.discount
          case _=>
            this.couponmoney = couponInfo.couponList.head.discount
        }
      }
    }
    @JsonProperty
    @JsonAlias(Array("inOrderCount30Days"))
    var itemsale:String = _  //sales_tip
    @JsonProperty
    @JsonAlias(Array("price"))
    var itemprice:String = _  //min_group_price
    @JsonProperty("priceInfo")
    private def unpackPrice(priceInfo: PriceInfo): Unit ={
      this.itemprice=priceInfo.price
    }
    @JsonProperty
    @JsonAlias(Array("shopname"))
    var shopname:String = _  // mall_name
    @JsonProperty("shopInfo")
    private def unpackShop(shopInfo: ShopInfo): Unit ={
      this.shopname = shopInfo.shopName
    }
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
    @JsonProperty
    @JsonAlias(Array("commission"))
    def setCommission(commision:String): Unit ={
      this.commissionStr = commision
    }

    @JsonProperty("tkmoney")
    def getTkmoney:Int={
      if(this.commissionStr != null)
        (this.commissionStr.toDouble * 100).intValue()
      else 0
    }

    @JsonProperty("taobao_image")
    def taobao_image:Array[String] = taobao_image_str.split(",")  // goods_gallery_urls
    @JsonIgnore
    private var taobao_image_str=""
    @JsonIgnore
    private var commissionStr:String = _
  }
}


