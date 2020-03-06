package reward.internal

import reward.RewardConstants
import stark.utils.StarkUtilsConstants
import stark.utils.services.SymbolExpander

import scala.io.Source
import scala.util.control.NonFatal

object StarkConfigFileUtils {

  /**
    * 读取配置文件内容
    * @param serverHome
    * @param configFile
    * @return
    */
  def readConfigFileContent(serverHome: String, configFile: String) = {
    var filePath = serverHome + "/config"
    try {
      filePath = SymbolExpander.valueForSymbol(RewardConstants.CONFIG_DIR)
    } catch {
      case NonFatal(e) => //ignore exception
    }
    filePath += "/" + configFile
    Source.fromFile(filePath, StarkUtilsConstants.UTF8_ENCODING).mkString
  }
}
