package stark.activerecord.services

/**
 * ActiveRecord field
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
trait Field {
  val fieldName:String
  def === (value:Any): Condition
}
class JPAField(val fieldName:String)  extends Field{
  def === (value:Any): Condition = {
    new FieldEqualsCondition(this,value)
  }
}
