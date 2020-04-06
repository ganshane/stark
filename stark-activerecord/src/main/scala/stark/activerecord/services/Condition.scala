package stark.activerecord.services

import javax.persistence.criteria.{Path, Predicate}
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

  private def findFieldPath[T](name:String):Path[T]={
    if(DSL.joinContext.value != null){
      DSL.joinContext.value.join.get(name)
    }else{
      DSL.dslContext.value.root.get(name)
    }
  }
  def eq[T](field:Field[T],value:T): Condition ={
    new PredicateCondition(
      DSL.dslContext.value.builder.equal(findFieldPath(field.fieldName),value)
    )
  }
  def notEq[T](field:Field[T],value:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.notEqual(
        findFieldPath(field.fieldName),
        findFieldPath(value.fieldName)
      )
    )
  }
  def notEq[T](field:Field[T],value:T):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.notEqual(findFieldPath(field.fieldName),value)
    )
  }
  def gt[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      value match{
        case v:Number =>
          DSL.dslContext.value.builder.gt(findFieldPath(field.fieldName),v)
        case other:Comparable[Any] =>
          DSL.dslContext.value.builder.greaterThan(findFieldPath(field.fieldName),other)
      }
    )
  }
  def ge[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      value match{
        case v:Number =>
          DSL.dslContext.value.builder.ge(findFieldPath(field.fieldName),v)
        case other:Comparable[Any] =>
          val expression:Path[Comparable[Any]] = findFieldPath(field.fieldName) //.as(classOf[T])
          DSL.dslContext.value.builder.greaterThanOrEqualTo(expression,other)
      }
    )
  }
  def lt[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      value match {
        case v:Number =>
          DSL.dslContext.value.builder.lt(findFieldPath(field.fieldName),v)
        case other:Comparable[Any] =>
          DSL.dslContext.value.builder.lessThan(findFieldPath(field.fieldName),other)
      }
    )
  }
  def le[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      value match {
        case v: Number =>
          DSL.dslContext.value.builder.le(findFieldPath(field.fieldName), v)
        case other: Comparable[Any] =>
          DSL.dslContext.value.builder.lessThanOrEqualTo(findFieldPath(field.fieldName), other)
      }
    )
  }
  def notNull[T](field:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.isNotNull(findFieldPath(field.fieldName))
    )
  }
  def isNull[T](field:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.isNull(findFieldPath(field.fieldName))
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
