package reward.pages

import io.swagger.annotations.{ApiModel, ApiModelProperty}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@ApiModel("API错误信息")
class ApiError(
                @ApiModelProperty(value="异常码",example = "999")
                val code:Int,
                @ApiModelProperty(value="异常消息",example = "错误消息")
                val message:String
              ) {
  @ApiModelProperty(value="开发者异常消息",example = "错误消息")
  var devMessage:Array[String] = _
}
