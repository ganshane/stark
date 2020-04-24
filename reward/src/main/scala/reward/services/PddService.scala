package reward.services

import com.pdd.pop.sdk.http.PopHttpClient

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
trait PddService {
  def queryAllPids(): java.util.Queue[String]
  def createPidByUserId(userId:Long):String
  def getClient():PopHttpClient
}
