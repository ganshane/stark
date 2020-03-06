package reward

import java.io.File
import java.net.InetAddress

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import javax.sql.DataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.{Bean, ComponentScan, Import, Lazy}
import org.springframework.util.FileSystemUtils
import reward.config.RewardConfig
import reward.pages.UserController
import reward.services.GlobalApiExceptionHandler
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.service._
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.swagger2.annotations.EnableSwagger2
import stark.activerecord.config.JpaProperty
import stark.activerecord.{StarkActiveRecordConstants, StarkActiveRecordModule}
import stark.migration.{DatabaseAdapter, InstallAllMigrations, Migrator, Vendor}
import stark.utils.services.StarkUtils

/**
  * reward application core modules
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-03-04
  */
@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackageClasses = {
  Array[Class[_]](classOf[UserController],classOf[GlobalApiExceptionHandler])
})
@Import(Array(classOf[StarkActiveRecordModule]))
class RewardModule {
  @Bean
  def buildRewardConfig(@Value(RewardConstants.SERVER_HOME) serverHome: String): RewardConfig={
    val config = new RewardConfig
    config.web.bind = "0.0.0.0:8080"

    val dbPath = "target/test.db"
    FileSystemUtils.deleteRecursively(new File(dbPath))
    config.db.url="jdbc:h2:file:"+dbPath+"/xx"
    config.db.user="public"
    config.db.driver = "org.h2.Driver"

    var jpaProperty = new JpaProperty
    jpaProperty.name = "hibernate.show_sql"
    jpaProperty.value="true"
    config.jpaProperties.add(jpaProperty)
    /*
    jpaProperty = new JpaProperty
    jpaProperty.name = "hibernate.hbm2ddl.auto"
    jpaProperty.value="create"
    config.jpaProperties.add(jpaProperty)
    */
    jpaProperty = new JpaProperty
    jpaProperty.name = "hibernate.physical_naming_strategy"
    jpaProperty.value="org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy"
    config.jpaProperties.add(jpaProperty)

    jpaProperty = new JpaProperty
    jpaProperty.name = StarkActiveRecordConstants.PACKAGE_SCAN_KEY
    jpaProperty.value="reward.entities"
    config.jpaProperties.add(jpaProperty)

    jpaProperty = new JpaProperty
    jpaProperty.name = "jadira.usertype.autoRegisterUserTypes"
    jpaProperty.value="true"

    config.jpaProperties.add(jpaProperty)

    config
  }
  @Bean
  def buildWebServerFactoryCustomizer(config:RewardConfig):WebServerFactoryCustomizer[ConfigurableServletWebServerFactory]= new WebServerFactoryCustomizer[ConfigurableServletWebServerFactory]{
    override def customize(factory: ConfigurableServletWebServerFactory): Unit = {
      val bindTuple = StarkUtils.parseBind(config.web.bind)
      factory.setAddress(InetAddress.getByName(bindTuple._1))
      factory.setPort(bindTuple._2)
    }
  }
  @Bean
  @Lazy
  def buildDataSource(config:RewardConfig): DataSource ={
    val hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(config.db.driver)
    hikariConfig.setJdbcUrl(config.db.url)
    hikariConfig.setUsername(config.db.user)
    hikariConfig.setPassword(config.db.password)
    //设置自动提交事务
    hikariConfig.setAutoCommit(false)

    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    hikariConfig.setMaximumPoolSize(15)


    val dataSource = new HikariDataSource(hikariConfig)

    //升级数据库
    val driverClassName: String = config.db.driver
    val vendor = Vendor.forDriver(driverClassName)
    val databaseAdapter = DatabaseAdapter.forVendor(vendor, Option(config.db.user))
    val migrator = new Migrator(dataSource, databaseAdapter)
    migrator.migrate(InstallAllMigrations, "reward.migrations", searchSubPackages = false)

    dataSource
  }
  @Bean
  def buildScalaJackson: Module ={
    DefaultScalaModule
  }

  import java.util

  import org.springframework.context.annotation.Bean
  import springfox.documentation.spi.DocumentationType
  import springfox.documentation.spring.web.plugins.Docket

  @Bean
  def userApi: Docket = {
    new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo)
      .select().paths(paths).build()
      .securitySchemes(util.Arrays.asList(apiKey()))
//      .securityContexts(util.Arrays.asList(securityContext()))
//    .securitySchemes(securitySchemes)
  }

  import com.google.common.base.Predicates
  import springfox.documentation.builders.PathSelectors

  //不显示错误的API
  private def paths = Predicates.not(PathSelectors.regex("/error.*"))
  private def apiInfo={
    new ApiInfoBuilder()
      .title("淘分享API")
      .description("淘分享API，注意查看API文档说明")
      .contact(new Contact("jcai","http://www.ganshane.com","jcai AT Ganshane dot com"))
      .build()
  }
  private def apiKey()= {
    new ApiKey(RewardConstants.GLOBAL_AUTH, RewardConstants.GLOBAL_AUTH, "header");
  }

  private def securityContext()= {
    SecurityContext.builder().securityReferences(defaultAuth())
//      .forPaths(PathSelectors.regex("^(?!auth).*$"))
      .build()
  }

  private def defaultAuth()= {
    val authorizationScope = new AuthorizationScope(
      "global", "accessEverything");
    util.Arrays.asList(new SecurityReference(RewardConstants.GLOBAL_AUTH,Array(authorizationScope)))
  }



  private def securitySchemes = util.Arrays.asList(new BasicAuth("Authorization"))
}

