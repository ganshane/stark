package reward.pages

import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import org.joda.time.DateTime
import org.springframework.security.access.annotation.Secured
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation._
import reward.RewardConstants
import reward.entities.Consumption
import collection.JavaConversions._

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
  @ApiOperation(value="消费",authorizations=Array(new Authorization(RewardConstants.ROLE_USER)))
  def consume(
             @ApiParam(value="消费金额",required = true)
             amount:Int,
             @ApiParam(value="商品ID",required = true)
             itemId:String,
             @ApiParam(value="商品图片",required = true)
             itemImg:String,
             @ApiParam(value="商品连接",required = true)
             itemLink:String): Unit ={
    val consumption = new Consumption
    consumption.amount = amount
    consumption.itemId = itemId
    consumption.itemImg = itemImg
    consumption.itemLink = itemLink

    consumption.createdAt = DateTime.now
    consumption.save
  }
  @GetMapping(Array("/list"))
  @ApiOperation(value="消费记录列表",authorizations=Array(new Authorization(RewardConstants.ROLE_USER)))
  def listConsumption():java.util.List[Consumption]={
    List.empty[Consumption]
  }
}
