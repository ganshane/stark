// Copyright 2012,2013,2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.utils.services

import org.springframework.util.{PropertyPlaceholderHelper, SystemPropertyUtils}

/**
 * 提供解析表达式
  *
  * @author jcai
 */
object SymbolExpander {

  /**
   * 解析字符串
   */
  def expand(input: String, params: Map[String, String] = Map[String, String]()) = {
    val symbolSource = new SymbolSource(input,params)
    val helper = new PropertyPlaceholderHelper(SystemPropertyUtils.PLACEHOLDER_PREFIX,SystemPropertyUtils.PLACEHOLDER_SUFFIX)
    helper.replacePlaceholders(input,symbolSource)
  }

  def valueForSymbol(symbolName: String) = {
    val symbolSource = new SymbolSource(symbolName,Map[String,String]())
    symbolSource.resolvePlaceholder(symbolName)
  }

  private class SymbolSource(text:String,params:Map[String,String]) extends PropertyPlaceholderHelper.PlaceholderResolver {

    def resolvePlaceholder(placeholderName: String): String = {
        params.get(placeholderName) match {
          case Some(propVal) =>
            propVal
          case None =>
            sys.props.get(placeholderName) match{
              case Some(v)=> v
              case None =>
                sys.env.get(placeholderName) match{
                  case Some(v)=> v
                  case None =>
                    throw new RuntimeException("Could not resolve placeholder '" + placeholderName + "' in [" + this.text + "] as system property")
                }
            }
        }
    }
  }

}
