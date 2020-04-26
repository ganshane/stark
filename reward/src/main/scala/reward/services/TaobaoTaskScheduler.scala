package reward.services

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-14
  */
trait TaobaoTaskScheduler {
  def sync(): Unit
  def syncTaobaoOrder(): Unit
}
