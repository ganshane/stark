package reward

/**
  * constants for reward
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
object RewardConstants {
  final val SERVER_HOME = "server.home"
  final val SERVER_HOME_KEY = "${server.home}"
  final val SERVER_HOME_DEFAULT = "support"
  /**配置文件所在目录*/
  final val CONFIG_DIR = "config.dir"
  /** js web token **/
  final val TOKEN_HTTP_HEADER= "Authorization"
  final val TOKEN_PREFIX: String = "Bearer "

  final val GLOBAL_AUTH="Authorization"
  /** role **/
  final val ROLE_USER="ROLE_USER"
  final val ROLE_ADMIN="ROLE_ADMIN"

  final val TAOBAO_DATETIME_FORMATE="yyyy-MM-dd HH:mm:ss"
  //https://open.taobao.com/api.htm?spm=a2e0r.13193907.0.0.233424adiQRoB7&docId=43328&docType=2
  //see tk_status
  final val TK_PAID_STATUS =3
  final val ORDER_PAID_STATUS =12
  final val ORDER_CLOSED_STATUS=13

  final val COMMISSION_CONFIG_KEY="commission_config"
}
