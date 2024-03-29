package stark.activerecord.services

import stark.activerecord.macroinstruction.ActiveRecordMacroDefinition

import scala.collection.immutable.LazyList
import scala.collection.{IterableOnce, mutable}
import scala.language.experimental.macros
import scala.language.{dynamics, postfixOps, reflectiveCalls}

/**
 * query relation interface
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-15
 */
trait Relation[A] {
  protected val primaryKey:String
  private[activerecord] var limit:Int = -1
  private[activerecord] var offset:Int = -1

  private var underlying_result:LazyList[A] = _
  protected def executeQuery: LazyList[A] = {
    if(underlying_result == null)
      underlying_result = ActiveRecord.find(this)
    underlying_result
  }

  private[activerecord] def count:Long={
    ActiveRecord.count(this)
  }

  def order(params:(String,String)*):this.type
  def asc(fields:String*):this.type={
    order(fields.map((_,"asc")):_*)
  }
  def desc(fields:String*):this.type={
    order(fields.map((_,"desc")):_*)
  }
  def exists():Boolean= limit(1).headOption.isDefined
  def limit(n:Int):this.type={
    limit=n
    this
  }
  def take:A= take(1).head
  def takeOption:Option[A]= take(1).headOption
  def take(n:Int):this.type={
    limit(n)
  }
  def first:A= first(1).head
  def firstOption:Option[A]= first(1).headOption
  def first(n:Int):this.type= {
    take(n).order(primaryKey-> "ASC")
  }
  def last:A= last(1).head
  def last(n:Int):this.type= {
    take(n).order(primaryKey->"DESC")
  }
  def offset(n:Int): this.type={
    offset = n
    this
  }
  @inline final def toList= executeQuery.toList
  @inline final def size = executeQuery.size
  @inline final def foreach[U](f: A => U) = executeQuery.foreach(f)
  @inline final def filter(f: A => Boolean) = executeQuery.filter(f)
  @inline final def head = executeQuery.head
  @inline final def headOption = executeQuery.headOption
  @inline final def tail = executeQuery.tail
  @inline final def map[B](f: A => B) = executeQuery.map(f)
  @inline final def flatMap[B](f: A => IterableOnce[B]): LazyList[B] = executeQuery.flatMap(f)
}

trait DynamicUpdateSupport[A] extends Dynamic{
  /**
   * update method
   */
  def applyDynamicNamed(name:String)(params:(String,Any)*):Int=macro ActiveRecordMacroDefinition.updateMethodImpl[A,Int]
  def internalUpdate(params:(String,Any)*): Int
}




/**
 * query case class
 * @param entityClazz entity class
 * @param primaryKey primary key
 * @tparam A type parameter
 */
class QlRelation[A](val entityClazz:Class[A],val primaryKey:String) extends Relation[A] with DynamicUpdateSupport[A]{

  def this(entityClazz:Class[A],primaryKey:String,query:String,queryParams:Seq[Any])={
    this(entityClazz,primaryKey)
    if(query != null)
      this.queryClause = Some(query)

    this.queryParams = queryParams
  }
  private[activerecord] var orderBy:Option[String]=None

  private[activerecord] var queryClause:Option[String] = None
  private[activerecord] var queryParams:Seq[Any] = Nil

  private[activerecord] var updateQl:Option[String] = None
  private[activerecord] var updateParams:Seq[Any] = Nil


  def order(params:(String,String)*):this.type= {
    params.foreach{case (key,value)=>
      orderBy match{
        case Some(o) =>
          orderBy = Some(o+",%s %s".format(key,value))
        case None=>
          orderBy = Some("%s %s".format(key,value))
      }
    }
    this
  }
  def internalUpdate(params:(String,Any)*): Int={
    var ql = ""
    var index = queryParams.size + 1
    val updateParams = mutable.Buffer[Any]()
    params.foreach{
      case (key,value)=>
        ql += "%s=?%s,".format(key,index)
        index += 1
        updateParams += value
    }

    this.updateParams = updateParams.toSeq
    if(ql.length>0){
      this.updateQl = Some(ql.dropRight(1))
    }

    ActiveRecord.updateRelation(this)
  }
  def delete:Int = {
    ActiveRecord.deleteRelation(this)
  }
}
