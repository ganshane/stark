package stark.activerecord

import org.junit.runner.RunWith
import org.junit.{After, Before, Test}
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.{EntityManagerFactoryUtils, EntityManagerHolder}
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.FileSystemUtils
import stark.activerecord.config.{ActiveRecordConfigSupport, JpaProperty}
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

import java.io.File
import java.time.ZonedDateTime
import javax.persistence._
import javax.sql.DataSource
import scala.reflect.{ClassTag, classTag}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
@RunWith(classOf[SpringJUnit4ClassRunner])
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = Array(classOf[TestDataModule],classOf[StarkActiveRecordModule]))
class BaseActiveRecordTestCase {
  @Autowired
  private var beanFactory:BeanFactory = _

  protected def getService[T:ClassTag]:T={
    beanFactory.getBean(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
  @Before
  def setup: Unit ={
    val entityManagerFactory= getService[EntityManagerFactory]
    val emHolder= new EntityManagerHolder(entityManagerFactory.createEntityManager())
    TransactionSynchronizationManager.bindResource(entityManagerFactory, emHolder)
  }
  @After
  def down: Unit ={
    val emf: EntityManagerFactory = getService[EntityManagerFactory]
 val emHolder: EntityManagerHolder = TransactionSynchronizationManager.unbindResource(emf).asInstanceOf[EntityManagerHolder]
 EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager)

   // registry.shutdown()
  }
  @Test
  def test: Unit ={
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
  var zonedDateTime:ZonedDateTime = _
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

  @Column(name = "test_id",nullable = true)
  var testId:Integer = _

  @ManyToOne(fetch = FetchType.LAZY,optional = true)
  @JoinColumn(name = "test_id",nullable=true,insertable = false,updatable = false)
  var testTableJoin:ModelA = _

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
    support.jpaProperties.add(jpaProperty)

    support
  }
}