package stark.activerecord.services

import javax.persistence.EntityManager
import javax.persistence.criteria.{Predicate, CriteriaBuilder, CriteriaQuery, Root}

import stark.activerecord.services.DSL.QueryContext

import scala.collection.JavaConversions


/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object DSL {
  private[activerecord] val dslContext = new scala.util.DynamicVariable[QueryContext](null)
  case class QueryContext(builder:CriteriaBuilder,query:CriteriaQuery[_],root:Root[_])

  def from[R,T](clazz:Class[T]):FromStep[R,T]={
    lazy val queryBuilder = ActiveRecord.getService[EntityManager].getCriteriaBuilder
    lazy val query  = queryBuilder.createQuery(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new FromStep[R,T](clazz)
  }
  def column(name:String):Field={
    new JPAField(name)
  }
}
class ConditionBuilder(implicit context: QueryContext) extends Fetch with Limit{
  private var conditions=List[Predicate]()
  def apply(fun: =>Condition):ConditionBuilder={
    and(fun)
    this
  }
  def or(fun: =>Condition):ConditionBuilder={
    DSL.dslContext.withValue(context){
      val condition = DSL.dslContext.value.builder.or(fun)
      conditions = condition :: conditions
    }
    this
  }
  def and(fun: =>Condition):ConditionBuilder={
    DSL.dslContext.withValue(context){
      val condition = context.builder.and(fun)
      conditions = condition :: conditions
    }
    this
  }
  def fetch=fetchAsStream(context,conditions.reverse)
}
class FromStep[R,T](clazz:Class[T])(implicit context: QueryContext){
  private lazy val conditionBuilder:ConditionBuilder = new ConditionBuilder
  def where:ConditionBuilder={
    conditionBuilder
  }
}
trait Limit{
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
trait Fetch {
  this:Limit=>
  def fetchAsStream[R](context:QueryContext,conditions:List[Predicate]): Stream[R]={
    val entityManager = ActiveRecord.getService[EntityManager]
    context.query.where(conditions :_ *)
    val query = entityManager.createQuery(context.query)
    if(limitNum >0 )
      query.setMaxResults(limitNum)
    if(offsetNum > 0)
      query.setFirstResult(offsetNum)
    JavaConversions.asScalaBuffer(query.getResultList).toStream.asInstanceOf[Stream[R]]
  }
}
