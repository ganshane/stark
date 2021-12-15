package stark.activerecord.internal

import javax.persistence.{EntityManager, Query}

import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import stark.activerecord.services._

import scala.collection.JavaConverters._
import scala.collection.immutable.Stream
import scala.reflect.{ClassTag, classTag}

/**
 * implement EntityService
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
class EntityServiceImpl(entityManager:EntityManager) extends EntityService {
  private val logger = LoggerFactory getLogger getClass
  @Transactional
  override def save[T](entity: T): T = {
    entityManager.persist(entity)
    entity
  }

  @Transactional
  override def merge[T](entity: T): T = {
    entityManager.merge(entity)
  }

  override def refresh[T](entity: T):Unit = {
    entityManager.refresh(entity)
  }

  @Transactional
  override def deleteById[T: ClassTag](id: Any): Unit = {
    val entity = entityManager.find(classTag[T].runtimeClass, id)
    entityManager.remove(entity)
  }

  @Transactional
  override def delete[T](entity: T): Unit = {
    entityManager.remove(entity)
  }


  @Transactional
  override def execute[T](func: EntityManager=>Int): Int={
    func(entityManager)
  }

  @Transactional
  override def deleteRelation[T](updateSupport: DynamicUpdateSupport[T]): Int = {
    updateSupport match {
      case relation:QlRelation[T]=>
        var fullQl = "delete from %s".format (relation.entityClazz.getSimpleName)
        relation.queryClause.foreach {
          fullQl += " where %s".format (_)
        }
        val query = entityManager.createQuery (fullQl)

        setQueryParameter (query, relation)

        query.executeUpdate()
      case other =>
        throw new UnsupportedOperationException
    }
  }

  @Transactional
  override def updateRelation[T](updateObject: DynamicUpdateSupport[T]): Int = {
    updateObject match {
      case relation: QlRelation[T] =>
        var fullQl = "update %s set".format(relation.entityClazz.getSimpleName)
        relation.updateQl.foreach {
          fullQl += " %s".format(_)
        }
        relation.queryClause.foreach {
          fullQl += " where %s".format(_)
        }

        val query = entityManager.createQuery(fullQl)

        var index: Int = setQueryParameter(query, relation)

        relation.updateParams.foreach { value =>
          query.setParameter(index, value)
          index += 1
        }
        query.executeUpdate()
      case other =>
        throw new UnsupportedOperationException
    }
  }

  private def setQueryParameter[T](query:Query,relation: QlRelation[T]): Int = {
    var index = 1
    relation.queryParams.foreach { value =>
      query.setParameter(index, value)
      index += 1
    }
    index
  }

  /**
   * find some records by Relation
 *
   * @param queryObj relation object
   * @tparam T type parameter
   * @return record stream
   */
  def find[T](queryObj:Relation[T]):Stream[T]={
    val query = queryObj match {
      case relation: QlRelation[T] =>
        var fullQl = "from %s".format(relation.entityClazz.getSimpleName)
        relation.queryClause.foreach {
          fullQl += " where %s".format(_)
        }
        relation.orderBy.foreach {
          fullQl += " order by %s".format(_)
        }

        logger.debug("ql:{}", fullQl)
        val query = entityManager.createQuery(fullQl)

        setQueryParameter(query, relation)

        query
      case other=>
        throw new UnsupportedOperationException("%s unspported".format(other))
    }
    if (queryObj.offset > -1)
      query.setFirstResult(queryObj.offset)
    if (queryObj.limit > -1)
      query.setMaxResults(queryObj.limit)
    //convert as scala stream
    query.getResultList.asInstanceOf[java.util.List[T]].asScala.toStream
//    JavaConversions.asScalaBuffer[T](query.getResultList.asInstanceOf[java.util.List[T]]).toStream
  }

  override def count[T](queryObj: Relation[T]): Long = {
    val query = queryObj match {
      case relation: QlRelation[T] =>
        var fullQl = "select count(*) from %s".format(relation.entityClazz.getSimpleName)

        relation.queryClause.foreach {
          fullQl += " where %s".format(_)
        }
        logger.debug("ql:{}", fullQl)
        val query = entityManager.createQuery(fullQl)

        setQueryParameter(query, relation)

        query
      case other=>
        throw new UnsupportedOperationException("%s unspported".format(other))
    }
    query.getResultList.get(0).asInstanceOf[Long]
  }
}
