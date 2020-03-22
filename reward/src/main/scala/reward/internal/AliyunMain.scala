package reward.internal

import java.util.concurrent.TimeUnit

import com.aliyun.oss.OSSClientBuilder
import com.aliyuncs.auth.sts.AssumeRoleRequest
import com.aliyuncs.http.MethodType

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-16
  */
object AliyunMain {
  def main(args: Array[String]): Unit = {
    import com.aliyuncs.DefaultAcsClient
    import com.aliyuncs.exceptions.ClientException
    import com.aliyuncs.profile.DefaultProfile
    val endpoint = "sts.aliyuncs.com"
    val accessKeyId = "LTAI4Fx8NYk2KYnBLhuTzDe5"
    val accessKeySecret = "TQHWw3acq0rZ1mu96wM9WH9o3MtAK6"
    val roleArn = "acs:ram::1161114981954871:role/taofenxiang-oss"
    val roleSessionName = "session-name"
    val policy =
      """
        |{
        |    "Statement": [
        |        {
        |            "Action": [
        |                "oss:PutObject"
        |            ],
        |            "Effect": "Allow",
        |            "Resource": [
        |                "acs:oss:*:*:taofenxiang/2020/*"
        |            ]
        |        }
        |    ],
        |    "Version": "1"
        |}
      """.stripMargin
    try { // 构造default profile（参数留空，无需添加region ID）
      val profile = DefaultProfile.getProfile("", accessKeyId, accessKeySecret)
      // 用profile构造client
      val client = new DefaultAcsClient(profile)
      val request = new AssumeRoleRequest
      request.setSysEndpoint(endpoint)
      request.setSysMethod(MethodType.POST)
      request.setRoleArn(roleArn)
      request.setRoleSessionName(roleSessionName)
//      request.setPolicy(policy)
      request.setDurationSeconds(TimeUnit.MINUTES.toSeconds(15))
      val response = client.getAcsResponse(request)
      System.out.println("Expiration: " + response.getCredentials.getExpiration)
      System.out.println("Access Key Id: " + response.getCredentials.getAccessKeyId)
      System.out.println("Access Key Secret: " + response.getCredentials.getAccessKeySecret)
      System.out.println("Security Token: " + response.getCredentials.getSecurityToken)
      System.out.println("RequestId: " + response.getRequestId)

      val ossEndpoint = "http://oss-cn-qingdao.aliyuncs.com"

      // init
      val ossClient = new OSSClientBuilder().build(ossEndpoint, response.getCredentials.getAccessKeyId,response.getCredentials.getAccessKeySecret,response.getCredentials.getSecurityToken)

      import java.io.ByteArrayInputStream
      val content = "Hello OSS"
      val putResponse = ossClient.putObject("taofenxiang", "2020/mytest1.txt", new ByteArrayInputStream(content.getBytes))
      println(putResponse)
    } catch {
      case e: ClientException =>
        System.out.println("Failed：")
        System.out.println("Error code: " + e.getErrCode)
        System.out.println("Error message: " + e.getErrMsg)
        System.out.println("RequestId: " + e.getRequestId)
    }
  }
}
