package reward.pages

import io.swagger.annotations.{Api, ApiOperation, Authorization}
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants

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
  @ApiOperation(value="充值",authorizations=Array(new Authorization("Authorization")))
  def doRecharge(){}
  @GetMapping(Array("/list"))
  @ApiOperation(value="充值列表",authorizations=Array(new Authorization("Authorization")))
  def listRecharge(){}
}
