package reward.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Transient
import org.joda.time.DateTime

/**
  * 统一的订单接口
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-28
  */
trait CommerceOrder extends CommerceOrderStatusSupport{
  @Transient
  @JsonIgnore
  def getTradeId:Long
  @Transient
  @JsonIgnore
  def toCommercePK:CommerceOrderPK
  @Transient
  @JsonIgnore
  def getCommission:Int
  @Transient
  @JsonIgnore
  def getEstimateCommission:Int
  @Transient
  @JsonIgnore
  def getClickTime:DateTime
}
