package reward.services

import com.fasterxml.jackson.annotation.JsonProperty
import com.pdd.pop.sdk.http.PopHttpClient

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
trait PddService {
//  def createOrUpdateOrder(order: PddDdkOrderListIncrementGetResponse.OrderListGetResponseOrderListItem): Unit

  def queryAllPids(): java.util.Queue[String]
  def createPidByUserId(userId:Long):String
  def getClient():PopHttpClient
}
object  PddService{
  class CustomParameter{
    @JsonProperty
    var uid:String = _
    @JsonProperty
    var showId:String = _
    var sid:String = _
  }
}
