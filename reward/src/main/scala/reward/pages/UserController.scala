package reward.pages

import io.swagger.annotations._
import javax.validation.constraints.Size
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.{OnlineUser, User}
import reward.services.UserService
import stark.activerecord.services.DSL._

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

  @Autowired
  private var userService:UserService = _

  @PostMapping(Array("/login"))
  @ApiOperation(value="登录")
  def login(
             @ApiParam(value="电话号码",required=true)
             @RequestParam("phone") @Size(min=11,max=11)
             phone:String,
             @ApiParam(value="验证码",required=true)
             @RequestParam("code")
             code:String): OnlineUser ={
    //通过电话找到用户
    lazy val user = {
      val headOpt = User.find_by_phone(phone).headOption
      headOpt match {
        case Some(u) => u
        case _ => //没找到则进行注册
          val user= new User
          user.phone = phone
          user.createdAt = DateTime.now
          user.save()
      }
    }

    //校验短信先
    userService.verifySmsCode(phone,code)

    val onlineUserOpt = OnlineUser.find_by_userId(user.id).headOption
    onlineUserOpt match{
      case Some(ou) =>
        ou.updatedAt= DateTime.now
        ou.expiredAt = DateTime.now.plusMinutes(30)
        ou.save()
      case _ =>
        val onlineUser = new OnlineUser
        onlineUser.token = userService.generateToken(user)
        onlineUser.userId = user.id
        onlineUser.createdAt = DateTime.now
        onlineUser.expiredAt = DateTime.now.plusMinutes(30)
        onlineUser.save()
    }
  }
  @PostMapping(Array("/logout"))
  @ApiOperation(value="登出",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  def logout(@AuthenticationPrincipal user:User): Unit ={
    delete[OnlineUser] where OnlineUser.userId === user.id execute
  }
  @GetMapping(Array("/info"))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="得到当前用户信息",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def info(@AuthenticationPrincipal user:User): User=user
  @PostMapping(Array("/info"))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="更新用户信息",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def updateInfo(
                  @ApiParam(value="电话号码",required=true)
                  user:User,
                  @AuthenticationPrincipal currentUser:User): User={
    //TODO 需要注意更新,此更新不正确
    user.id = currentUser.id
    user.isAdmin = currentUser.isAdmin //不能更新成为管理员
    user.createdAt = currentUser.createdAt
    user.updatedAt = DateTime.now

    //更新用户信息
    user.save()
  }

  @PostMapping(Array("/sendSms"))
  @ApiOperation(value="发送短信验证码")
  def sendSmsCode(
                   @ApiParam(value="电话号码",required=true)
                   @RequestParam("phone") @Size(min=11,max=11)
                   phone:String
                 ):Unit ={
    userService.sendSmsCode(phone)
  }
}
