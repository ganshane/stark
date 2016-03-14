package stark.activerecord.services

import javax.persistence.{EntityManager, Id, Transient}

import org.apache.tapestry5.ioc.ObjectLocator
import org.slf4j.LoggerFactory
import stark.activerecord.macroinstruction.ActiveRecordMacroDefinition
import stark.activerecord.services.DSL.DSLSelectionQuery

import scala.collection.immutable.Stream
import scala.language.experimental.macros
import scala.language.{dynamics, postfixOps, reflectiveCalls}
import scala.reflect.{ClassTag, classTag}

/**
 * active record
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
object ActiveRecord {
  //logger
  private val logger = LoggerFactory getLogger getClass
  @volatile
  private[activerecord] var objectLocator:ObjectLocator= _
  private[activerecord] lazy val entityManager = getService[EntityManager]
  private[activerecord] lazy val entityService = getService[EntityService]

  /**
   * Saves the model.
   * If the model is new a record gets created in the database,otherwise
   * the existing record gets updated.
   */
  def save[T](record:T):T={
    entityService.save(record)
  }

  /**
   * Deletes the record in the database
   * The row is simply removed with an SQL +DELETE+ statement on the
   * record's primary key.
   */
  def delete[T](record:T): Unit ={
    entityService.delete(record)
  }
  def updateRelation[T](relation: DynamicUpdateSupport[T]):Int={
    entityService.updateRelation(relation)
  }
  def deleteRelation[T](relation: DynamicUpdateSupport[T]):Int={
    entityService.deleteRelation(relation)
  }

  /**
   * find some records by Relation
    *
    * @param relation relation object
   * @tparam T type parameter
   * @return record stream
   */
  private[services] def find[T](relation:Relation[T]):Stream[T]={
    entityService.find(relation)
  }

  /**
   * find some service using ObjectLocator
   */
  private def getService[T:ClassTag]:T={
    if(objectLocator == null)
      throw new IllegalStateException("object locator is null")
    objectLocator.getService(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
  /**
   * find_by and where function
    *
    * @param ql query
   * @param params parameter values
   * @return Relation object
   */
  def internalWhere[A](clazz:Class[A],primaryKey:String,ql:String)(params:Any*): QlRelation[A]={
    new QlRelation[A](clazz,primaryKey,ql,params.toSeq)
  }
  def createCriteriaRelation[A:ClassTag](clazz:Class[A],primaryKey:String,params:Condition*):DSLSelectionQuery[A,A]={
    val where = DSL.select[A].where
    params.foreach(p=>where.apply(p))

    where
  }
}

/**
 * ActiveRecord trait
 */
trait ActiveRecord {
  @Transient
  def save():this.type = {
    ActiveRecord.save(this)
  }
  @Transient
  def delete(): Unit ={
    ActiveRecord.delete(this)
  }
}

abstract class ActiveRecordInstance[A](implicit val clazzTag:ClassTag[A]) extends Dynamic{
  /**
   * get model class and primary key
   */

  val clazz = clazzTag.runtimeClass.asInstanceOf[Class[A]]
  protected val field = clazz.getDeclaredFields.find(_.isAnnotationPresent(classOf[Id]))
  val primaryKey = field.getOrElse(throw new IllegalStateException("primary key is null")).getName

  /**
   * where method
   * sucha as:
   *
   * find_by(name="jcai",code="1232")
    *
    * @param name method name
   * @param params method parameter
   * @return relation query object
   */
  def applyDynamicNamed(name:String)(params:(String,Any)*):DSLSelectionQuery[A,A]=macro ActiveRecordMacroDefinition.findByNamedParameterImpl[A,DSLSelectionQuery[A,A]]
  def selectDynamic[T](fieldName:String):Field[T] = macro ActiveRecordMacroDefinition.findField[A,Field[T]]
  /*{
    field match{
      case "find_by"=>
        ActiveRecord.createCriteriaRelation(clazz,primaryKey)
      case other=>
        throw new IllegalAccessException("unsupported!")
    }
  }
  */
  def update:UpdateStep[A]={
    DSL.update[A]
  }
  def delete:DeleteStep[A]={
    DSL.delete[A]
  }

  /**
   * find_by_xx_and_yy method
   * such as:
   *
   * ModelA.find_by_name_and_code("jcai","1232")
   *
   * @param name method name
   * @param params parameter list
   * @return Relation query instance
   */
  def applyDynamic(name:String)(params:Any*):DSLSelectionQuery[A,A]= macro ActiveRecordMacroDefinition.findByMethodImpl[A,DSLSelectionQuery[A,A]]

  /**
   * where(ql,parameters)
   * ModelA.where("name=?1 and code=?2","jcai","1232")
    *
    * @param ql query language
   * @param parameters parameters
   * @return Realtion Object
   */
  def where(ql:String,parameters:Any*): QlRelation[A]={
    ActiveRecord.internalWhere(clazz,primaryKey,ql)(parameters:_*)
  }


  /**
   * retrieving single object，must exist
    *
    * @param key primary key
   * @return entity object
   */
  def find(key:Any):A={
    internalFind(key).head
  }

  /**
   * retrieving single object，may not exist
    *
    * @param key primary key
   * @return entity object
   */
  def findOption(key:Any):Option[A]={
    internalFind(key).headOption
  }

  /**
   * find some records by primary key
    *
    * @param keys key array
   * @return query object
   */
  def find(keys:Array[Any]):QlRelation[A]={
    internalFind(keys)
  }
  private def internalFind(key:Any):QlRelation[A]={
    key match{
      case _:Int|_:String|_:Long=>
        new QlRelation[A](clazz,primaryKey,"%s=?1".format(primaryKey),Seq(key))
      case keys:Array[_] =>
        new QlRelation[A](clazz,primaryKey,"%s IN (?1)".format(primaryKey),Seq(key))
      case None =>
        new QlRelation[A](clazz,primaryKey,null,Seq())
    }
  }
  //retrieving single object
  def take:A= take(1).head
  def take(n:Int):QlRelation[A]={
    internalFind(None).limit(n)
  }
  def first:A= first(1).head
  def first(n:Int=1):QlRelation[A]= {
    take(n).order(primaryKey-> "ASC")
  }
  def last:A= last(1).head
  def last(n:Int):QlRelation[A]= {
    take(n).order(primaryKey->"DESC")
  }
  def all:QlRelation[A]= {
    internalFind(None)
  }
  def asc(fields:String*):QlRelation[A]={
    internalFind(None).asc(fields:_*)
  }
  def desc(fields:String*):QlRelation[A]={
    internalFind(None).desc(fields:_*)
  }

  /*
  def find_each(start:Int=0,batchSize:Int = 100)(f:A=>Unit): Unit ={

  }
  */
}
