package reward.services

import com.jd.open.api.sdk.JdClient

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
trait JdService {
  def getClient(): JdClient
}
