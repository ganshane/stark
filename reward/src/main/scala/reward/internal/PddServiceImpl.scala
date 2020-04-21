package reward.internal

import java.util
import java.util.concurrent.ConcurrentLinkedQueue

import com.pdd.pop.sdk.http.PopHttpClient
import com.pdd.pop.sdk.http.api.request.PddDdkGoodsPidQueryRequest
import org.springframework.stereotype.Service
import reward.services.PddService

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
  * 针对pdd的服务类
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
@Service
class PddServiceImpl extends PddService{
  private val clientId="869872c316ea408fb16b61b0fcdccc0b"
  private val clientSecret="7735676a0bcc5b42cf26d428b2ef1616041f90f5"
  private val pid = "9959558_132231707"
  private val client = new PopHttpClient(clientId, clientSecret)
  override def queryAllPids(): util.Queue[String]={
    val queue = new ConcurrentLinkedQueue[String]()
    loopGetAllPid().foreach(queue.offer)
    queue
  }

  override def getClient(): PopHttpClient = client

  @tailrec
  private def loopGetAllPid(page:Int=1,pageSize:Int=100,data:List[String]=List()): List[String]={
    val request = new PddDdkGoodsPidQueryRequest
    request.setPage(page)
    request.setPageSize(pageSize)
    val response = client.syncInvoke(request)
    if(response.getErrorResponse == null) {
      val pidResponse = response.getPIdQueryResponse
      val list = pidResponse.getPIdList.toList.map(_.getPId)
      if(list.size < pageSize){
        data ::: list
      }else{
        loopGetAllPid(page+1,pageSize,data ::: list )
      }
    }else{
      throw new RuntimeException("query pid excepiton,msg:"+response.getErrorResponse.getErrorMsg)
    }
  }
}
