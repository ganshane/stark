package stark.activerecord

import java.io.File
import javax.inject.Inject
import javax.persistence._
import javax.sql.DataSource

import org.apache.tapestry5.ioc.{Configuration, Registry}
import org.junit.runner.RunWith
import org.junit.{After, Before}
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.{EntityManagerFactoryUtils, EntityManagerHolder}
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.FileSystemUtils
import stark.activerecord.config.{ActiveRecordConfigSupport, JpaProperty}
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

import scala.reflect.{ClassTag, classTag}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[TestDataModule],classOf[StarkActiveRecordModule]))
class BaseActiveRecordTestCase {
  @Inject
  private var beanFactory:BeanFactory = _

  private var registry:Registry = _
  protected def getService[T:ClassTag]:T={
    beanFactory.getBean(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
  @Before
  def setup: Unit ={
    /*
    val entityManagerFactory= getService[EntityManagerFactory]
    val emHolder= new EntityManagerHolder(entityManagerFactory.createEntityManager())
    TransactionSynchronizationManager.bindResource(entityManagerFactory, emHolder)
    */
  }
  @After
  def down: Unit ={
    val emf: EntityManagerFactory = registry.getService(classOf[EntityManagerFactory])
    val emHolder: EntityManagerHolder = TransactionSynchronizationManager.unbindResource(emf).asInstanceOf[EntityManagerHolder]
    EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager)

    registry.shutdown()
  }
}
object ModelA extends ActiveRecordInstance[ModelA]{
}
@Entity
@Table(name = "model_a")
@javax.persistence.SequenceGenerator(name = "entity_a_seq", sequenceName = "entity_a_seq")
class ModelA extends ActiveRecord with BaseModeA{
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_a_seq")
  @Column(name = "id")
  var id:Int = _
  var seq:Int = _
  var l:Long = _
  @Lob
  @Column(length=100000)
  var clob:String = _
  @Lob
  @Column(length=100000)
  var blob:Array[Byte] = _
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CASE_OCCUR_DATE", length = 23)
  var date:java.util.Date = _
  var i:java.lang.Integer = _
}
@MappedSuperclass
trait BaseModeA{
  @Column(length=100)
  var name:String = _
}
object ModelB extends ActiveRecordInstance[ModelB]{
}
@Entity
@Table(name = "model_B")
@javax.persistence.SequenceGenerator(name = "entity_a_seq", sequenceName = "entity_a_seq")
class ModelB extends ActiveRecord{
  @Id
  @Column(name = "id")
  var id:Int = _
  var name:String = _

  @ManyToOne
  @JoinColumn(name="model_a_id")
  var modelA:ModelA = _
}
@annotation.Configuration
class TestDataModule{
  @Bean
  def buildDataSource: DataSource ={
    val dbPath = "target/test.db"
    FileSystemUtils.deleteRecursively(new File(dbPath))
    val dataSource = new DriverManagerDataSource("jdbc:h2:file:"+dbPath+"/xx","sa",null)
    dataSource
  }
  def contributeEntityManagerFactory(configuration:Configuration[String]): Unit ={
    configuration.add("stark.activerecord")
  }
  @Bean
  def buildHallOrmConfigSupport: ActiveRecordConfigSupport={
    val support = new ActiveRecordConfigSupport {}
    var jpaProperty = new JpaProperty
    jpaProperty.name = "hibernate.show_sql"
    jpaProperty.value="true"
    support.jpaProperties.add(jpaProperty)
    jpaProperty = new JpaProperty
    jpaProperty.name = "hibernate.hbm2ddl.auto"
    jpaProperty.value="create"
    support.jpaProperties.add(jpaProperty)

    jpaProperty = new JpaProperty
    jpaProperty.name = StarkActiveRecordConstants.PACKAGE_SCAN_KEY
    jpaProperty.value="stark.activerecord"

    support
  }
}