package reward.app

import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import reward.{RewardConstants, RewardModule}
import stark.utils.services.GlobalLoggerConfigurationSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
object RewardApp extends GlobalLoggerConfigurationSupport{
  def main(args: Array[String]): Unit = {
    val serverHome = System.getProperty(RewardConstants.SERVER_HOME, RewardConstants.SERVER_HOME_DEFAULT)
    System.setProperty(RewardConstants.SERVER_HOME, serverHome)
    val ctx: ApplicationContext = SpringApplication.run(
      Array[Class[_]](classOf[RewardModule]),args)
  }
}
