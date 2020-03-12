package reward.pages

import io.swagger.annotations._
import org.joda.time.DateTime
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.{Recharge, User}
import reward.services.ActiveRecordPageableSupport
import springfox.documentation.annotations.ApiIgnore

import collection.JavaConversions._
/**
  * 管理相关接口
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
@RestController
@RequestMapping(Array("/admin"))
@Api(value="管理功能",description="管理功能",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
@Validated
@Secured(Array(RewardConstants.ROLE_ADMIN))
class AdminController extends ActiveRecordPageableSupport{
  @GetMapping(Array("/users"))
  @ApiOperation(value="获取所有用户列表",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def listUser(@ApiIgnore pageable: Pageable):java.util.List[User]={
    pageActiveRecordsByPageable(User.all,pageable)
  }
  @GetMapping(Array("/cards"))
  @ApiOperation(value="获取所有充值卡",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def listCards(@ApiIgnore pageable: Pageable):java.util.List[Recharge]={
    pageActiveRecordsByPageable(Recharge.all,pageable)
  }
  @PostMapping(Array("/cards"))
  @ApiOperation(value="增加卡",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def addCard(
               @ApiParam(value="卡号",required = true) @RequestParam no:String,
               @ApiParam(value="密码",required = true) @RequestParam secret:String,
               @ApiParam(value="金额,单位:分",required = true,example = "100000")
               @RequestParam(required = true) amount:java.lang.Integer,
               @ApiParam(value="卡初始赋予的用户",required = true,example= "1")
               @RequestParam(name="created_id",required = true) createdId:java.lang.Long
             ):Recharge={
    val recharge = new Recharge
    recharge.amount=amount
    recharge.cardNo = no
    recharge.cardSecret = secret
    recharge.createdId = createdId
    recharge.createdAt = DateTime.now
    recharge.save()
  }
}
