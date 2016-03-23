package stark.activerecord.internal

import org.junit.Test
import stark.activerecord.{ModelA, BaseActiveRecordTestCase}
import stark.activerecord.services.EntityService

/**
 * implements EntityService
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-03
 */
class EntityServiceImplTest extends BaseActiveRecordTestCase{
  @Test
  def test_save: Unit ={
    val entityService = getService[EntityService]
    val modelA = new ModelA
    entityService.save(modelA)
  }
}
