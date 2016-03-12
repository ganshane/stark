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
    val predicate =  DSL.dslContext.value.builder.equal(DSL.dslContext.value.root.get(field.fieldName),value)
    new PredicateCondition(predicate)
  }
  def notEq[T](field:Field[T],value:Field[T]):Condition={
    val predicate =  DSL.dslContext.value.builder.notEqual(
      DSL.dslContext.value.root.get(field.fieldName),
      DSL.dslContext.value.root.get(value.fieldName))
    new PredicateCondition(predicate)
  }
  def notEq[T](field:Field[T],value:T):Condition={
    val predicate =  DSL.dslContext.value.builder.notEqual(DSL.dslContext.value.root.get(field.fieldName),value)
    new PredicateCondition(predicate)
  }
  def gt[T](field:Field[T],value:T): Condition={
    val predicate =  DSL.dslContext.value.builder.gt(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    new PredicateCondition(predicate)
  }
  def ge[T](field:Field[T],value:T): Condition={
    val predicate =  DSL.dslContext.value.builder.ge(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    new PredicateCondition(predicate)
  }
  def lt[T](field:Field[T],value:T): Condition={
    val predicate =  DSL.dslContext.value.builder.lt(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    new PredicateCondition(predicate)
  }
  def le[T](field:Field[T],value:T): Condition={
    val predicate =  DSL.dslContext.value.builder.le(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
    new PredicateCondition(predicate)
  }
  def notNull[T](field:Field[T]):Condition={
    val predicate =  DSL.dslContext.value.builder.isNotNull(DSL.dslContext.value.root.get(field.fieldName))
    new PredicateCondition(predicate)
  }
  def isNull[T](field:Field[T]):Condition={
    val predicate =  DSL.dslContext.value.builder.isNull(DSL.dslContext.value.root.get(field.fieldName))
    new PredicateCondition(predicate)
  }
  def between[T<:Comparable[T]](field:Field[T],v1:T,v2:T):Condition={
    val predicate =  DSL.dslContext.value.builder.between(DSL.dslContext.value.root.get[T](field.fieldName),v1,v2)
    new PredicateCondition(predicate)
  }
  def like[T](field:Field[T],value:String):Condition={
    val predicate =  DSL.dslContext.value.builder.like(DSL.dslContext.value.root.get[String](field.fieldName),value)
    new PredicateCondition(predicate)
  }
  def notLike[T](field:Field[T],value:String):Condition={
    val predicate =  DSL.dslContext.value.builder.notLike(DSL.dslContext.value.root.get[String](field.fieldName),value)
    new PredicateCondition(predicate)
  }

}
trait Condition{
  def and(condition: Condition):Condition
  def or(condition: Condition):Condition
  def toPredicate:Predicate
}
class PredicateCondition(predicate: Predicate) extends Condition{
  override def and(condition: Condition): Condition =
    DSL.dslContext.value.builder.and(Seq[Predicate](predicate,condition):_*)
  override def or(condition: Condition): Condition =
    DSL.dslContext.value.builder.or(Seq[Predicate](predicate,condition):_*)
  override def toPredicate: Predicate = predicate
}
