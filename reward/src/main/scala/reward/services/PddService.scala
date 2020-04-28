package reward.services

import com.fasterxml.jackson.annotation.JsonIgnore
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
    var uid:String = _
    var sid:String = _
    @JsonIgnore
    def userId:Long = uid.toLong
  }
}
