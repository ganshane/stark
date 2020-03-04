package reward.pages

import io.swagger.annotations.{Api, ApiOperation, ApiParam}
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RequestParam, RestController}

/**
  * 用户相关的控制器
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@RestController
@RequestMapping(Array("/user"))
@Api("用户相关接口")
class UserController {


  @GetMapping(Array("/sayHello"))
  @ApiOperation("say hello")
    def sayHello(@ApiParam(value="姓名",required = true)
                 @RequestParam(value = "name", defaultValue = "World") name: String)={
    name+"  sb"
  }
}
