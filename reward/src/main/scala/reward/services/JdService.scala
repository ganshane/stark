package reward.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.jd.open.api.sdk.JdClient

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
trait JdService {
//  def createOrUpdateOrder(order: OrderResp, sku: SkuInfo): Unit


  def getClient(): JdClient
  def queryAllPids(): java.util.Queue[String]
  def createPidByUserId(userId:Long):String
}
object JdService{
  class MiaoYouQuanResponse[T]{
    var code:Int = _
    var msg:String = _
    var data:T = _
  }

  class PromotionPosition{
    var total:Int = _
    var pageNo:Int = _
    var pageSize:Int = _
    var result:Array[Position] = _
  }
  class Position{
    var id:Long= _
    var siteId:Int = _
    var spaceName:String = _
    @JsonProperty("type")
    var positionType:Int = _
  }
  class PidData{
    var resultList:Map[String,String] = _
    var msg:String = _
  }
}
