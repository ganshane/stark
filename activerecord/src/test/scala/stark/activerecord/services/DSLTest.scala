package stark.activerecord.services

import org.junit.{Assert, Test}
import stark.activerecord.services.DSL._
import stark.activerecord.{BaseActiveRecordTestCase, ModelA}

import scala.language.postfixOps

/**
 * dsl test
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
class DSLTest extends BaseActiveRecordTestCase{
  @Test
  def test_dsl: Unit ={
    val modelA = new ModelA
    modelA.name = "asdf"
    modelA.save

    /*
    val entityManager = getService[EntityManager]
    val queryBuilder = getService[EntityManager].getCriteriaBuilder
    val query  = queryBuilder.createQuery(classOf[ModelA])
    val root = query.from(classOf[ModelA])
    implicit val context = QueryContext(queryBuilder,query,root)

    val field = new JPAField[String]("name")
    val condition = field === "asdf"
    val field2 = new JPAField[Int]("seq")
    val finalCondition = condition.or(field2 === 1)

    query.where(Seq(finalCondition.toPredicate):_*)

    entityManager.createQuery(query).getResultList
    */


    val streamQL = from(classOf[ModelA]) where column("name") === "asdf" or (
      column("seq") === 1 or column("name") === "asdf"
       or column("seq") === 1 or column("name") === "fdsa"
      )
    val stream = streamQL.fetch


    Assert.assertEquals(1,stream.size)
  }
}
