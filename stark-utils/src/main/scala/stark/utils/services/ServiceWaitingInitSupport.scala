// Copyright 2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.utils.services

import java.util.concurrent.{CountDownLatch, TimeUnit}

/**
 * 等待服务完成
 */
trait ServiceWaitingInitSupport {
  private val latch = new CountDownLatch(1)

  /**
   * 等待服务初始化
 *
   * @param time 等待时间
   * @param unit 时间单元
   * @return 是否初始化完成
   */
  protected def awaitServiceInit(time: Long, unit: TimeUnit): Boolean = {
    latch.await(time, unit)
  }

  /**
   * 等待1分钟，服务初始化
   */
  protected def awaitServiceInit() :Unit={
    if (!latch.await(1, TimeUnit.MINUTES)) {
      throw new StarkException("timeout to wating service init", StarkUtilsErrorCode.WAITING_SERVER_INIT_TIMEOUT)
    }
  }

  /**
   * 判断服务是否已经初始化
   */
  protected def throwExceptionIfServiceInitialized() = {
    if (latch.getCount == 0) {
      throw new StarkException("service already initialized", StarkUtilsErrorCode.SERVICE_HAS_INITIALIZED)
    }
  }

  /**
   * 标记服务已经初始化完毕
   */
  protected def serviceInitialized() :Unit={
    latch.countDown()
  }
}
