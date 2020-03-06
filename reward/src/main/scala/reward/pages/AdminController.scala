package reward.pages

import io.swagger.annotations._
import org.springframework.data.domain.Pageable
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{PostMapping, RequestMapping, RestController}
import reward.RewardConstants
import reward.entities.User
import reward.services.RewardUtils
import springfox.documentation.annotations.ApiIgnore

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
class AdminController {
  @PostMapping(Array("/users"))
  @ApiOperation(value="获取所有用户列表",authorizations = Array(new Authorization(RewardConstants.GLOBAL_AUTH)))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
      value = "抓取的页数(0..N)"),
    new ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
      value = "每页多少条记录."),
    new ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
      value = "对查询进行排序，格式为: property(,asc|desc).支持多种排序,传递多个sort参数")
  ))
  def listUser(@ApiIgnore pageable: Pageable):List[User]={
    RewardUtils.pageActiveRecordsByPageable(User.all,pageable)
  }
}
