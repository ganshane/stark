package reward.pages

import java.net.URLDecoder
import java.{lang, util}

import com.fasterxml.jackson.annotation.{JsonAlias, JsonIgnore, JsonIgnoreProperties, JsonProperty}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import reward.entities.TraceOrder.CommerceType
import reward.entities.{TraceOrder, User}
import reward.services.JdService.MiaoYouQuanResponse
import reward.services.{JdService, TraceOrderService}
import stark.activerecord.services.DSL

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
  private val MYQ_AP_KEY:String ="a781b1d1-60d0-f570-dfea-64f02c7f834b"
  private val MYQ_JD_CONNECT_KEY:String ="J4672635259984466"

  private val restTemplate = new RestTemplate()
//  @Autowired
  private var objectMapper:ObjectMapper= {
    new ObjectMapper().registerModule(DefaultScalaModule)
  }
  @Autowired
  private val traceOrderService:TraceOrderService = null
  @Autowired
  private val jdService:JdService= null
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
  @ApiOperation(value="JD推广链接生成")
  def promotion(
                 @AuthenticationPrincipal user:User,
                 @RequestParam(required = false,defaultValue = "100")
                 @ApiParam(value="优惠券金额,单位为分",required = false,example="100",defaultValue = "100")
                 coupon_amount:Int,
                 @RequestParam(required = true)
                 @ApiParam(value="对应的商品ID",required = true)
                 item_id:Long,
                 @RequestParam(required = false)
                 @ApiParam(value="优惠券链接地址",required =false)
                 coupon_url:String
               )  ={

    val where = TraceOrder where DSL.column("item.commerceType") === CommerceType.JD and
      TraceOrder.userId === user.id limit 1
    val headOption = where.toList.headOption
    val pid = headOption match{
      case Some(tr) => tr.pid
      case _ =>
        val jdPid=jdService.createPidByUserId(user.id)
        jdPid
    }
    traceOrderService.savePid(pid,user,coupon_amount,item_id,CommerceType.JD)


    val url = "http://api.gofanli.cn/jingdong/doItemCpsUrl?"
//    val material="https://item.jd.com/"+item_id+".html"
    val material="https://wqitem.jd.com/item/view?sku="+item_id
    val parameters = Map(
      "apkey" -> MYQ_AP_KEY,
      "key_id"->MYQ_JD_CONNECT_KEY,
      "materialId"->material,
      "positionId"->pid,
      "couponUrl"->URLDecoder.decode(coupon_url,"UTF-8")
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
            ): MockHandankuResponse[Array[JdGoods.Goods]] ={

    val url="http://api.web.21ds.cn/jingdong/getJdUnionItems"
    val uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    uriComponentsBuilder.queryParam("apkey",MYQ_AP_KEY)
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
    uriComponentsBuilder.queryParam("pageIndex",min_id.toString)
    uriComponentsBuilder.queryParam("pageSize","30")


    val uri = uriComponentsBuilder.build().encode().toUri
    println("uri:",uri)
    val json = restTemplate.getForObject(uri,classOf[String])
    val response = objectMapper.readValue(json, new TypeReference[MiaoYouQuanResponse[JdGoods.GoodsList]](){} )
    if(response.code == 200){
      val haodankuResponse = new MockHandankuResponse[Array[JdGoods.Goods]]
      haodankuResponse.data = response.data.list
      haodankuResponse.min_id = (min_id+1).toString

      haodankuResponse

    }else {
      throw new RuntimeException(response.msg)
    }

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

  private[pages] def setObjectMapper(objectMapper:ObjectMapper): Unit ={
    this.objectMapper=objectMapper
  }
}
object JdController {
  def main(args: Array[String]): Unit = {
    val controller = new JdController
    val user = new User()
    user.id=123L
    controller.promotion(user,100,1234L,"http%3A%2F%2Fcoupon.m.jd.com%2Fcoupons%2Fshow.action%3Fkey%3D8a851116810a45468528a290fdff92ff%26roleId%3D28861074%26to%3Dmall.jd.com%2Findex-953594.html")
//    controller.categories()
//    val response = controller.search(null,11,1,1)
//    println(response.data.head.itemid)
//    val head = response.data.head
//
//    controller.detail(head.itemid.toString)
////    val promotionResponse = controller.promotion(head.itemid.toString,head.couponurl)
////    println(promotionResponse.data)


//    val client = new DefaultJdClient("https://router.jd.com/api","", "190562315b06c899931ed8d341aafc47", "0415c3ed82df420f8c700732e7ae0c1b")
//    client.


  }
}

object JdGoods{

  @JsonIgnoreProperties(ignoreUnknown = true)
  class CommissionInfo{
    var commission:String=_
    var commissionShare:String = _
  }
  class CouponInfo{
    var couponList:Array[CouponInfoData] =_
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
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
    var imageList:Array[Map[String,String]] = _
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
  class PriceInfo{
    var price:String = _
  }
  class ShopInfo{
    var shopName:String = _
    var shopId:String = _
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
  class Goods{
    @JsonProperty
    @JsonAlias(Array("skuName"))
    var itemtitle:String = _  //goods_name
    @JsonProperty
    @JsonAlias(Array("discount"))
    var couponmoney:String = _ //coupon_discount
    @JsonProperty("couponInfo")
    def unpackCoupon(couponInfo: CouponInfo): Unit = {
      if(couponInfo.couponList != null){
        val bestCouponOpt= couponInfo.couponList.find(_.isBest == "1")
        val coupon=bestCouponOpt match{
          case Some(c) =>  c
          case _=>couponInfo.couponList.head
        }

        this.couponmoney = coupon.discount
        this.start_time = coupon.getStartTime.toLong
        this.end_time= coupon.getEndTime.toLong
        this.couponurl=coupon.link

        if(this.itemendprice == null && this.itemprice != null){
          this.itemendprice = (this.itemprice.toDouble -this.couponmoney.toDouble).toString
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
    def unpackPrice(priceInfo: PriceInfo): Unit ={
      this.itemprice=priceInfo.price
      if(this.itemendprice == null && this.couponmoney != null){
        this.itemendprice = (this.itemprice.toDouble -this.couponmoney.toDouble).toString
      }
    }
    @JsonProperty
    @JsonAlias(Array("shopname"))
    var shopname:String = _  // mall_name
    @JsonProperty("shopInfo")
    def unpackShop(shopInfo: ShopInfo): Unit ={
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

    @JsonProperty("imageInfo")
    def unpackImage(imageInfo: ImageInfo): Unit ={
      this.itempic = imageInfo.imageList.head("url")
//      println(imageInfo.imageList)
    }
    @JsonProperty
    @JsonAlias(Array("skuId"))
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
    var tkmoney:Int = _
    @JsonProperty("commissionInfo")
    def setCommissionInfo(commissionInfo: CommissionInfo): Unit ={
      this.tkmoney = (commissionInfo.commission.toDouble * 100).intValue()
    }

    @JsonProperty("taobao_image")
    def taobao_image:Array[String] = taobao_image_str.split(",")  // goods_gallery_urls
    @JsonIgnore
    private var taobao_image_str=""
    @JsonIgnore
    private var commissionStr:String = _
  }
  @JsonIgnoreProperties(ignoreUnknown = true)
  class GoodsList{
    var list:Array[Goods] = _
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
