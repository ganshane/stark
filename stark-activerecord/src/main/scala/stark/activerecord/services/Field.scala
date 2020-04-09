package stark.activerecord.services


import javax.persistence.criteria.Selection
import org.joda.time.DateTime

import scala.language.implicitConversions
import scala.reflect.runtime.universe._

/**
 * ActiveRecord field
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
object Field {
  def countField= new SelectionField {
    override def toSelection[X]: Selection[X] = {
      val context =DSL.dslContext.value
      context.builder.count(context.root).asInstanceOf[Selection[X]]
    }
  }
  implicit def wrapNumericField[T<:Number](field: Field[T]):NumericField[T]= new NumericField(field)
  implicit def wrapNumericField[T](field: Field[T])(implicit num:Numeric[T]) = new NumericField(field)
  implicit def wrapStringField(field: Field[String]):StringField = new StringField(field)
  implicit def wrapDateTimeField(field: Field[DateTime]):DateTimeField= new DateTimeField(field)
  implicit def wrapEnumerationField[T <: Enumeration#Value](field: Field[T]):EnumerationField[T]= new EnumerationField(field)
}
trait Field[T] extends SelectionField{
  val fieldName:String
  def === (value:T): Condition
  def !==(value : T)                                 : Condition
  def !==(value : Field[T])                          : Condition
  def <>(value : T)= !==(value)
  def <>(value : Field[T])= !==(value)

  def isNull:Condition
  def notNull:Condition

  def desc:SortField[T]
  def asc:SortField[T]
  def count:SelectionField
  def sum:SelectionField
  def distinct:SelectionField
  def max:SelectionField
}
trait SelectionField{
  def toSelection[X]:Selection[X]
}
trait DistinctSelectionField extends SelectionField{
}
case class SortField[T](field: Field[T],isAsc:Boolean=true)

private[activerecord] class JPAField[T : TypeTag](val fieldName:String)  extends Field[T] {
  def ===(value: T): Condition = {
    Condition.eq(this,value)
  }
  override def !==(value: T): Condition = Condition.notEq(this,value)
  override def !==(value: Field[T]): Condition = Condition.notEq(this,value)

  override def isNull: Condition = Condition.isNull(this)
  override def notNull: Condition = Condition.notNull(this)

  override def desc: SortField[T] = SortField(this,false)
  override def asc: SortField[T] = SortField(this,isAsc = true)

  override def toSelection[X]: Selection[X]= {
    DSL.dslContext.value.root.get[X](fieldName)
  }
  private def expression[X]={
    DSL.dslContext.value.root.get[X](fieldName)
  }

  override def count: SelectionField ={
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        DSL.dslContext.value.builder.count(expression).asInstanceOf[Selection[X]]
      }
    }
  }
  override def sum: SelectionField ={
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        DSL.dslContext.value.builder.sum(expression).asInstanceOf[Selection[X]]
      }
    }
  }


  override def max: SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        DSL.dslContext.value.builder.max(expression).asInstanceOf[Selection[X]]
      }
    }
  }

  override def distinct: SelectionField = {
    new DistinctSelectionField {
      override def toSelection[X]: Selection[X] = {
        JPAField.this.toSelection
      }
    }
  }
}
class NumericField[T](field:Field[T]){
  def >(value:T):Condition={
    Condition.gt(field,value)
  }
  def >=(value: T): Condition = Condition.ge(field,value)
  def <(value: T): Condition = Condition.lt(field,value)
  def <=(value: T): Condition = Condition.le(field,value)
  def avg:SelectionField = {
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.avg(expression).asInstanceOf[Selection[X]]
      }
    }
  }
  def max:SelectionField={
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.max(expression).asInstanceOf[Selection[X]]
      }
    }
  }
  def min:SelectionField={
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.min(expression).asInstanceOf[Selection[X]]
      }
    }
  }
  def sum:SelectionField={
    new SelectionField {
      override def toSelection[X]: Selection[X] = {
        val expression = DSL.dslContext.value.root.get[Number](field.fieldName)
        DSL.dslContext.value.builder.sum(expression).asInstanceOf[Selection[X]]
      }
    }
  }
}
class StringField(field:Field[String]){
  def like(value: String): Condition = Condition.like(field,value)
  def notLike(value: String): Condition = Condition.notLike(field,value)
}
class DateTimeField(field:Field[DateTime]){
  def >(value:DateTime):Condition={
    Condition.gt(field,value)
  }
  def <(value:DateTime):Condition={
    Condition.lt(field,value)
  }
}
class EnumerationField[T<: Enumeration#Value](field:Field[T]){
  def >(value:T):Condition={
    Condition.gt(field,value)
  }
  def >=(value:T):Condition={
    Condition.ge(field,value)
  }
  def <(value:T):Condition={
    Condition.lt(field,value)
  }
  def <= (value:T):Condition={
    Condition.le(field,value)
  }
}
