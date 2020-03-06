package reward.services

import org.springframework.data.domain.Pageable
import stark.activerecord.services.QlRelation

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
object RewardUtils {
  /**
    * 使用spring的pageable对象对记录进行分页排序
    * @param records 记录
    * @param pageable 分页对象
    * @tparam T 操作的实体对象
    */
  def pageActiveRecordsByPageable[T](records:QlRelation[T],pageable: Pageable): List[T]={
    val result = records.offset(pageable.getPageNumber*pageable.getPageSize).limit(pageable.getPageSize)
    val it = pageable.getSort.iterator()
    var sorts=Seq[(String, String)]()
    while(it.hasNext){
      val sort = it.next
      if(sort.isAscending) sorts :+= (sort.getProperty,"asc")
      else sorts :+= (sort.getProperty,"desc")
    }
    result.order(sorts: _*).toList
  }
}
