package reward.pages

import io.swagger.annotations._
import javax.validation.constraints.Size
import org.joda.time.DateTime
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.User
import springfox.documentation.annotations.ApiIgnore

/**
  * 用户相关的控制器
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@RestController
@RequestMapping(Array("/user"))
@Api(value="用户相关接口",description="用户相关接口")
@Validated
class UserController {
  @PostMapping(Array("/sendSms"))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="发送短信验证码",authorizations=Array(new Authorization("Authorization")))
  def sendSmsCode(
                   @ApiParam(value="电话号码",required=true)
                   @RequestParam("phone") @Size(min=11,max=11)
                   phone:String,
                   @ApiIgnore @AuthenticationPrincipal
                   pricipal:BearerTokenAuthentication
                 )={
    println("--------->",pricipal.getToken.getTokenValue)
    ResponseEntity.ok().build()
  }
  @GetMapping(Array("/sayHello"))
  @ApiOperation("say hello")
  def sayHello(
                @ApiParam(value="姓名",required = true)
                @RequestParam(value = "name", defaultValue = "World") name: String
              ):User={
    if(2>1) throw new Exception("asdf")
    val user = new User
    user.name="jcai"
    user.phone="13311336277"
    user.createdAt=DateTime.now()

    user.save

  }
}
