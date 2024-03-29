// Copyright 2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.utils.services

import java.io.{PrintStream, PrintWriter}


/**
 * Monad整个系统异常消息
 * 异常消息编号有:
 * monad-support             1000
 * monad-jni                 4000
 *
 * @author jcai
 */
object StarkException {
  def wrap(exception: Throwable): StarkException = wrap(exception, null)

  def wrap(exception: Throwable, errorCode: ErrorCode): StarkException = {
    wrap(exception, errorCode, exception.getMessage)
  }

  def wrap(exception: Throwable, errorCode: ErrorCode, message: String): StarkException = {
    exception match {
      case me: StarkException =>
        if (errorCode != null && errorCode != me.errorCode) {
          return new StarkException(message, exception, errorCode)
        }
        me
      case _ =>
        new StarkException(message, exception, errorCode)
    }
  }
}

class StarkException(message: String, cause: Throwable, val errorCode: ErrorCode) extends RuntimeException(message, cause) {
  def this(errorCode: ErrorCode) = this(null, null, errorCode)

  def this(message: String, errorCode: ErrorCode) = this(message, null, errorCode)

  def this(cause: Throwable, errorCode: ErrorCode) = this(cause.getMessage, cause, errorCode)

  override def printStackTrace(s: PrintStream) :Unit={
    s.synchronized {
      printStackTrace(new PrintWriter(s))
    }
  }

  override def printStackTrace(s: PrintWriter) :Unit={
    s.synchronized {
      s.println(this)
      getStackTrace.foreach { trace =>
        s.println("\tat " + trace)
      }
      if (cause != null) {
        cause.printStackTrace(s)
      }
      s.flush()
    }
  }

  override def toString = {
    val sb = new StringBuilder
    if (errorCode != null) {
      sb.append("monad-").append(errorCode.code).append(":")
      sb.append(errorCode.getClass.getSimpleName).append(" ")
    } else {
      sb.append("monad-0000 UNKNOWN ")
    }
    if (message != null) {
      sb.append(message)
    }
    if (cause != null) {
      sb.append(" -> ").append(cause)
    }

    sb.toString()
  }

}

abstract class ErrorCode(val code: Int)

object UNKNOWN extends ErrorCode(2)
