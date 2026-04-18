// Copyright 2016 the original author or authors. All rights reserved.
// site: http://www.ganshane.com
package stark.migration

/**
 * Dameng is largely Oracle-compatible for the DDL this project emits,
 * so reuse the Oracle adapter behavior and only override the vendor.
 */
class DamengDatabaseAdapter(override val schemaNameOpt: Option[String])
    extends OracleDatabaseAdapter(schemaNameOpt) {
  override val vendor = Dameng
}
