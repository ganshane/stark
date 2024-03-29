package stark.activerecord.services

import org.junit.{Assert, Test}
import stark.activerecord.services.DSL._
import stark.activerecord.services.Field.wrapNumericField
import stark.activerecord.{BaseActiveRecordTestCase, ModelA, ModelB}

import java.time.{LocalTime, ZonedDateTime}
import java.time.temporal.TemporalAdjusters
import javax.persistence.EntityManager
import javax.persistence.criteria.{Expression, Predicate, Selection}
import scala.language.postfixOps

/**
 * dsl test
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-03-09
 */
class DSLTest extends BaseActiveRecordTestCase{
  @Test
  def test_sum_expression: Unit ={
    Range(0,5).foreach(i=>{
      val modelA = new ModelA
      modelA.i = 12
      modelA.l = 1
      modelA.save()
    })

    val result  = ModelA.where(ModelA.i + ModelA.l === 13 ).toList
    Assert.assertEquals(5,result.size)
    val result2  = ModelA.where(ModelA.i + ModelA.l + ModelA.l === 14 ).toList
    Assert.assertEquals(5,result2.size)
    val result3  = ModelA.where(ModelA.i + ModelA.l + ModelA.l > ModelA.i).toList
    Assert.assertEquals(5,result3.size)
    val result4  = ModelA.where(ModelA.i + ModelA.l + ModelA.l < ModelA.i).toList
    Assert.assertEquals(0,result4.size)

    val result5  = ModelA.where(ModelA.i - ModelA.l + ModelA.l === ModelA.i).toList
    Assert.assertEquals(5,result5.size)
  }
  @Test
  def test_orderByBuilder: Unit ={
    val modelA = new ModelA
    modelA.zonedDateTime = ZonedDateTime.now()
    modelA.l = 1L
    modelA.i = 2
    modelA.save()

    val coll = ModelA.where.orderBy(context=>{
      val cb = context.builder
      val r = context.root
      cb.asc(cb.prod(r.get("l"), r.get("i")))
    })

    Assert.assertEquals(1,coll.size)
  }
  @Test //测试引用字段
  def test_zonedDateTime:Unit={
    val modelA = new ModelA
    modelA.zonedDateTime = ZonedDateTime.now()
    modelA.save()

    val max = LocalTime.MAX
    val beginTime = ZonedDateTime.now().minusDays(1).withHour(max.getHour).withMinute(max.getMinute).withSecond(max.getSecond).withNano(max.getNano)
    val min = LocalTime.MIN
    val endTime = ZonedDateTime.now().plusDays(1).withHour(min.getHour).withMinute(min.getMinute).withSecond(min.getSecond).withNano(min.getNano)

    val result:Iterable[ModelA]  = ModelA.where(ModelA.zonedDateTime > beginTime and ModelA.zonedDateTime < endTime)
    Assert.assertEquals(1,result.size)
    Assert.assertTrue(result.nonEmpty)
    val result2:Iterable[ModelA] = select[ModelA].where(ModelA.zonedDateTime > modelA.zonedDateTime.minusDays(1) and ModelA.zonedDateTime < ZonedDateTime.now())
    Assert.assertEquals(1,result2.size)
    Assert.assertTrue(result2.nonEmpty)

  }
  @Test //测试引用字段
  def test_fieldRef:Unit={
    ModelA.where(ModelA.seq === ModelA.l).toList
    ModelA.where(ModelA.seq > ModelA.l).toList
  }
  @Test
  def test_update: Unit ={
    val num = update[ModelA] set (ModelA.name ~=? null) set(seq = 1) where ModelA.name === "cctv" or ModelA.seq === 1  execute;
  }
  @Test
  def test_simpleJoin: Unit = {

    val query = ModelB.where.join[ModelA](ModelB.modelA)(ModelA.i === 123)
    query.size
    val modelA = new ModelA
    modelA.name = "cctv"
    modelA.i = 1234
    modelA.save
    val modelB = new ModelB
    modelB.modelA = modelA
    modelB.testId = modelA.id
    modelB.save

    val query2 = ModelB.where.join[ModelA](ModelB.modelA)(ModelA.i === 1234)
    Assert.assertEquals(1,query2.size)
    Assert.assertEquals(modelA.id,query2.head.testId)

    val query3 = ModelB.where.join[ModelA](ModelB.testTableJoin)(ModelA.i === 22222)
    Assert.assertEquals(0,query3.size)
  }
  @Test
  def test_select_one: Unit = {
    val modelA = new ModelA
    modelA.name = "cctv"
    modelA.i = 1234
    modelA.l = 1234L
    modelA.save

    val idOpt = ModelA.select(ModelA.id).where.headOption
    Assert.assertEquals(1,idOpt.get)
  }
  @Test
  def test_sum: Unit = {
    val modelA = new ModelA
    modelA.name = "cctv"
    modelA.i = 1234
    modelA.l = 1234L
    modelA.save

    Assert.assertEquals(1234L,select[ModelA](ModelA.i.sum) head)
    val tmpL:Any = select[ModelA](ModelA.l.sum) head

    Assert.assertEquals(1234L,tmpL.asInstanceOf[Long])
    Assert.assertEquals(1234,select[ModelA](ModelA.i.max) head)
    Assert.assertEquals(1234,select[ModelA](ModelA.i.min) head)
    Assert.assertEquals(1234.0,select[ModelA](ModelA.i.avg) head)

    val multiSum:Array[Any] = select[ModelA](ModelA.l.sum,ModelA.i.sum).groupBy(ModelA.name).head

    Assert.assertEquals(1234L,multiSum(0).asInstanceOf[Long])
    Assert.assertEquals(1234L,multiSum(1).asInstanceOf[Long])
  }
  @Test
  def testBuildCondition:Unit={
    ModelA.select(ModelA.buildSelection(context=>{
      val cb = context.builder
      val root = context.root
      new SelectionField {
        override def toSelection[X]: Selection[X] = {
          cb.sum(root.get[java.lang.Long]("l"),2L.asInstanceOf[java.lang.Long]).asInstanceOf[Selection[X]]
        }
      }
    })).where(ModelA.buildCondition(context=>{
      val cb = context.builder
      val root = context.root
      cb.le(cb.sum(root.get[java.lang.Long]("l"),2L.asInstanceOf[java.lang.Long]),12)
    }).and(ModelA.name === "A")).toList
  }
  @Test
  def test_select: Unit = {
    val modelA = new ModelA
    modelA.name = "cctv"
    modelA.save
    val modelA2 = new ModelA
    modelA2.name = "asdf"
    modelA2.save;

    val q1 = select[ModelA](ModelA.name,ModelA.seq) orderBy  ModelA.name
    Assert.assertEquals(2, q1.size)
    val head = q1.head
    val first = q1.head
    val q2 = select[ModelA](ModelA.name.count,ModelA.name) groupBy ModelA.name
    Assert.assertEquals(2, q2.size)
    Assert.assertEquals(1L, q2.head.head)

    val q3 = select[ModelA] groupBy ModelA.name
    Assert.assertEquals(2, q3.size)

    Assert.assertEquals(2L, ModelA.count.head)
    Assert.assertEquals(2L, select[ModelA].count)



  }
  @Test
  def test_dsl: Unit ={
    val modelA = new ModelA
    modelA.name = "cctv"
    modelA.save
    val modelA2 = new ModelA
    modelA2.name="asdf"
    modelA2.save;
    {
      val q1 = select[ModelA] orderBy  ModelA.name
      Assert.assertEquals(2, q1.size)
      val q2 = select[ModelA] limit 10 offset 1
      Assert.assertEquals(1, q2.size)

      val q3 = select[ModelA] where ModelA.name === "cctv" and (
        ModelA.seq === 1 or ModelA.name === "cctv" or ModelA.name.isNull
          or ModelA.seq > 1 or ModelA.name === "asdf" or ModelA.i < 0
        ) limit 3 offset 0 orderBy ModelA.name.desc


      Assert.assertEquals(1, q3.size)

      val q4 = select[ModelA] where ModelA.name === "cctv" or ModelA.seq === 1
      Assert.assertEquals(1, q4.size)
    }


    //update
    {
      Assert.assertEquals(0, select[ModelA] where ModelA.name === "fdsa" size)
      val num = update[ModelA] set (name="fdsa") where ModelA.name === "cctv" or ModelA.seq === 1  execute;
      Assert.assertEquals(1,num)
      Assert.assertEquals(1, select[ModelA] where ModelA.name === "fdsa" size)
    }
    //delete
    {
      Assert.assertEquals(2, select[ModelA].size)
      delete[ModelA] where ModelA.name === "fdsa" execute;
      Assert.assertEquals(1, select[ModelA].size)
    }

  }
}
