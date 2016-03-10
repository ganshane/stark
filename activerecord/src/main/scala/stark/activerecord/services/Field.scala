package stark.activerecord.services

import scala.reflect.ClassTag

/**
 * ActiveRecord field
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
trait Field[T] {
  val fieldName:String
  def === (value:T): Condition
}
class JPAField[T:ClassTag](val fieldName:String)  extends Field[T] {
  def ===(value: T): Condition = {
    new FieldEqualsCondition(this, value)
  }
}
