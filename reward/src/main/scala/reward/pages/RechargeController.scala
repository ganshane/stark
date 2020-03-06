package reward.pages

import io.swagger.annotations._
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.{Recharge, User}
import reward.services.RewardUtils
import springfox.documentation.annotations.ApiIgnore
import collection.JavaConversions._

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
class RechargeController {

  @PostMapping
  @ApiOperation(value="充值",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def doRecharge(
                  @ApiParam(name="no",value="卡号",required = true)
                  @RequestParam
                  cardNo:String,
                  @RequestParam
                  @ApiParam(name="no",value="卡号",required = true)
                  cardSecret:String,
                  @ApiIgnore
                  @AuthenticationPrincipal user:User
                ): Recharge ={
    val cardOpt = Recharge.find_by_cardNo(cardNo).headOption
    cardOpt match{
      case Some(card) =>
        if(card.cardSecret != cardSecret) throw new IllegalArgumentException("错误的卡密")
        else if(card.ownerId != null) throw new IllegalStateException("卡已经被注册")
        else {card.ownerId=user.id;card.save()}
      case _ =>
        throw new UnsupportedOperationException("卡未找到")
    }
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
    RewardUtils.pageActiveRecordsByPageable(Recharge.all,pageable)
  }
}
