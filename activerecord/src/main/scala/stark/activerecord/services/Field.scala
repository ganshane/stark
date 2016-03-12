package stark.activerecord.services


import scala.reflect.runtime.universe._
import scala.reflect.runtime.universe.definitions._

/**
 * ActiveRecord field
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
trait Field[T] {
  val fieldName:String
  def === (value:T): Condition
  def >(value:T)(implicit n:Numeric[T]): Condition
}
class JPAField[T : TypeTag](val fieldName:String)  extends Field[T] {
  def ===(value: T): Condition = {
    Condition.eq(this,value)
  }
  def >(value:T)(implicit n:Numeric[T]): Condition= {
    if(typeOf[T] weak_<:< DoubleTpe)
        Condition.gt(this,value)
    else
        throw new IllegalArgumentException("wrong type "+typeOf[T]+" ,Number Type required")
  }
}
