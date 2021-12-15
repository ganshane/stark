// Copyright 2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.utils.services

import scala.util.control.NonFatal


/**
 * run some function in no exception thrown
 */
trait RunInNoExceptionThrown {
  this: LoggerSupport =>
  def runInNotExceptionThrown(fun: => Unit) :Unit={
    try {
      fun
    } catch {
      case NonFatal(e) => error(e.getMessage, e)
    }
  }
}
