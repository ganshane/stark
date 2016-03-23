package stark.activerecord.generator

import java.io.File
import javax.sql.DataSource

import org.apache.tools.ant.Project
import org.apache.tools.ant.types.Environment.Variable
import org.apache.tools.ant.types.Path
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
import org.hibernate.mapping.Property
import org.hibernate.tool.ant.{GenericExporterTask, HibernateToolTask}
import org.hibernate.tool.hbm2x.POJOExporter
import org.hibernate.tool.hbm2x.pojo.ImportContext
import org.springframework.jdbc.datasource.DriverManagerDataSource

/**
 * activerecord generator
 *
 * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
 * @since 2016-01-05
 */
object ActiverecordGenerator {
  val configTimeDataSourceHolder  = new ThreadLocal[DataSource]
  def main(args:Array[String]): Unit ={
    val Array(url,user,password,packageName)=args

    val ds = new DriverManagerDataSource(url,user,password)
    configTimeDataSourceHolder.set(ds)

    val task = new HibernateToolTask
    task.setDestDir(new File("target/dest"))
    val project = new Project
    task.setProject(project)
    task.setTemplatePath(new Path(project, "template"))

    val exporter =  task.createHbmTemplate().asInstanceOf[GenericExporterTask]
    exporter.setExporterClass(classOf[ActiverecordExporter].getName)
    exporter.setTemplate("template/Pojo.ftl")
    var jpaProperty =  new Variable
    jpaProperty.setKey("ejb3")
    jpaProperty.setValue("true")
    task.addConfiguredProperty(jpaProperty)
    jpaProperty =  new Variable
    jpaProperty.setKey("field-description")
    jpaProperty.setValue("true")
    task.addConfiguredProperty(jpaProperty)

    exporter.setFilePattern("{package-name}/{class-name}.scala")

    val config = task.createJDBCConfiguration()
    config.setPackageName(packageName)
    //config.setPropertyFile(new File("hall-orm-generator/src/test/resources/hibernate.properties"))


    task.execute()
  }
}
class InMemoryDataSourceProvider extends DatasourceConnectionProviderImpl{
  setDataSource(ActiverecordGenerator.configTimeDataSourceHolder.get)
}
class ActiverecordExporter extends POJOExporter{
  override def init(): Unit = {
    super.init()
  }
  def getJavaTypeName (p: Property, useGenerics: Boolean) :String ={
    getCfg2JavaTool.getJavaTypeName(p,useGenerics).trim match{
      case "int" => "Int"
      case "long" => "Long"
      case "short" => "Short"
      case other => other
    }
  }

  def asParameterList(fields: java.util.List[_], useGenerics: Boolean, ic: ImportContext): String = {
    return asParameterList(fields.iterator, useGenerics, ic)
  }

  def asParameterList(fields: java.util.Iterator[_], useGenerics: Boolean, ic: ImportContext): String = {
    val buf: StringBuffer = new StringBuffer
    while (fields.hasNext) {
      val field: Property = fields.next.asInstanceOf[Property]
      buf.append(field.getName).append(":").append(getJavaTypeName(field, useGenerics))
      if (fields.hasNext) {
        buf.append(", ")
      }
    }
    return buf.toString
  }
}
