package reward.internal

import com.typesafe.scalalogging.LazyLogging
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reward.services.TaobaoService
import wechaty.plugins.{RoomConnector, RoomConnectorConfig, RoomJoinHello, RoomJoinHelloConfig}
import wechaty.puppet.schemas.Message.MessageType
import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload
import wechaty.puppet.schemas.Puppet
import wechaty.user.{Conversation, MiniProgram, Room}
import wechaty.{Wechaty, WechatyOptions}

import scala.io.Source

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2020-06-19
 */
@Service
class BotServiceImpl extends LazyLogging{
  @Autowired
  private val taobaoService:TaobaoService = null
  val weixin="jufuteaship"
  val mpJson="{\"appid\":\"wx7c54357940c1d76a\",\"description\":\"Wechaty优惠券\",\"iconUrl\":\"http://mmbiz.qpic.cn/mmbiz_png/lQLdvqlFfCYNMSG6vEPoFNGPvIES1mwEOibI5fCH2p0hS7S9L6xvSOppdQByj3u6764j3nRrMFSfcyyibV9JKouw/640?wx_fmt=pngwxfrom=200\",\"pagePath\":\"pages/detail/index.html?item_id=%s&pid=65&commerce_type=%s\",\"shareId\":\"20a3b1336c2807932df41a0193618e04\",\"thumbKey\":\"46f45b1a2cdcf21998b2c06987bb1893\",\"thumbUrl\":\"305902010004523050020100020448529f2302033d0af802047034feb602045eecd4a2042b777875706c6f61645f3930353135343733314063686174726f6f6d32383631355f313539323537393233330204010400030201000400\",\"title\":\"点击领取抵扣券抢购\",\"username\":\"gh_1c0dae5a5b83@app\"}"
  val myOrderJson="{\"appid\":\"wx7c54357940c1d76a\",\"description\":\"\",\"iconUrl\":\"http://mmbiz.qpic.cn/mmbiz_png/lQLdvqlFfCYNMSG6vEPoFNGPvIES1mwEOibI5fCH2p0hS7S9L6xvSOppdQByj3u6764j3nRrMFSfcyyibV9JKouw/640?wx_fmt=pngwxfrom=200\",\"pagePath\":\"pages/me/order.html?1=1&pid=65\",\"shareId\":\"20a3b1336c2807932df41a0193618e04\",\"thumbKey\":\"8b5f2b32c45e7f9ca9171574f9b66bb9\",\"thumbUrl\":\"305902010004523050020100020448529f2302033d0af802046734feb602045eed5a53042b777875706c6f61645f3930353135343733314063686174726f6f6d32383633385f313539323631333435390204010400030201000400\",\"title\":\"[狐淘] 我的订单\",\"username\":\"gh_1c0dae5a5b83@app\"}"
  val withDrawJson="{\"appid\":\"wx7c54357940c1d76a\",\"description\":\"\",\"iconUrl\":\"http://mmbiz.qpic.cn/mmbiz_png/lQLdvqlFfCYNMSG6vEPoFNGPvIES1mwEOibI5fCH2p0hS7S9L6xvSOppdQByj3u6764j3nRrMFSfcyyibV9JKouw/640?wx_fmt=pngwxfrom=200\",\"pagePath\":\"pages/me/withdraw.html?1=1&pid=65\",\"shareId\":\"20a3b1336c2807932df41a0193618e04\",\"thumbKey\":\"56099755f7f9d6678ed849049ebc3f9f\",\"thumbUrl\":\"305902010004523050020100020448529f2302033d0af802046d34feb602045eed5be1042b777875706c6f61645f3930353135343733314063686174726f6f6d32383633395f313539323631333835370204010400030201000400\",\"title\":\"[狐淘] 提现中心\",\"username\":\"gh_1c0dae5a5b83@app\"}"

  @PostConstruct
  def init(): Unit ={
    val option = new WechatyOptions
    implicit val bot = Wechaty.instance(option)
    //          (message.room.get.id =="21822642010@chatroom" && message.from.id == "baihe646666")
    val hello=Source.fromInputStream(getClass.getResourceAsStream("/hello.txt")).getLines().mkString("")
    val members = Array("wxid_q7mg8ubtjwtx22", "wxid_q1l3js8xkshf52", "wxid_80e9yn3mzhgj22") //wxid_f2utv94ag90t12
    bot
      .use(new RoomJoinHello(RoomJoinHelloConfig(Array("18911721443@chatroom","17203004929@chatroom"),hello=hello)))
      .use(new RoomConnector(RoomConnectorConfig(
      from=Array("21822642010@chatroom"), //(21822642010@chatroom,【进宝联盟】6⃣ 拼多多爆款肉单)
//      from=Array("905154731@chatroom"),
//      to=Array("905154731@chatroom","17203004929@chatroom"),//(905154731@chatroom,$20,000,000,000)
      to=Array("17203004929@chatroom","18911721443@chatroom"),//(905154731@chatroom,$20,000,000,000)
      mapper = (_,msg,toRoom) => {
//        println(msg)
        if (!members.contains(msg.from.id)) {
          logger.warn(s"member[${msg.from.id}] not in members list ${members.toList}")
          return None
        }
        if (msg.`type` == MessageType.Text) {
          if (msg.text.matches("\\d+")) {
            sendMPItem(toRoom, msg.text, 3)
            None
          } else if (msg.text.contains("pinduoduo.com")) { //链接
            None
          } else Some(msg)
        } else Some(msg)
      }
    )))
      .onScan(payload => {
        println("Scan QR Code to login: %s\nhttps://api.qrserver.com/v1/create-qr-code/?data=%s\n".format(payload.status, payload.qrcode))
      })
      .onLogin(payload => {
        println("User %s logined\n".format(payload.id))
        Room.findAll().foreach(x=>{
          println(x.id,x.payload.topic)
        })
      })
      .onMessage(message=>{
        val ourRoomIds = Array("905154731@chatroom","18911721443@chatroom","17203004929@chatroom")
        var itemIdOpt:Option[String] = None
        var commerceType = 0
        if(
          (message.room.isDefined && ourRoomIds.contains(message.room.get.id))
          || message.room.isEmpty
        ) {
          if(message.`type` == MessageType.Text){
            itemIdOpt = taobaoService.tryParserTkl(message.text)
            commerceType = 1
            if(itemIdOpt.isEmpty ) { //&& message.room.isDefined){
              val jsonOpt = message.text match{
                case "提现" => Some(withDrawJson)
                case "订单" => Some(myOrderJson)
                case _ => None
              }
              if(jsonOpt.isDefined) {
                val miniProgramPayload = Puppet.objectMapper.readValue(jsonOpt.get, classOf[MiniProgramPayload])
                val miniProgram = new MiniProgram(miniProgramPayload)
                message.say(miniProgram)
              }
            }
          }else if(message.`type` == MessageType.Url){
            val urlLink = message.toUrlLink()
            val url = urlLink.payload.url
            logger.debug("urlLink=>{}",url)
            if(url.indexOf(".jd.com")>0){//jd
              JD_ITEMID_REG.findFirstMatchIn(url) match{
                case Some(matcher) =>
                  itemIdOpt = Some(matcher.group(1))
                  commerceType = 2
                case _ =>
              }
            }else if(url.indexOf("yangkeduo.com")>0){ //pdd
              PDD_ITEM_REG.findFirstMatchIn(url) match{
                case Some(matcher) =>
                  itemIdOpt = Some(matcher.group(1))
                  commerceType = 3
                case _ =>
              }
            }
          }
          itemIdOpt match{
            case Some(itemId) =>
              sendMPItem(message.conversation,itemId,commerceType)
            case _ =>
              logger.debug("itemid not found {}",message)
          }
        }
      })

    bot.start()
//    bot.logout()
    println("bot started done!!")
  }
  private def sendMPItem(conversation:Conversation,itemId:String,commerceType:Int): Unit ={
    val json = mpJson.format(itemId,commerceType)
    logger.debug(s"json :${json}")
    val miniProgramPayload = Puppet.objectMapper.readValue(json,classOf[MiniProgramPayload])
    miniProgramPayload.description="Wechaty优惠券助手"
    val miniProgram = new MiniProgram(miniProgramPayload)
    conversation.say(miniProgram)
  }
  private val JD_ITEMID_REG="/(\\d+)\\.html".r
  private val PDD_ITEM_REG="goods_id=(\\d+)".r
}