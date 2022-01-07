package stark.activerecord.services

import javax.persistence.criteria.{CriteriaBuilder, Expression, Path, Predicate}
import stark.activerecord.services.Condition._

import scala.language.implicitConversions

/**
 * condition
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object Condition{
  implicit def conditionToPredicate(condition:Condition):Predicate = condition.toPredicate
  implicit def predicateToCondition(predicate: Predicate):Condition= new PredicateCondition(predicate)

  type ExpressionBuilder[T] = (CriteriaBuilder) =>Expression[T]

  private[activerecord] def findFieldPath[T](name:String):Path[T]={
    if(DSL.joinContext.value != null){
      DSL.joinContext.value.join.get(name)
    }else{
      val fieldPaths = name.split("\\.")
      var path:Path[_] = DSL.dslContext.value.root

      if(fieldPaths.isEmpty) {
        path=path.get(name)
      }
      else{
        fieldPaths.foreach(p => {
          path = path.get(p)
        })
      }
      path.asInstanceOf[Path[T]]
    }
  }
  def minus[T](fields:Field[T]*):ExpressionBuilder[java.lang.Number]={
    (builder:CriteriaBuilder)=>{
      fields.toList match{
        case head::rest=>
          rest.foldLeft[Expression[java.lang.Number]](head.getExpression(DSL.dslContext.value.builder).asInstanceOf[Expression[java.lang.Number]]){ (result, field)=>
            builder.diff(result,field.getExpression(DSL.dslContext.value.builder).asInstanceOf[Expression[java.lang.Number]])
          }
        case _ =>
          throw new IllegalArgumentException("fields is empty")
      }
    }
  }
  def plus[T](fields:Field[T]*):ExpressionBuilder[java.lang.Number]={
    (builder:CriteriaBuilder)=>{
      fields.toList match{
        case head::rest=>
          rest.foldLeft[Expression[java.lang.Number]](head.getExpression(DSL.dslContext.value.builder).asInstanceOf[Expression[java.lang.Number]]){ (result, field)=>
            builder.sum(result,field.getExpression(DSL.dslContext.value.builder).asInstanceOf[Expression[java.lang.Number]])
          }
        case _ =>
          throw new IllegalArgumentException("fields is empty")
      }
    }
  }
  def eq[T](field:Field[T],value:T): Condition ={
    new PredicateCondition(
      value match{
        case v:Field[_] =>
          DSL.dslContext.value.builder.equal(field.getExpression(DSL.dslContext.value.builder), v.getExpression(DSL.dslContext.value.builder))
        case _ =>
          DSL.dslContext.value.builder.equal(field.getExpression(DSL.dslContext.value.builder),value)
      }
    )
  }
  def notEq[T](field:Field[T],value:Any):Condition={
    new PredicateCondition(
      value match{
        case v:Field[_]=>
          DSL.dslContext.value.builder.notEqual( field.getExpression(DSL.dslContext.value.builder), v.getExpression(DSL.dslContext.value.builder))
        case _ =>
          DSL.dslContext.value.builder.notEqual(field.getExpression(DSL.dslContext.value.builder),value)
      }
    )
  }
  def gt[T](field:Field[T],value:Any): Condition={
    new PredicateCondition(
      value match{
        case v:Field[_] =>
          DSL.dslContext.value.builder.gt(field.getExpression(DSL.dslContext.value.builder),v.getExpression(DSL.dslContext.value.builder))
        case v:Number =>
          DSL.dslContext.value.builder.gt(field.getExpression(DSL.dslContext.value.builder),v)
        case other:Comparable[Any] =>
          DSL.dslContext.value.builder.greaterThan(field.getExpression(DSL.dslContext.value.builder),other)
      }
    )
  }
  def ge[T](field:Field[T],value:Any): Condition={
    new PredicateCondition(
      value match{
        case v:Field[_] =>
          DSL.dslContext.value.builder.ge(field.getExpression(DSL.dslContext.value.builder),v.getExpression(DSL.dslContext.value.builder))
        case v:Number =>
          DSL.dslContext.value.builder.ge(field.getExpression(DSL.dslContext.value.builder),v)
        case other:Comparable[Any] =>
          val expression:Path[Comparable[Any]] = field.getExpression(DSL.dslContext.value.builder).asInstanceOf[Path[Comparable[Any]]]
          DSL.dslContext.value.builder.greaterThanOrEqualTo(expression,other)
      }
    )
  }
  def lt[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      value match {
        case v:Field[_]=>
          DSL.dslContext.value.builder.lt(field.getExpression(DSL.dslContext.value.builder),v.getExpression(DSL.dslContext.value.builder))
        case v:Number =>
          DSL.dslContext.value.builder.lt(field.getExpression(DSL.dslContext.value.builder),v)
        case other:Comparable[Any] =>
          DSL.dslContext.value.builder.lessThan(field.getExpression(DSL.dslContext.value.builder),other)
      }
    )
  }

  def le[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      value match {
        case v:Field[_]=> DSL.dslContext.value.builder.le(field.getExpression(DSL.dslContext.value.builder),v.getExpression(DSL.dslContext.value.builder))
        case v: Number =>
          DSL.dslContext.value.builder.le(field.getExpression(DSL.dslContext.value.builder), v)
        case other: Comparable[Any] =>
          DSL.dslContext.value.builder.lessThanOrEqualTo(field.getExpression(DSL.dslContext.value.builder), other)
      }
    )
  }
  def notNull[T](field:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.isNotNull(field.getExpression(DSL.dslContext.value.builder))
    )
  }
  def isNull[T](field:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.isNull(field.getExpression(DSL.dslContext.value.builder))
    )
  }
  def between[T<:Comparable[T]](field:Field[T],v1:T,v2:T):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.between(findFieldPath[T](field.fieldName),v1,v2)
    )
  }
  def like[T](field:Field[T],value:String):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.like(findFieldPath[String](field.fieldName),value)
    )
  }
  def notLike[T](field:Field[T],value:String):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.notLike(findFieldPath[String](field.fieldName),value)
    )
  }
}
trait Condition{
  def and(condition: Condition):Condition
  def or(condition: Condition):Condition
  def or(conditionFun: ()=>Condition):Condition
  def toPredicate:Predicate
}
class PredicateCondition(predicateFun: => Predicate) extends Condition{
  private lazy val expression:Predicate = predicateFun
  override def and(condition: Condition): Condition =
    DSL.dslContext.value.builder.and(Seq[Predicate](expression,condition):_*)
  override def or(condition: Condition): Condition =
    DSL.dslContext.value.builder.or(Seq[Predicate](expression,condition):_*)


  override def or(conditionFun: () => Condition): Condition = ???

  override def toPredicate: Predicate = expression
}
