package reward.services

import java.util.Collections

import org.joda.time.DateTime
import org.springframework.http.{HttpEntity, HttpHeaders, MediaType}
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import org.springframework.web.client.RestTemplate
import reward.entities.{OnlineUser, User}
import stark.activerecord.services.DSL._
import stark.utils.services.LoggerSupport

import scala.collection.JavaConversions._

/**
  * 用户服务类
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
@Service
class UserService extends LoggerSupport{
  private val restTemplate = new RestTemplate()
  private val LC_HEADERS=createHeaders
  private val LC_API_BASE_URL="https://leancloud.cn/1.1/"
  def verifySmsCode(phone:String,code: String)= {
    val json= Map("mobilePhoneNumber"->phone)
    val requestEntity = new HttpEntity[java.util.Map[String,Any]](json, LC_HEADERS)
    restTemplate.postForEntity(LC_API_BASE_URL + "verifySmsCode/" + code, requestEntity, classOf[Void])
  }
  def sendSmsCode(phone:String): Unit ={
    val json= Map("template"->"gofanli","mobilePhoneNumber"->phone)
    val requestEntity = new HttpEntity[java.util.Map[String,Any]](json, LC_HEADERS)
    restTemplate.postForEntity(LC_API_BASE_URL+"requestSmsCode",requestEntity,classOf[Void])
  }
  def generateToken(user:User):String={
    val keySource=user.phone+user.id+System.currentTimeMillis()
    DigestUtils.md5DigestAsHex(keySource.getBytes)
  }
  private def createHeaders={
    val requestHeaders = new HttpHeaders()
    requestHeaders.add("X-LC-Id","2s0KOFLTsO3t4NTt4zygN8tj-gzGzoHsz")
    requestHeaders.add("X-LC-Key", "3IBcP0QFKETbWiySCHKNHDNt")
    requestHeaders.setContentType(MediaType.APPLICATION_JSON)
    requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
    requestHeaders
  }

  /**
    * 定时删除过期用户
    */
  @Scheduled(fixedDelay = 5*60*1000)
  def deleteExpiredUser(): Unit = {
    logger.info("delete expired user")

    val num = delete[OnlineUser] where OnlineUser.expiredAt[DateTime] < DateTime.now() execute

    logger.info("finish to delete {} expired user",num)
  }

}
