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
@RequestMapping(Array("/consumption"))
@Api(value="消费相关API",description="购物消费相关PI")
@Validated
@Secured(Array(RewardConstants.ROLE_USER))
class ConsumptionController {

  @PostMapping
  @ApiOperation(value="消费",authorizations=Array(new Authorization("Authorization")))
  def consume(){}
  @GetMapping(Array("/list"))
  @ApiOperation(value="消费记录列表",authorizations=Array(new Authorization("Authorization")))
  def listConsumption(){}
}
