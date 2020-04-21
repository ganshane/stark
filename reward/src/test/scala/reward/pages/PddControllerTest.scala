package reward.pages

import org.junit.Test

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-25
  */
class PddControllerTest {
  @Test
  def test_opts: Unit ={
    val controller = new PddController
    controller.createPromotionId
  }
}
