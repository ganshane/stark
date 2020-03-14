package reward.pages

import io.swagger.annotations.{Api, ApiOperation}
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}
import reward.entities.{Announce, Slide}

import scala.collection.JavaConversions._
import stark.activerecord.services.DSL._
/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
@RestController
@RequestMapping(Array("/public"))
@Api(value="公共相关接口",description="公共相关接口")
@Validated
class PublicController {
  @ApiOperation(value="消息列表")
  @GetMapping(Array("/announces"))
  def announces():java.util.List[Announce]={
    Announce.all.toList
  }
  @GetMapping(Array("/slides"))
  @ApiOperation(value="轮播图列表")
  def listSlide():java.util.List[Slide]={
    val coll = select[Slide] where Slide.status[Int] > 0
    coll.toList
  }
}
