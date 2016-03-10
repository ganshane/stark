package stark.activerecord.services

import javax.persistence.EntityManager
import javax.persistence.criteria.{Predicate, CriteriaBuilder, CriteriaQuery, Root}

import stark.activerecord.services.DSL.QueryContext

import scala.collection.{GenTraversableOnce, JavaConversions}
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag
import scala.reflect.classTag


/**
 * ActiveRecord DSL
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object DSL {
  private[activerecord] val dslContext = new scala.util.DynamicVariable[QueryContext](null)
  case class QueryContext(builder:CriteriaBuilder,query:CriteriaQuery[_],root:Root[_])

  def from[T:ClassTag]:FromStep[T]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.getService[EntityManager].getCriteriaBuilder
    lazy val query  = queryBuilder.createQuery(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new FromStep[T](clazz)
  }
  def column[T : ClassTag](name:String):Field[T]={
    new JPAField[T](name)
  }
}
class ConditionBuilder[R](implicit val context: QueryContext) extends Fetch[R] with Limit with ConditionsGetter with OrderBy{
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
class FromStep[T](clazz:Class[T])(implicit val context: QueryContext) extends Fetch[T] with Limit with ConditionsGetter with OrderBy{
  def where:ConditionBuilder[T]=new ConditionBuilder[T]
  override private[activerecord] def getConditions: List[Predicate] = Nil
}
private[activerecord] trait OrderBy {
  val context:QueryContext
  def asc(field: Field[_]): this.type ={
    context.query.orderBy(context.builder.asc(context.root.get(field.fieldName)))
    this
  }
  def desc(field: Field[_]): this.type ={
    context.query.orderBy(context.builder.desc(context.root.get(field.fieldName)))
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
abstract class Fetch[A](implicit context:QueryContext) {
  this:Limit with ConditionsGetter =>
  private lazy val executeQuery:Stream[A]= fetchAsStream
  private def fetchAsStream: Stream[A]={
    val entityManager = ActiveRecord.getService[EntityManager]
    context.query.where(getConditions:_ *)
    val query = entityManager.createQuery(context.query)
    if(limitNum >0 )
      query.setMaxResults(limitNum)
    if(offsetNum > 0)
      query.setFirstResult(offsetNum)

    JavaConversions.asScalaBuffer(query.getResultList).toStream.asInstanceOf[Stream[A]]
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
