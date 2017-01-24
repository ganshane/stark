package stark.activerecord

import java.util.Properties
import javax.persistence.{EntityManager, EntityManagerFactory}
import javax.sql.DataSource

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.orm.jpa.support.SharedEntityManagerBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.orm.jpa.{JpaTransactionManager, JpaVendorAdapter, LocalContainerEntityManagerFactoryBean}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import stark.activerecord.config.ActiveRecordConfigSupport
import stark.activerecord.internal.EntityServiceImpl
import stark.activerecord.services.{ActiveRecord, EntityService}

/**
  * 实现了简单方便的ORM模块
  * 利用了Spring-orm和JPA相关东西
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-02
 */
@Configuration
@EnableTransactionManagement
class StarkActiveRecordModule(beanFactory: AutowireCapableBeanFactory) {
  ActiveRecord.objectLocator = beanFactory
 // private var springBeanFactoryOpt:Option[LocalContainerEntityManagerFactoryBean] =  None
 @Bean
 def entityService(): EntityService ={
   beanFactory.createBean(classOf[EntityServiceImpl])
 }
  @Bean
  def buildEntityManagerFactory(dataSource: DataSource,
                                activeRecordConfig:ActiveRecordConfigSupport):LocalContainerEntityManagerFactoryBean = {
    val entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean

    entityManagerFactoryBean.setDataSource(dataSource)


    val jpaIt = activeRecordConfig.jpaProperties.iterator()
    while(jpaIt.hasNext){
      val jpaProperty = jpaIt.next()
      if(jpaProperty.name == StarkActiveRecordConstants.PACKAGE_SCAN_KEY){
        val packages = jpaProperty.value.split(",")
        entityManagerFactoryBean.setPackagesToScan(packages:_*)
      }
    }

    val properties = new Properties
    val it = activeRecordConfig.jpaProperties.iterator()
    while(it.hasNext){
      val jpaProperty = it.next()
      if(jpaProperty.name != StarkActiveRecordConstants.PACKAGE_SCAN_KEY) {
        properties.put(jpaProperty.name, jpaProperty.value)
      }
    }
    entityManagerFactoryBean.setJpaProperties(properties)

    val vendorClassName = properties.getProperty(StarkActiveRecordConstants.JPA_VENDOR_KEY)
    if( vendorClassName != null)
      entityManagerFactoryBean.setJpaVendorAdapter(Thread.currentThread().getContextClassLoader.loadClass(vendorClassName).newInstance().asInstanceOf[JpaVendorAdapter])
    else
      entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter)

    entityManagerFactoryBean
  }

  //@Scope(ScopeConstants.PERTHREAD)
  @Bean
  def buildEntityManager(entityManagerFactory: EntityManagerFactory):SharedEntityManagerBean={
    val shared = new SharedEntityManagerBean()
    shared.setEntityManagerFactory(entityManagerFactory)
    shared.setEntityManagerInterface(classOf[EntityManager])

    shared

  }
  @Bean
  def buildJpaTransactionManager(entityManagerFactory:EntityManagerFactory,
                                 dataSource: DataSource
                                 ):PlatformTransactionManager={
    val transactionManager = new JpaTransactionManager()
    //保证全局事务使用的key都是自身申明的对象
    transactionManager.setEntityManagerFactory(entityManagerFactory)
    //设置JPA厂商
//    springBeanFactoryOpt.foreach(x=>transactionManager.setJpaDialect(x.getJpaDialect))

    transactionManager
  }

  /*
   def buildTransactionInterceptor(@Local transactionManager: PlatformTransactionManager): TransactionInterceptor = {
    val transactionAttributeSource = new AnnotationTransactionAttributeSource
    val transactionInterceptor = new TransactionInterceptor(transactionManager, transactionAttributeSource)
    transactionInterceptor.afterPropertiesSet()
    transactionInterceptor
  }

  @Match(Array("*"))
  def adviseTransactional(receiver: MethodAdviceReceiver, @Local transactionInterceptor: TransactionInterceptor) {
    for (m <- receiver.getInterface.getMethods) {
      if (receiver.getMethodAnnotation(m, classOf[Transactional]) != null)
        receiver.adviseMethod(m, new EntityManagerTransactionAdvice(transactionInterceptor, m))
    }
  };
  */
}
