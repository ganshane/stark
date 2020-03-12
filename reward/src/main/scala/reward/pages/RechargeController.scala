package reward.pages

import io.swagger.annotations._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.{Recharge, User}
import reward.services.{ActiveRecordPageableSupport, UserService}
import springfox.documentation.annotations.ApiIgnore

import scala.collection.JavaConversions._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
@RestController
@RequestMapping(Array("/recharge"))
@Api(value="充值相关",description="充值API")
@Validated
@Secured(Array(RewardConstants.ROLE_USER))
class RechargeController extends ActiveRecordPageableSupport{
  @Autowired
  private val userService:UserService = null

  @PostMapping
  @ApiOperation(value="充值",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def doRecharge(
                  @RequestParam(name="no",required = true)
                  @ApiParam(value="卡号",required = true)
                  cardNo:String,
                  @RequestParam(name="secret",required = true)
                  @ApiParam(value="卡密",required = true)
                  cardSecret:String,
                  @ApiIgnore
                  @AuthenticationPrincipal user:User
                ): Recharge ={
    userService.recharge(cardNo,cardSecret,user)
  }
  @GetMapping(Array("/list"))
  @ApiOperation(value="充值列表",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def listRecharge(
                    @ApiIgnore
                    @AuthenticationPrincipal user:User,
                    @ApiIgnore pageable: Pageable
                  ): java.util.List[Recharge]={
    pageActiveRecordsByPageable(Recharge.find_by_ownerId(user.id),pageable)
  }
}
