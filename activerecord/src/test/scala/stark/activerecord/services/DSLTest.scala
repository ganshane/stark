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

    val streamQL = from(classOf[ModelA]) where column("name") === "asdf" or (
      column("seq") === 1 or column("name") === "asdf"
       or column("seq") === 1 or column("name") === "fdsa"
      ) limit 3 offset 0
    val stream = streamQL.fetch


    Assert.assertEquals(1,stream.size)
  }
}
