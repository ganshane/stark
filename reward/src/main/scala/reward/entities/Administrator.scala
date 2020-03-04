package reward.entities

import javax.persistence._
import org.joda.time.DateTime
import stark.activerecord.services.ActiveRecord

/**
  * 管理员
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@Entity
@Table(name = "administrator")
class Administrator extends ActiveRecord{
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  var id:Int = _
  @Column
  var name:String= _
  @Column
  var password:String= _
  @Column
  var createdAt:DateTime = _
}


