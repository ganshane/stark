package reward.pages

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.junit.Test


/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-23
  */
class JdControllerTest {
  @Test
  def test_search: Unit ={
    val controller = new JdController
    controller.setObjectMapper(new ObjectMapper().registerModule(DefaultScalaModule))
    controller.search("女装",0,1,4)
  }
}
