package stark.activerecord.services


import scala.reflect.runtime.universe._

/**
 * ActiveRecord field
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
trait Field[T] {
  val fieldName:String
  def === (value:T): Condition
  def !==(value : T)                                 : Condition
  def !==(value : Field[T])                          : Condition
  def <>(value : T)= !==(value)
  def <>(value : Field[T])= !==(value)

  def >(value:T):Condition
  def >=(value:T):Condition

  def <(value:T):Condition
  def <=(value:T):Condition

  def like(value:String):Condition
  def notLike(value:String):Condition

  def isNull:Condition
  def notNull:Condition

}
class JPAField[T : TypeTag](val fieldName:String)  extends Field[T] {
  def ===(value: T): Condition = {
    Condition.eq(this,value)
  }
  override def !==(value: T): Condition = Condition.notEq(this,value)
  override def !==(value: Field[T]): Condition = Condition.notEq(this,value)

  override def >(value: T): Condition = Condition.gt(this,value)
  override def >=(value: T): Condition = Condition.ge(this,value)


  override def isNull: Condition = Condition.isNull(this)
  override def notNull: Condition = Condition.notNull(this)

  override def <(value: T): Condition = Condition.lt(this,value)
  override def <=(value: T): Condition = Condition.le(this,value)

  override def like(value: String): Condition = Condition.like(this,value)
  override def notLike(value: String): Condition = Condition.notLike(this,value)

}

