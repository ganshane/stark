// Copyright 2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.utils.services

/**
 * service lifecycle
 */
trait ServiceLifecycle {
  /**
   * 启动服务
   */
  def start():Unit

  /**
   * 服务关闭
   */
  def shutdown():Unit
}
