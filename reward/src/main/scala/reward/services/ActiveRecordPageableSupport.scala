package reward.services

import org.springframework.data.domain.Pageable
import stark.activerecord.services.DSL.DSLSelectionQuery
import stark.activerecord.services.{DSL, QlRelation}

/**
  * 支持对数据分页的支持
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-06
  */
class ActiveRecordPageableSupport{
  /**
    * 使用spring的pageable对象对记录进行分页排序
    * @param records 记录
    * @param pageable 分页对象
    * @tparam T 操作的实体对象
    */
  protected def pageActiveRecordsByPageable[T](records:QlRelation[T],pageable: Pageable): List[T]={
    val result = records.offset(pageable.getPageNumber*pageable.getPageSize).limit(pageable.getPageSize)
    val it = pageable.getSort.iterator()
    var sorts=Seq[(String, String)]()
    while(it.hasNext){
      val sort = it.next
      val propertyName = snakeToCamel(sort.getProperty)
      if(sort.isAscending) sorts :+= (propertyName,"asc")
      else sorts :+= (propertyName,"desc")
    }
    result.order(sorts: _*).toList
  }
  protected  def pageActiveRecordsByPageable[T](records:DSLSelectionQuery[T,T], pageable: Pageable): List[T]={
    var result = records.offset(pageable.getPageNumber*pageable.getPageSize).limit(pageable.getPageSize)
    val it = pageable.getSort.iterator()

    while(it.hasNext){
      val sort = it.next
      val propertyName = snakeToCamel(sort.getProperty)
      if(sort.isAscending) result = result.orderBy(DSL.column(propertyName))
      else result = result.orderBy(DSL.column(propertyName).desc)
    }
    result.toList
  }
  private def snakeToCamel(name:String): String ={
    val sb = new StringBuilder
    var flag = false
    name.foreach(ch=>{
      if(ch == '_') flag = true
      else if(flag) {sb.append(ch.toUpper);flag=false}
      else sb.append(ch)
    })
    sb.toString()
  }
}
