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
    modelA.name = "cctv"
    modelA.save
    val modelA2 = new ModelA
    modelA2.name="asdf"
    modelA2.save

    println(ModelA.name)
    val q1 = from[ModelA]
    Assert.assertEquals(2,q1.size)
    val q2 = from[ModelA] limit 10
    val q3 = from[ModelA] where ModelA.name === "cctv" or (
      ModelA.seq === 1 or ModelA.name === "cctv"
       or ModelA.seq === 1 or ModelA.name === "fdsa"

      ) limit 3 offset 0


    Assert.assertEquals(1,q3.size)
  }
}
