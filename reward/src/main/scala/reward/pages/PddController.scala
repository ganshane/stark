package reward.pages

import java.{lang, util}

import com.pdd.pop.sdk.common.util.JsonUtil
import com.pdd.pop.sdk.http.PopHttpClient
import com.pdd.pop.sdk.http.api.request.{PddDdkGoodsDetailRequest, PddDdkGoodsPromotionUrlGenerateRequest, PddDdkGoodsSearchRequest, PddGoodsOptGetRequest}
import com.pdd.pop.sdk.http.api.response.PddDdkGoodsPromotionUrlGenerateResponse
import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.util.StringUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}

import scala.collection.JavaConversions._
/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-25
  */
@RestController
@RequestMapping(Array("/pdd"))
@Api(value="拼多多",description="拼多多",tags=Array("pdd"))
@Validated
class PddController {
  private val clientId="869872c316ea408fb16b61b0fcdccc0b"
  private val clientSecret="7735676a0bcc5b42cf26d428b2ef1616041f90f5"
  private val pid = "9959558_132231707"
  private val client = new PopHttpClient(clientId, clientSecret)
  private val optMapping:Map[Int,Int]=Map(
    11  ->  1,
    1 -> 14,
    2 -> 743,
    9 -> 8587,
    4 -> 16,
    3 -> 1282,
    5 -> 20495,
    6 -> 20497,
    7 -> 20496,
    8 -> 11392,
    10 -> 11254,
    12 -> 3162,
    13 -> 18,
    15 -> 10301,
    16 -> 3175,
    17 -> 11263
  )
  def opts(): Unit ={
    val request = new PddGoodsOptGetRequest
    request.setParentOptId(0)
    val response = client.syncInvoke(request)
    System.out.println(JsonUtil.transferToJson(response))
  }

  @GetMapping(Array("/promotion"))
  @ApiOperation(value="多多进宝推广链接生成")
  def promotion(
              @RequestParam(defaultValue = "0",required = true)
              @ApiParam(value="商品ID",required = true)
              itemid:String,
              @RequestParam(defaultValue = "0")
              @ApiParam(value="搜索id",defaultValue = "0",example = "0")
              search_id:String
            )  ={
    val request = new PddDdkGoodsPromotionUrlGenerateRequest
    request.setPId(pid)
    val ids = new util.ArrayList[java.lang.Long]()
    ids.add(itemid.toLong)
    request.setGoodsIdList(ids)
    request.setSearchId(search_id)
    request.setGenerateWeApp(true)

    val response = client.syncInvoke(request).getGoodsPromotionUrlGenerateResponse
    val urls = response.getGoodsPromotionUrlList

    val haodankuResponse = new MockHandankuResponse[util.List[PddDdkGoodsPromotionUrlGenerateResponse.GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem]]
    haodankuResponse.code = 1
    haodankuResponse.data = urls



    haodankuResponse

  }

  @GetMapping(Array("/detail"))
  @ApiOperation(value="查询商品详情")
  def detail(
              @RequestParam(defaultValue = "0",required = true)
              @ApiParam(value="商品ID",defaultValue = "0",example = "0",required = true)
              itemid:String,
              @RequestParam(defaultValue = "0",required = false)
              @ApiParam(value="搜索id",defaultValue = "0",example = "0",required = false)
              searchId:String
            ): MockHandankuResponse[HaodankuGoods] ={
    val request = new PddDdkGoodsDetailRequest()

    val ids = new util.ArrayList[java.lang.Long]()
    ids.add(itemid.toLong)
    request.setGoodsIdList(ids)

    request.setSearchId(searchId)
    request.setPid(pid)

    val response = client.syncInvoke(request)
    val haodankuResponse = new MockHandankuResponse[HaodankuGoods]
    haodankuResponse.code = 1
    val detail = response.getGoodsDetailResponse.getGoodsDetails.head

    val data = new HaodankuGoods
    data.itemtitle = detail.getGoodsName
    data.couponmoney = String.valueOf(detail.getCouponDiscount/100)
    data.itemsale = detail.getSalesTip
    data.itemprice = String.valueOf(detail.getMinGroupPrice/100)
    data.shopname = detail.getMallName
    data.itemendprice = String.valueOf((detail.getMinGroupPrice - detail.getCouponDiscount)/100)
    data.itempic = detail.getGoodsImageUrl
    data.itemid = detail.getGoodsId

    data.start_time = detail.getCouponStartTime
    data.end_time = detail.getCouponEndTime
    data.itemdesc = detail.getGoodsDesc
    data.taobao_image = detail.getGoodsGalleryUrls.toArray(Array[String]())

    haodankuResponse.data = data

    haodankuResponse
  }
  @GetMapping(Array("/search"))
  @ApiOperation(value="搜索pdd")
  def search(
              @RequestParam(required = false)
              @ApiParam(value="关键词",required = false)
              keyword:String,
              @RequestParam(defaultValue = "0")
              @ApiParam(value="关键词",defaultValue = "0",example = "0")
              cid:Int,
              @RequestParam(defaultValue = "0")
              @ApiParam(value="分页使用",defaultValue = "0",example = "0")
              min_id:Long,
              @RequestParam(defaultValue = "0")
              @ApiParam(value="排序",defaultValue = "0",example = "0")
              sort_type:Int,
              @RequestParam(required = false)
              @ApiParam(value="活动类别",example = "[0]",allowMultiple = true,required = false)
              activity_tags:Array[java.lang.Integer]
            ): MockHandankuResponse[Array[HaodankuGoods]] ={
    val request = new PddDdkGoodsSearchRequest()
    if(!StringUtils.isEmpty(keyword)) request.setKeyword(keyword)
    request.setOptId(optMapping.getOrElse(cid,0).toLong)
    request.setListId(min_id.toString)
    request.setSortType(sort_type)
    if(activity_tags != null)
      request.setActivityTags(activity_tags.toList)

    val response = client.syncInvoke(request)
    val list = response.getGoodsSearchResponse.getGoodsList
    val haodankuResponse = new MockHandankuResponse[Array[HaodankuGoods]]
    haodankuResponse.min_id = response.getGoodsSearchResponse.getListId
    val data = list.map(g=>{
      val goods = new HaodankuGoods
      goods.itemtitle = g.getGoodsName
      goods.couponmoney = String.valueOf(g.getCouponDiscount/100)
      goods.itemsale = g.getSalesTip
      goods.itemprice = String.valueOf(g.getMinGroupPrice/100)
      goods.shopname = g.getMallName
      goods.shoptype = g.getMerchantType.toString
      goods.itemendprice = String.valueOf((g.getMinGroupPrice - g.getCouponDiscount)/100)
      goods.itempic = g.getGoodsImageUrl
      goods.itemid = g.getGoodsId
      goods.search_id = g.getSearchId

      goods
    }).toArray
    haodankuResponse.data = data
    haodankuResponse
  }

}
class MockHandankuResponse[T] {
  var code:Int = 1
  var min_id:String= "1"
  var data:T= _
}
class HaodankuGoods{
  var itemtitle:String = _  //goods_name
  var couponmoney:String = _ //coupon_discount
  var itemsale:String = _  //sales_tip
  var itemprice:String = _  //min_group_price
  var shopname:String = _  // mall_name
  var shoptype:String=_ // merchant_type
  var itemendprice:String = _  // 需要通过优惠券进行计算
  var itempic:String = _ //goods_image_url
  var itemid:lang.Long = _  // goods_id

  var start_time:Long = _ //coupon_start_time
  var end_time:Long = _ //coupon_end_time
  var itemdesc:String = _  // goods_desc
  var taobao_image:Array[String] = _  // goods_gallery_urls

  //增加字段
  var search_id:String = _ // 转链接必须
  var wechat_url:String = _
}
