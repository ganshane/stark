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
}
trait Condition{
  def and(condition: Condition):Condition
  def or(condition: Condition):Condition
  def toPredicate:Predicate
}
class FieldEqualsCondition(field:Field,value:Any) extends Condition{
  private val expression:Predicate =  DSL.dslContext.value.builder.equal(DSL.dslContext.value.root.get(field.fieldName),value)
  override def and(condition: Condition): Condition =
    DSL.dslContext.value.builder.and(Seq[Predicate](expression,condition):_*)
  override def or(condition: Condition): Condition =
    DSL.dslContext.value.builder.or(Seq[Predicate](expression,condition):_*)
  override def toPredicate: Predicate = expression
}
class PredicateCondition(predicate: Predicate) extends Condition{
  override def and(condition: Condition): Condition =
    DSL.dslContext.value.builder.and(Seq[Predicate](predicate,condition):_*)
  override def or(condition: Condition): Condition =
    DSL.dslContext.value.builder.or(Seq[Predicate](predicate,condition):_*)
  override def toPredicate: Predicate = predicate
}
