package reward.pages

import org.junit.Test
import org.springframework.web.client.RestTemplate

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-25
  */
class PddControllerTest {
  @Test
  def test_opts: Unit ={
    val MIAYOUQUAN_KEY:String ="a781b1d1-60d0-f570-dfea-64f02c7f834b"
    val JD_CONNECT_KEY:String ="J4672635259984466"
    val rest = new RestTemplate()
    Range(1,2000).foreach{case i=>
      val r=rest.getForObject("http://api.web.21ds.cn/jingdong/createUnionPosition?apkey="+MIAYOUQUAN_KEY+"&key_id="+JD_CONNECT_KEY+"&unionType=1&type=3&siteId=223314&spaceNameList=B"+i,classOf[String])
        println(r)
    }
  }
}
