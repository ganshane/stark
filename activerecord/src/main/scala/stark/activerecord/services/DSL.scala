package stark.activerecord.services

import javax.persistence.EntityManager
import javax.persistence.criteria._

import stark.activerecord.services.DSL.QueryContext

import scala.collection.{GenTraversableOnce, JavaConversions}
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag
import scala.reflect.classTag

import scala.reflect.runtime.universe._

/**
 * ActiveRecord DSL
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object DSL {
  private[activerecord] val dslContext = new scala.util.DynamicVariable[QueryContext](null)

  type DSLQuery={

  }

  case class QueryContext(builder:CriteriaBuilder,query:DSLQuery,root:Root[_])

  def from[T:ClassTag]: SelectStep[T]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.getService[EntityManager].getCriteriaBuilder
    lazy val query  = queryBuilder.createQuery(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new SelectStep[T](clazz)
  }
  def delete[T:ClassTag]: DeleteStep[T]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.getService[EntityManager].getCriteriaBuilder
    lazy val query  = queryBuilder.createCriteriaDelete(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new DeleteStep[T](clazz)
  }
  def column[T : TypeTag](name:String):Field[T]={
    new JPAField[T](name)
  }
}
class ConditionBuilder[R](implicit val context: QueryContext) extends ConditionsGetter {
  private var conditions=List[Predicate]()
  def apply(fun: =>Condition):this.type={
    and(fun)
    this
  }
  def or(fun: =>Condition):this.type={
    DSL.dslContext.withValue(context){
      val condition = DSL.dslContext.value.builder.or(fun)
      conditions = condition :: conditions
    }
    this
  }
  def and(fun: =>Condition):this.type={
    DSL.dslContext.withValue(context){
      val condition = context.builder.and(fun)
      conditions = condition :: conditions
    }
    this
  }

  override private[activerecord] def getConditions: List[Predicate] = conditions
}
class SelectStep[T](clazz:Class[T])(implicit val context: QueryContext) extends Fetch[T] with Limit with ConditionsGetter with OrderBy{
  def where=new ConditionBuilder[T] with Limit with Fetch[T] with OrderBy
  override private[activerecord] def getConditions: List[Predicate] = Nil
}
class UpdateStep[T](clazz:Class[T])(implicit val context: QueryContext) extends ConditionsGetter{
  def where=new ConditionBuilder[T] with Execute[T] with Limit
  override private[activerecord] def getConditions: List[Predicate] = Nil
}
class DeleteStep[T](clazz:Class[T])(implicit val context: QueryContext) extends ConditionsGetter{
  def where=new ConditionBuilder[T] with Execute[T] with Limit
  override private[activerecord] def getConditions: List[Predicate] = Nil
}
private[activerecord] trait OrderBy {
  val context:QueryContext
  def asc(field: Field[_]): this.type ={
    context.query.asInstanceOf[CriteriaQuery[_]].orderBy(context.builder.asc(context.root.get(field.fieldName)))
    this
  }
  def desc(field: Field[_]): this.type ={
    context.query.asInstanceOf[CriteriaQuery[_]].orderBy(context.builder.desc(context.root.get(field.fieldName)))
    this
  }
}
private[activerecord] trait Limit{
  private[services] var limitNum:Int = 0
  private[services] var offsetNum:Int = 0
  def limit(limit:Int):this.type={
    this.limitNum = limit
    this
  }
  def offset(offset:Int):this.type={
    this.offsetNum = offset
    this
  }
}
private[activerecord]trait ConditionsGetter{
  private[activerecord] def getConditions:List[Predicate]
}
trait Execute[A]{
  this:Limit with ConditionsGetter =>
  val context:QueryContext
  def execute: Int ={
    val entityManager = ActiveRecord.getService[EntityManager]
    val query = context.query match{
      case q:CriteriaUpdate[A] =>
        val criteriaUpdate = context.query.asInstanceOf[CriteriaUpdate[A]]
        criteriaUpdate.where(getConditions:_ *)
        entityManager.createQuery(criteriaUpdate)
      case q:CriteriaDelete[A] =>
        val criteriaDelete = context.query.asInstanceOf[CriteriaDelete[A]]
        criteriaDelete.where(getConditions:_ *)
        entityManager.createQuery(criteriaDelete)

    }
    if(limitNum >0 )
      query.setMaxResults(limitNum)
    if(offsetNum > 0)
      query.setFirstResult(offsetNum)

    val entityService = ActiveRecord.getService[EntityService]
    entityService.execute(query)
  }
}
trait Fetch[A] {
  this:Limit with ConditionsGetter =>
  val context:QueryContext
  private lazy val executeQuery:Stream[A]= fetchAsStream
  private def fetchAsStream: Stream[A]={
    val entityManager = ActiveRecord.getService[EntityManager]
    val criteriaQuery = context.query.asInstanceOf[CriteriaQuery[A]]
    criteriaQuery.where(getConditions:_ *)
    val query = entityManager.createQuery(criteriaQuery)
    if(limitNum >0 )
      query.setMaxResults(limitNum)
    if(offsetNum > 0)
      query.setFirstResult(offsetNum)

    JavaConversions.asScalaBuffer(query.getResultList).toStream
  }

  @inline final def size = executeQuery.size
  @inline final def foreach[U](f: A => U) = executeQuery.foreach(f)
  @inline final def filter[U](f: A => Boolean) = executeQuery.filter(f)
  @inline final def head = executeQuery.head
  @inline final def headOption = executeQuery.headOption
  @inline final def tail = executeQuery.tail
  @inline final def map[B, That](f: A => B)(implicit bf: CanBuildFrom[Stream[A], B, That]): That =  executeQuery.map(f)
  @inline final def flatMap[B, That](f: A => GenTraversableOnce[B])(implicit bf: CanBuildFrom[Stream[A], B, That]): That = executeQuery.flatMap(f)
}
