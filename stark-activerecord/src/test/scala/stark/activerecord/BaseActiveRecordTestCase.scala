package stark.activerecord

import java.io.File
import javax.persistence._
import javax.sql.DataSource

import org.apache.tapestry5.ioc.{Configuration, Registry, RegistryBuilder}
import org.junit.{After, Before}
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.{EntityManagerHolder, EntityManagerFactoryUtils}
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.FileSystemUtils
import stark.activerecord.config.{JpaProperty, ActiveRecordConfigSupport}
import stark.activerecord.services.{ActiveRecordInstance, ActiveRecord}

import scala.reflect.{ClassTag, classTag}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
class BaseActiveRecordTestCase {

  private var registry:Registry = _
  protected def getService[T:ClassTag]:T={
    registry.getService(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
  @Before
  def setup: Unit ={
    val modules = Seq[String](
      "stark.activerecord.StarkActiveRecordModule",
      "stark.activerecord.TestDataModule").map(Class.forName)
    registry = RegistryBuilder.buildAndStartupRegistry(modules: _*)
    //OpenSession In Thread
    val entityManagerFactory= getService[EntityManagerFactory]
    val emHolder= new EntityManagerHolder(entityManagerFactory.createEntityManager())
    TransactionSynchronizationManager.bindResource(entityManagerFactory, emHolder)
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
}
object TestDataModule{
  def buildDataSource: DataSource ={
    val dbPath = "target/test.db"
    FileSystemUtils.deleteRecursively(new File(dbPath))
    val dataSource = new DriverManagerDataSource("jdbc:h2:file:"+dbPath+"/xx","sa",null)
    dataSource
  }
  def contributeEntityManagerFactory(configuration:Configuration[String]): Unit ={
    configuration.add("stark.activerecord")
  }
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

    support
  }
}