package stark.activerecord.services

import scala.reflect.ClassTag
import scala.reflect.classTag

/**
 * ActiveRecord field
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
trait Field[T] {
  val fieldName:String
  def === (value:T): Condition
}
class JPAField[T:ClassTag](val fieldName:String)  extends Field[T]{
  private lazy val dataType = classTag[T].runtimeClass
  def === (value:T): Condition = {
    new FieldEqualsCondition(this,value)
  }
}
