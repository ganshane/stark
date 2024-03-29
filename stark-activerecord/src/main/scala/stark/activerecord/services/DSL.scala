package stark.activerecord.services

import stark.activerecord.macroinstruction.ActiveRecordMacroDefinition
import stark.activerecord.services.DSL.{DSLExecuteQuery, DSLSelectionQuery, JoinQueryContext, QueryContext, UpdateField}

import javax.persistence.criteria._
import scala.collection.immutable.ArraySeq.unsafeWrapArray
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.language.dynamics
import scala.language.experimental.macros
import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, classTag}

/**
 * ActiveRecord DSL
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object DSL {
  //DSL Context using DynamicVariable method
  private[activerecord] val dslContext = new scala.util.DynamicVariable[QueryContext](null)
  private[activerecord] val joinContext = new scala.util.DynamicVariable[JoinQueryContext](null)
  //Execute Query type
  type DSLExecuteQuery[T] = ConditionClause[T] with Execute[T] with LimitClause with UtilSupport
  //Selection Query
  type DSLSelectionQuery[T,R] = ConditionClause[T]  with LimitClause with Fetch[R] with OrderByClause with GroupByClause with UtilSupport
  type UpdateField = CriteriaUpdate[_]=>CriteriaUpdate[_]

  //Query Context
  private[activerecord] case class QueryContext(builder:CriteriaBuilder,var query:Any,root:Root[_])
  //Join Query Context
  private[activerecord] case class JoinQueryContext(joinRoot:Path[_],join:Join[_,_])

  /**
    *  select method
    *
    * @tparam T entity type
    * @return Selection step
    */
  def select[T:ClassTag]: SelectStep[T,T]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.entityManager.getCriteriaBuilder
    lazy val query  = queryBuilder.createQuery(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new SelectStep[T,T](clazz)
  }

  /**
    * selection field
    *
    * @param fields selection field
    * @tparam T entity type
    * @return selection step
    */
  def select[T:ClassTag](fields:SelectionField*): SelectStep[T,Array[Any]]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.entityManager.getCriteriaBuilder
    lazy val query  = queryBuilder.createQuery(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)
    new SelectStep[T,Array[Any]](clazz).apply(fields:_*)
  }

  /**
    * delete entity
    *
    * @tparam T entity type
    * @return delete step
    */
  def delete[T:ClassTag]: DeleteStep[T]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.entityManager.getCriteriaBuilder
    lazy val query  = queryBuilder.createCriteriaDelete(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new DeleteStep[T]()
  }

  /**
    * update entity
    *
    * @tparam T entity type
    * @return update step
    */
  def update[T:ClassTag]:UpdateStep[T]={
    lazy val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    lazy val queryBuilder = ActiveRecord.entityManager.getCriteriaBuilder
    lazy val query  = queryBuilder.createCriteriaUpdate(clazz)
    lazy val root = query.from(clazz)
    implicit lazy val queryContext = QueryContext(queryBuilder,query,root)

    new UpdateStep[T]()
  }

  /**
    * create field
    *
    * @param name field name
    * @tparam T field type
    * @return
    */
  def column[T : TypeTag](name:String):Field[T]={
    new JPAField[T](name)
  }
}
class ConditionClause[R](implicit val context: QueryContext) extends ConditionsGetter {
  private var condition:Option[Predicate] = None
  def apply(fun: =>Condition):this.type={
    and(fun)
    this
  }
  def or(fun: =>Condition):this.type={
    DSL.dslContext.withValue(context){
      val currentCondition = fun
      condition =Some(condition.fold(currentCondition){p=>context.builder.or(Array[Predicate](p,currentCondition):_*)})
    }
    this
  }
  def and(fun: =>Condition):this.type={
    DSL.dslContext.withValue(context){
      val currentCondition = fun
      condition =Some(condition.fold(currentCondition){p=>context.builder.and(Array[Predicate](p,currentCondition):_*)})
    }
    this
  }
  def join[F](field:Field[F],joinType: JoinType=JoinType.LEFT)(fun: =>Condition):this.type={
    DSL.dslContext.withValue(context){
      val join = context.root.join(field.fieldName,joinType)
      val joinContext = JoinQueryContext(context.root.get(field.fieldName),join)
      DSL.joinContext.withValue(joinContext) {
        val currentCondition = fun
        condition = Some(condition.fold(currentCondition) { p => context.builder.and(Array[Predicate](p, currentCondition): _*) })
      }
    }
    this
  }
  override private[activerecord] def conditionOpt: Option[Predicate] = condition
}
class SelectStep[T,R](clazz:Class[T])(implicit val context: QueryContext) extends Fetch[R] with LimitClause with ConditionsGetter with OrderByClause with UtilSupport with GroupByClause{
  private lazy val criteriaQuery = context.query.asInstanceOf[CriteriaQuery[T]]
  def where:DSLSelectionQuery[T,R]=new ConditionClause[T] with LimitClause with Fetch[R] with OrderByClause with GroupByClause with UtilSupport
  def apply(f:SelectionField*):this.type={
    DSL.dslContext.withValue(context){
      if(f.nonEmpty) {
        val index = f.indexWhere(_.isInstanceOf[DistinctSelectionField])
        if(index > 0)
          throw new RuntimeException("distinct field must in first field")
        val selection = context.builder.array(f.map(_.toSelection): _*).asInstanceOf[Selection[T]]
        criteriaQuery.select(selection).distinct(index == 0)

        /*
        f.foreach{
          case dsf:DistinctSelectionField =>
             criteriaQuery = criteriaQuery.select(dsf.toSelection).distinct(true)
          case sf:SelectionField =>
//          val selection = context.builder.array(f.map(_.toSelection): _*)
            criteriaQuery = criteriaQuery.select(sf.toSelection)
        }
        */
      }
      context.query = criteriaQuery
    }
    this
  }
}
class UpdateStep[T](implicit val context: QueryContext) extends AbstractExecuteStep[T] with Dynamic{
  private lazy val criteriaUpdate = context.query.asInstanceOf[CriteriaUpdate[T]]
  def applyDynamicNamed(name:String)(params:(String,Any)*):this.type=macro ActiveRecordMacroDefinition.updateMethodImpl[T,this.type]
  def applyDynamic(name:String)(params:UpdateField *): this.type ={
    params.foreach(x=>x(criteriaUpdate))
    this
  }
  def setWithType[F](field:String,value:F):this.type={
    criteriaUpdate.set(field,value)
    this
  }
  def setWithType[F](field:Field[F],value:F):this.type={
    criteriaUpdate.set(field.fieldName,value)
    this
  }
  def internalUpdate(params: (String,Any)*):this.type = {
    params.foreach{
      case (field,value) =>
        criteriaUpdate.set(field,value)
    }
    this
  }
}
class DeleteStep[T](implicit val context: QueryContext) extends AbstractExecuteStep[T]{}

sealed abstract class AbstractExecuteStep[T](implicit context:QueryContext) extends ConditionsGetter with Execute[T] with LimitClause{
  def where:DSLExecuteQuery[T]=new ConditionClause[T] with Execute[T] with LimitClause with UtilSupport
}
private[activerecord] trait GroupByClause{
  val context:QueryContext
  def groupBy[T](field: Field[T]): this.type ={
      context.query.asInstanceOf[CriteriaQuery[_]].groupBy(context.root.get(field.fieldName))
    this
  }
}
private[activerecord] trait UtilSupport{
  val context:QueryContext
  def getPath(paths: String*):Path[_] ={
    var path:Path[_] = context.root

    paths.foreach(p=>{
      path = path.get(p)
    })

    path
  }
}
private[activerecord] trait OrderByClause {
  this:UtilSupport=>
  val context:QueryContext
  def orderBy[T](field: Field[T]): this.type ={
    orderBy(field.asc)
    this
  }
  def orderBy(orderBuilder:QueryContext=>Order):this.type={
    context.query.asInstanceOf[CriteriaQuery[_]].orderBy(orderBuilder(context))
    this
  }
  def orderBy[T](field: SortField[T]): this.type ={
    val fieldPaths = field.field.fieldName.split("\\.")
    val finalPath =
      if(fieldPaths.isEmpty) getPath(field.field.fieldName)
      else getPath(unsafeWrapArray(fieldPaths):_*)
    if(field.isAsc)
      context.query.asInstanceOf[CriteriaQuery[_]].orderBy(context.builder.asc(finalPath))
    else
      context.query.asInstanceOf[CriteriaQuery[_]].orderBy(context.builder.desc(finalPath))
    this
  }
}
private[activerecord] trait LimitClause{
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
  private[activerecord] def conditionOpt:Option[Predicate]=None
}
private[activerecord] trait Execute[A]{
  this:LimitClause with ConditionsGetter =>
  val context:QueryContext
  def execute: Int ={
    ActiveRecord.executeInTransaction{entityManager=>
    val query = context.query match{
      case q:CriteriaUpdate[A] =>
        conditionOpt.foreach(q.where)
        entityManager.createQuery(q)
      case q:CriteriaDelete[A] =>
        conditionOpt.foreach(q.where)
        entityManager.createQuery(q)
    }
    if(limitNum >0 )
      query.setMaxResults(limitNum)
    if(offsetNum > 0)
      query.setFirstResult(offsetNum)

      query.executeUpdate()
    }
  }
}
private[activerecord] trait Fetch[A] extends Iterable[A]{
  this:LimitClause with ConditionsGetter =>
  val context:QueryContext
  private lazy val executeQuery:LazyList[A]= fetchAsStream
  private lazy val totalNum:Long = executeCount
  private def fetchAsStream: LazyList[A]={
    val entityManager = ActiveRecord.entityManager
    val criteriaQuery = context.query.asInstanceOf[CriteriaQuery[A]]
    conditionOpt.foreach(criteriaQuery.where)
    val query = entityManager.createQuery(criteriaQuery)
    if(limitNum >0 )
      query.setMaxResults(limitNum)
    if(offsetNum > 0)
      query.setFirstResult(offsetNum)

    query.getResultList.asScala.to(LazyList)
  }

  override def iterator: Iterator[A] = executeQuery.iterator

  /**
    * count all records
    * @return
    */
  def count:Long=totalNum
  private def findFirstExpression(selection:Selection[_]): Expression[_] ={
    selection match{
      case e:Expression[_]=> e
      case s:CompoundSelection[_] => findFirstExpression(s.getCompoundSelectionItems.get(0))
    }
  }
  private def executeCount:Long={
    val entityManager = ActiveRecord.entityManager
    val criteriaQuery = context.query.asInstanceOf[CriteriaQuery[A]]

    val countSelection =
      if(criteriaQuery.isDistinct){
        val querySelection = criteriaQuery.getSelection
        val expression = findFirstExpression(querySelection)
        context.builder.countDistinct(expression)
      }
      else
        context.builder.count(context.root)
    //remove distinct expression
    criteriaQuery.select(countSelection.asInstanceOf[Selection[A]]).distinct(false)
    conditionOpt.foreach(criteriaQuery.where)
    val query = entityManager.createQuery(criteriaQuery)

    query.getSingleResult.asInstanceOf[Long]
  }
}
