package stark.activerecord.services

import javax.persistence.EntityManager

import org.springframework.transaction.annotation.Transactional

import scala.reflect.ClassTag

/**
 * entity service
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
trait EntityService {
  def count[T](relation: Relation[T]): Long

  @Transactional
  def save[T](entity:T):T
  @Transactional
  def merge[T](entity:T):T
  def refresh[T](entity:T):Unit
  @Transactional
  def deleteById[T:ClassTag](id:Any):Unit
  @Transactional
  def delete[T](entity:T):Unit
  @Transactional
  def execute[T](function :EntityManager=>Int):Int
  @Transactional
  def deleteRelation[T](relation: DynamicUpdateSupport[T]):Int
  @Transactional
  def updateRelation[T](relation: DynamicUpdateSupport[T]):Int
  def find[T](relation:Relation[T]):LazyList[T]
}
