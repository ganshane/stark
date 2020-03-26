package reward.pages

import com.pdd.pop.sdk.common.util.JsonUtil
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
    val response = controller.promotion("96633572616","1b4984ae75dc54c5a9b1cc16ea0353d4d39")
    System.out.println(JsonUtil.transferToJson(response))
  }
}
