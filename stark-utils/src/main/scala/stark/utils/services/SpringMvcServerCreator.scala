// Copyright 2014,2015,2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.utils.services

import java.net.BindException
import java.util.concurrent._

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.{Connector, Server}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.springframework.core.io.ClassPathResource
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet

trait SpringMvcServerCreator{
  private var serverOpt: Option[Server] = None

  protected def startServer(config: WebServerConfig, pkg: String, classes: Class[_]*): Server = {
    try {
      val bind = StarkUtils.parseBind(config.bind)
      val context = new AnnotationConfigWebApplicationContext
      classes.foreach(c=>context.register(c))

      val server = SpringMvcServerCreator.createSpringWebapp(bind._1, bind._2, context)
      SpringMvcServerCreator.configServer(server, config)
      server.start()
      serverOpt = Some(server)
      server
    } catch {
      case e: BindException =>
        throw new StarkException(e.getMessage, StarkUtilsErrorCode.SERVER_FAIL_BIND)
    }
  }

  protected def join() :Unit={
    serverOpt.foreach(_.join())
  }

  protected def shutdownServer() :Unit={
    serverOpt.foreach(_.stop())
  }
}

/**
 * jetty servelt container
 */
object SpringMvcServerCreator {
  /**
    * create spring web application
    *
    * @param host listen host
    * @param port listen port
    * @param webContext  web application
    * @return server instance
    */
  def createSpringWebapp(host: String,
                         port: Int,
                         webContext: AnnotationConfigWebApplicationContext): Server = {
    val contextHandler = new ServletContextHandler()
    contextHandler.setErrorHandler(null)
    contextHandler.setContextPath("/")
    contextHandler.addServlet(new ServletHolder(new DispatcherServlet(webContext)), "/*")
    contextHandler.addEventListener(new ContextLoaderListener(webContext))
    contextHandler.setResourceBase(new ClassPathResource("webapp").getURI().toString())

    val server = new Server()
    //对connector进行配置
    val connector = new SelectChannelConnector
    connector.setHost(host)
    connector.setPort(port)


    server.setConnectors(Array[Connector](connector))


    server.setSendServerVersion(false)
    server.setHandler(contextHandler)

    server
  }

  def configServer(server: Server, webServerConfig: WebServerConfig) :Unit={
    /*
    val executorService = ThreadPoolCreator.newSaturatingThreadPool(
      webServerConfig.minConnCount,
      webServerConfig.maxConnCount,
      webServerConfig.waitingQueueSize,
      webServerConfig.keepAliveTimeInMinutes,
      TimeUnit.MINUTES,
      "monad-web",
      new RejectedExecutionHandler {
        override def rejectedExecution(r: Runnable, executor: ThreadPoolExecutor): Unit = {
          throw new RejectedExecutionException("reach max connection")
        }
      })
    val threadPool = new ExecutorThreadPool(executorService)
    */
    val threadPool = new QueuedThreadPool(new ArrayBlockingQueue[Runnable](6000))
    threadPool.setMinThreads(webServerConfig.minConnCount)
    threadPool.setMaxThreads(webServerConfig.maxConnCount)
    threadPool.setMaxIdleTimeMs(TimeUnit.MINUTES.toMillis(webServerConfig.keepAliveTimeInMinutes).asInstanceOf[Int])
    threadPool.setDaemon(true)

    server.setThreadPool(threadPool)

    val connector = server.getConnectors.head.asInstanceOf[SelectChannelConnector]
    //connector.setAcceptors(Math.max(1, (Runtime.getRuntime.availableProcessors + 3) / 4))

    if (webServerConfig.acceptor == 0)
      connector.setAcceptors(Math.min(4, Runtime.getRuntime.availableProcessors + 3) / 4)
    else
      connector.setAcceptors(webServerConfig.acceptor)
    connector.setAcceptQueueSize(webServerConfig.backlog)
    connector.setMaxIdleTime(webServerConfig.idleTimeSecs * 1000)
    connector.setRequestBufferSize(webServerConfig.requestBufferSizeKB * 1024)
    connector.setResponseBufferSize(webServerConfig.responseBufferSizeKB * 1024)


    //graceful shutdown
    server.setStopAtShutdown(true)
    server.setGracefulShutdown(5000)
  }

  class OverflowingSynchronousQueue[E](capacity: Int) extends LinkedBlockingQueue[E](capacity) {
    private val synchronousQueue = new SynchronousQueue[E]()

    // Create a new thread or wake an idled thread
    override def offer(e: E) = synchronousQueue.offer(e)

    // Add to queue
    def offerToOverflowingQueue(e: E) = super.offer(e)

    override def take(): E = {
      // Return tasks from queue, if any, without blocking
      val task = super.poll()
      if (task != null) task else synchronousQueue.take()
    }

    override def poll(timeout: Long, unit: TimeUnit): E = {
      // Return tasks from queue, if any, without blocking
      val task = super.poll()
      if (task != null) task else synchronousQueue.poll(timeout, unit)
    }
  }

}
