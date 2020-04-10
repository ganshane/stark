package reward.pages

import io.swagger.annotations._
import javax.validation.constraints.Size
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities.{UserOrder, _}
import reward.services.{ActiveRecordPageableSupport, TaobaoService, UserService}
import springfox.documentation.annotations.ApiIgnore
import stark.activerecord.services.Condition
import stark.activerecord.services.DSL._

import scala.collection.JavaConversions._

/**
  * 用户相关的控制器
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@RestController
@RequestMapping(Array("/user"))
@Api(value="用户相关接口",description="用户相关接口")
@Validated
class UserController extends ActiveRecordPageableSupport{
  @Autowired
  private val taobaoService:TaobaoService = null

  @Autowired
  private var userService:UserService = _
  @PostMapping(Array("/withdraw"))
  @ApiOperation(value="提现",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  def withdraw(
             @ApiParam(name="user_order_id",value="用户订单ID",required=true)
             @RequestParam(name="user_order_id",required = true)
             userOrderId:Long,
             @AuthenticationPrincipal user:User
              ): UserWithdraw ={
    userService.withdraw(userOrderId,user)
  }

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
  @GetMapping(Array("/orders"))
  @ApiOperation(value="得到自己以及下属客户订单",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def orders(
              @ApiParam(name="status",allowMultiple=true,value="提现状态",required=false)
              @RequestParam(name="status",required = false)
              status:java.util.List[Integer],
              @AuthenticationPrincipal user:User,
              @ApiIgnore pageable: Pageable): List[TaobaoPublisherOrder]={
    val uos = UserOrder where UserOrder.userId === user.id
    if(status != null&&status.size > 0) {
      uos.and({
        var condition:Condition  = null
        status.foreach(s => {
          if(condition == null)
            condition = UserOrder.withdrawStatus === WithdrawResult(s)
          else
            condition = condition.or(UserOrder.withdrawStatus === WithdrawResult(s))
        })
        condition
      })
    }

    pageActiveRecordsByPageable(uos,pageable).map(uo=>TaobaoPublisherOrder.find(uo.tradeId).setUserOrder(uo))
  }
  @GetMapping(Array("/son"))
  @ApiOperation(value="得到儿子级",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  def son(@AuthenticationPrincipal user:User): List[User]={
    //等级为1
    val urs = select[UserRelation] where UserRelation.parentId === user.id and UserRelation.level === 1
    urs.map(ur=>User.find(ur.userId)).toList
  }
  @GetMapping(Array("/grandson"))
  @ApiOperation(value="得到孙子级",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @Secured(Array(RewardConstants.ROLE_USER))
  def grandson(@AuthenticationPrincipal user:User): List[User]={
    //等级为1
    val urs = select[UserRelation] where UserRelation.parentId === user.id and UserRelation.level === 2
    urs.map(ur=>User.find(ur.userId)).toList
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
  def info(@AuthenticationPrincipal user:User): User={
    UserStatistic.findOption(user.id).foreach(s=>{
      user.balance= s
      user.statistic = s
    })
    user
  }
  @GetMapping(Array("/aliyun/oss"))
//  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="得到操作阿里云的临时token")//,authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def getOssAccess={
    taobaoService.getJsClientAccessInfo()
//    taobaoService.getOssAccessInfo()
  }
  @PostMapping(Array("/receiving_qr"))
  @Secured(Array(RewardConstants.ROLE_USER))
  @ApiOperation(value="更新用户收款码信息",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def addReceivingQR(
                @RequestParam(value="收款码",required=true)
                @ApiParam(value="收款码",required=true)
                url:String,
                @AuthenticationPrincipal user:User): User={

    user.receivingQR=url
    user.updatedAt = DateTime.now

    //更新用户信息
    user.save()
  }
  /*
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
  */

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
