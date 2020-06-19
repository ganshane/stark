package reward.internal

import org.junit.Test


/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2020-06-19
 */
class TaobaoServiceImplTest {
  @Test
  def test_tkl: Unit ={
    val service = new TaobaoServiceImpl
    val value=
      """
        |緮置本段内容₤2kdH1vaUqJ4₤咑幵👉τa0寳👈【网红时尚职业两件套装女夏季短袖t恤高腰条纹开叉中长款半身裙子】
        |""".stripMargin
    println(service.tryParserTkl(value))
  }
}
