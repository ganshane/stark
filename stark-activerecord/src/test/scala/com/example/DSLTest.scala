package com.example

import org.junit.{Assert, Test}
import stark.activerecord.services.DSL._
import stark.activerecord.{ModelA, BaseActiveRecordTestCase}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2016-03-14
  */
class DSLTest extends BaseActiveRecordTestCase{
  @Test
  def test_delete: Unit = {
    ModelA.delete.execute
  }
  @Test
  def test_select: Unit = {
    val modelA = new ModelA
    modelA.name = "cctv"
    modelA.save
    val modelA2 = new ModelA
    modelA2.name = "asdf"
    modelA2.save;
    val modelA3 = new ModelA
    modelA3.name = "asdf"
    modelA3.save;

    /*
    val q1 = select[ModelA](ModelA.name,ModelA.seq) orderBy  ModelA.name
    Assert.assertEquals(3, q1.size)
    */

    val q = select[ModelA](ModelA.name.distinct) where ModelA.name === "asdf";
    val r = q.toList
    Assert.assertEquals(1,q.size)
    Assert.assertEquals(1,q.count)
    val count = select[ModelA](ModelA.name.count) ;
    Assert.assertEquals(3, count.head.asInstanceOf[Long])

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
          or ModelA.seq > 1 or ModelA.name === "asdf"
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
