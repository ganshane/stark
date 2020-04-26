package reward.entities

import javax.persistence.{Embeddable, EmbeddedId, Entity, Table}
import org.joda.time.DateTime
import stark.activerecord.services.{ActiveRecord, ActiveRecordInstance}

/**
  * 京东订单同步数据
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-19
  */
@Entity
@Table(name = "jd_order_info")
class JdOrder extends ActiveRecord{
  @EmbeddedId
  var tradeId:JdOrderInfoPK = _
  var finishTime:DateTime= _
  var orderTime:DateTime= _

  var actualCosPrice :Int = _
  var actualFee :Int = _
  var commissionRate:Int = _
  var estimateCosPrice :Int = _
  var estimateFee :Int = _
  var finalRate :Int = _
  var cid1:Long = _
  var frozenSkuNum:Long = _
  var pid :String = _
  var positionId :Long = _
  var price :Int = _
  var cid2 :Long = _
  var siteId :Long = _

  var skuName :String = _
  var skuNum :Long = _
  var skuReturnNum :Long = _
  var subSideRate :Int = _
  var subsidyRate :Int = _
  var cid3 :Long = _
  var unionAlias :String = _
  var unionTag :String = _
  var unionTrafficGroup :String = _
  var validCode :String = _
  var subUnionId :String = _
  var traceType :String = _
  var payMonth :String = _
  var popId :Long = _
  var ext1 :String = _
  var cpActId :Long = _
  var unionRole :String = _
  var giftCouponKey :String = _
  var giftCouponOcsAmount :Int = _

  var updatedAt:DateTime = _
}
object JdOrder extends ActiveRecordInstance[JdOrder]
@Embeddable
class JdOrderInfoPK extends Serializable {
  var orderId:Long = _
  var skuId:Long = _
  def this(orderId:Long,skuId:Long){
    this()
    this.orderId = orderId
    this.skuId = skuId
  }
  override def hashCode(): Int = {
    (orderId + skuId).hashCode()
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case objPk: JdOrderInfoPK =>
        return objPk.orderId == orderId && objPk.skuId == skuId
      case _ =>
    }
    false
  }
}