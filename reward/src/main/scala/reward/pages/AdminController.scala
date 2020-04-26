package reward.pages

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations._
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.{HttpStatus, MediaType}
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import org.springframework.web.server.ResponseStatusException
import reward.RewardConstants
import reward.entities.UserWithdraw.WithdrawResult
import reward.entities._
import reward.services.{ActiveRecordPageableSupport, TaobaoService}
import springfox.documentation.annotations.ApiIgnore
import stark.activerecord.services.DSL.delete

import scala.collection.JavaConversions._
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
class AdminController(@Autowired taobaoService: TaobaoService) extends ActiveRecordPageableSupport{
  @Autowired
  private val objectMapper:ObjectMapper = null

  @PostMapping(value=Array("/config/add"),consumes = Array(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
  @ApiOperation(value="增加配置",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @throws(classOf[JsonParseException])
  def addConfig(
                 @ApiParam(value="配置的KEY",required = true) @RequestParam(required = true) key:String,
                 @ApiParam(value="配置的值",required = true) @RequestParam(required = true) value:String
              ):AppConfig={
    val jsonNode = objectMapper.readTree(value)
    val appConfig = new AppConfig
    appConfig.key = key
    appConfig.value = jsonNode.toString
    appConfig.createdAt = DateTime.now()
    appConfig.save()
  }
  @PostMapping(Array("/config/delete"))
  @ApiOperation(value="删除配置信息",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def deleteAppConfig(
                       @ApiParam(value="配置的KEY",required = true) @RequestParam(required = true) key:String):Unit={
    delete[AppConfig] where AppConfig.key === key execute
  }
  @GetMapping(Array("/orders/detail"))
  @ApiOperation(value="通过user_order_id来获取订单详细信息",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def detail(
              @ApiParam(value="user_order_id",required = true,example = "1") @RequestParam(name="user_order_id") userOrderId:Long
              ): TaobaoPublisherOrder={
    UserOrder findOption userOrderId map(uo=>TaobaoPublisherOrder.find(uo.tradeOrder.tradeId).setUserOrder(uo)) getOrElse(throw new ResponseStatusException(HttpStatus.NOT_FOUND))
  }
  @GetMapping(Array("/orders"))
  @ApiOperation(value="得到订单",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def orders2(
              @ApiParam(name="status",allowMultiple=true,value="提现状态",required=false)
              @RequestParam(name="status",required = false)
              status:java.util.List[Integer],
              @ApiIgnore pageable: Pageable): List[UserOrder]={
    val uos = UserOrder where

    if(status != null&&status.size > 0) {
      status.foreach(s => {
        uos.or(UserOrder.withdrawStatus === WithdrawResult(s))
      })
    }

    pageActiveRecordsByPageable(uos,pageable).map(uo=>uo.initCommerceOrder)
  }
  @GetMapping(Array("/orders/tbk"))
  @ApiOperation(value="得到订单",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
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
              @ApiIgnore pageable: Pageable): List[TaobaoPublisherOrder]={
    val uos = UserOrder where

    if(status != null&&status.size > 0) {
      status.foreach(s => {
        uos.or(UserOrder.withdrawStatus === WithdrawResult(s))
      })
    }

    pageActiveRecordsByPageable(uos,pageable).map(uo=>TaobaoPublisherOrder.find(uo.tradeOrder.tradeId).setUserOrder(uo))
  }
  @GetMapping(Array("/withdraws"))
  @ApiOperation(value="得到用户的申请打款列表",authorizations=Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def withdraws(
                @ApiParam(name="status",allowMultiple=true,value="提现状态",required=false)
                @RequestParam(name="status",required = false)
                status:java.util.List[Integer],
                @ApiIgnore pageable: Pageable): List[UserWithdraw]={
    val uw = UserWithdraw.where
    if(status != null&&status.size > 0) {
      status.foreach(s => {
        uw.or(UserWithdraw.sendResult === WithdrawResult(s))
      })
    }
    pageActiveRecordsByPageable(uw,pageable)
  }
  @GetMapping(Array("/withdraw"))
  @ApiOperation(value="针对某条申请进行提现",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def doWithdraw(
                @ApiParam(name="id",value="ID",required = true,example = "1")
                @RequestParam id:Long):UserWithdraw ={
    val uw = UserWithdraw.find(id)
    uw.successTime = DateTime.now()
    uw.sendResult = UserWithdraw.WithdrawResult.SUCCESS
    uw.save()
  }
  @GetMapping(Array("/user/info"))
  @ApiOperation(value="得到用户信息",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def userInfo(
                @ApiParam(name="id",value="ID",required = true,example = "1")
                @RequestParam id:Long):User={
    User.find(id)
  }

  @PostMapping(Array("/slide/delete"))
  @ApiOperation(value="删除轮播信息",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def deleteSlide(
                      @ApiParam(value="ID",required = true,example = "1") @RequestParam id:Long
                    ):Unit={
    delete[Slide] where Slide.id === id execute
  }
  @PostMapping(Array("/slide/add"))
  @ApiOperation(value="增加轮播",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def addSlide(
                   @ApiParam(value="指向URL",required = false) @RequestParam url:String,
                   @ApiParam(value="轮播图片",required = true) @RequestParam img_url:String,
                   @ApiParam(value="状态",example = "1") @RequestParam status:Int
                 ):Slide={
    val slide= new Slide
    slide.imgUrl= img_url
    slide.url=url
    slide.status = status
    slide.createdAt = DateTime.now()
    slide.save
  }
  @PostMapping(Array("/slide/status"))
  @ApiOperation(value="消息列表",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def updateStatus(
                    @ApiParam(value="ID",required = true,example = "1") @RequestParam id:Long,
                    @ApiParam(value="状态",required = true,example = "1") @RequestParam status:Int ):Slide={
    val slideOpt = Slide.findOption(id)
    slideOpt match{
      case Some(slide) =>
        slide.status = status
        slide.save()
      case _ =>
        throw new IllegalArgumentException("slide not found by id"+id)
    }
  }
  @GetMapping(Array("/slide/list"))
  @ApiOperation(value="消息列表",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def listSlide():java.util.List[Slide]={
    Slide.all.toList
  }

  @PostMapping(Array("/announce/delete"))
  @ApiOperation(value="删除系统消息",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def deleteAnnounce(
                   @ApiParam(value="消息的ID",required = true,example = "1") @RequestParam id:Long
                 ):Unit={
    delete[Announce] where Announce.id === id execute
  }
  @PostMapping(Array("/announce/add"))
  @ApiOperation(value="增加系统消息",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def addAnnounce(
                   @ApiParam(value="消息内容",required = true) @RequestParam content:String,
                   @ApiParam(value="消息指向的URL") @RequestParam url:String
                   ):Announce={
    val announce = new Announce
    announce.content = content
    announce.url=url
    announce.createdAt = DateTime.now()
    announce.save
  }
  @GetMapping(Array("/announce/list"))
  @ApiOperation(value="消息列表",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  def listAnnounce():java.util.List[Announce]={
    Announce.all.toList
  }


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
  @GetMapping(Array("/card/list"))
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
  @PostMapping(Array("/card/add"))
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
