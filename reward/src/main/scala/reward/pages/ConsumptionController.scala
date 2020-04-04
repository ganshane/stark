package reward.pages

import io.swagger.annotations._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.{Consumption, TaobaoPublisherOrder, User}
import reward.services.{ActiveRecordPageableSupport, UserService}
import springfox.documentation.annotations.ApiIgnore

import scala.collection.JavaConversions._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
@RestController
@RequestMapping(Array("/consumption"))
@Api(value="消费相关API",description="购物消费相关PI")
@Validated
@Secured(Array(RewardConstants.ROLE_USER))
class ConsumptionController extends ActiveRecordPageableSupport{
  @Autowired
  private val userService:UserService = null

  /*
  @ApiOperation(value="消费",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @PostMapping(value=Array("/add"),consumes = Array(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
  def add(
             @ApiParam(value="消费金额",required = true,example = "10000")
             @RequestParam(required = true)
             amount:Int,
             @ApiParam(value="商品ID",required = true)
             @RequestParam(name="item_id",required = true)
             itemId:String,
             @ApiParam(value="商品图片",required = true)
             @RequestParam(name="item_img",required = true)
             itemImg:String,
             @ApiParam(value="商品连接",required = true)
             @RequestParam(name="item_link",required = true)
             itemLink:String,
             @AuthenticationPrincipal user:User): Consumption ={
    userService.consume(amount,itemId,itemImg,itemLink,user)
  }
  */
  @GetMapping(Array("/list"))
  @ApiOperation(value="消费记录列表",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def list(@ApiIgnore pageable: Pageable,@AuthenticationPrincipal user:User):java.util.List[Consumption]={
    val coll=Consumption.find_by_userId(user.id)
    coll.foreach(c=>{
      TaobaoPublisherOrder.findOption(c.tradeId).foreach(o=>c.order=o)
    })
    pageActiveRecordsByPageable(coll,pageable)
  }
}
