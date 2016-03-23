package stark.activerecord.services

import javax.persistence.criteria.Predicate

import stark.activerecord.services.Condition._

/**
 * condition
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object Condition{
  implicit def conditionToPredicate(condition:Condition):Predicate = condition.toPredicate
  implicit def predicateToCondition(predicate: Predicate):Condition= new PredicateCondition(predicate)
  def eq[T](field:Field[T],value:T): Condition ={
    new PredicateCondition(
      DSL.dslContext.value.builder.equal(DSL.dslContext.value.root.get(field.fieldName),value)
    )
  }
  def notEq[T](field:Field[T],value:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.notEqual(
        DSL.dslContext.value.root.get(field.fieldName),
        DSL.dslContext.value.root.get(value.fieldName))
    )
  }
  def notEq[T](field:Field[T],value:T):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.notEqual(DSL.dslContext.value.root.get(field.fieldName),value)
    )
  }
  def gt[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.gt(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    )
  }
  def ge[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.ge(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    )
  }
  def lt[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.lt(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    )
  }
  def le[T](field:Field[T],value:T): Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.le(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    )
  }
  def notNull[T](field:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.isNotNull(DSL.dslContext.value.root.get(field.fieldName))
    )
  }
  def isNull[T](field:Field[T]):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.isNull(DSL.dslContext.value.root.get(field.fieldName))
    )
  }
  def between[T<:Comparable[T]](field:Field[T],v1:T,v2:T):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.between(DSL.dslContext.value.root.get[T](field.fieldName),v1,v2)
    )
  }
  def like[T](field:Field[T],value:String):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.like(DSL.dslContext.value.root.get[String](field.fieldName),value)
    )
  }
  def notLike[T](field:Field[T],value:String):Condition={
    new PredicateCondition(
      DSL.dslContext.value.builder.notLike(DSL.dslContext.value.root.get[String](field.fieldName),value)
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
