package reward.services

import reward.entities.TraceOrder.CommerceType
import reward.entities.User

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-04-21
  */
trait TraceOrderService {
  def getPid(user:User,
             coupon_amount:Int,
             itemid:Long,
             commerce_type:CommerceType.Type
            ):String
}
