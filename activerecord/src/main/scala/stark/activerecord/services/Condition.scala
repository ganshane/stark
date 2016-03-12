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
  def gt[T](field:Field[T],value:T): Condition={
    val predicate =  DSL.dslContext.value.builder.gt(DSL.dslContext.value.root.get(field.fieldName),value.asInstanceOf[Number])
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
