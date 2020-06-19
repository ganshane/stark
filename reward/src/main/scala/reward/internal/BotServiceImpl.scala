package reward.internal

import com.typesafe.scalalogging.LazyLogging
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reward.services.TaobaoService
import wechaty.puppet.schemas.Message.MessageType
import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload
import wechaty.puppet.schemas.Puppet
import wechaty.user.{MiniProgram, Room}
import wechaty.{Wechaty, WechatyOptions}

/**
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2020-06-19
 */
@Service
class BotServiceImpl extends LazyLogging{
  @Autowired
  private val taobaoService:TaobaoService = null
  val mpJson="{\"appid\":\"wx7c54357940c1d76a\",\"description\":\"\",\"iconUrl\":\"http://mmbiz.qpic.cn/mmbiz_png/lQLdvqlFfCYNMSG6vEPoFNGPvIES1mwEOibI5fCH2p0hS7S9L6xvSOppdQByj3u6764j3nRrMFSfcyyibV9JKouw/640?wx_fmt=pngwxfrom=200\",\"pagePath\":\"pages/detail/index.html?item_id=%s&pid=65&commerce_type=%s\",\"shareId\":\"20a3b1336c2807932df41a0193618e04\",\"thumbKey\":\"0f13237ce8339c569330119c049e4736\",\"thumbUrl\":\"304f02010004483046020100020448529f2302033d0af802046c34feb602045eeb85ab0421777875706c6f61645f66696c6568656c7065723630345f313539323439333438330204010400030201000400\",\"title\":\"点击领取优惠券\",\"username\":\"gh_1c0dae5a5b83@app\"}"

  @PostConstruct
  def init(): Unit ={
    val option = new WechatyOptions
    implicit val bot = Wechaty.instance(option)
    bot
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
        println(message)
        val ourRoomId = "905154731@chatroom"
        if(message.room.isDefined && message.room.get.id == ourRoomId) {
          if(message.`type` == MessageType.Text){
            val room = message.room.get
            val itemIdOpt = taobaoService.tryParserTkl(message.text)
            itemIdOpt match{
              case Some(itemId) =>
                val json = mpJson.format(itemId,1)
                logger.debug(s"json :${json}")
                val miniProgramPayload = Puppet.objectMapper.readValue(json,classOf[MiniProgramPayload])
                val miniProgram = new MiniProgram(miniProgramPayload)
                room.say(miniProgram)
              case _ =>

            }
          }
        }
    })

    bot.start()
    println("bot started done!!")
  }
}
