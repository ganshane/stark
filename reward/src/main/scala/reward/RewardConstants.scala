package reward

/**
  * constants for reward
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
object RewardConstants {
  final val SERVER_HOME = "server.home"
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
}
