import sbt.Keys._
import sbt._

object BuildSettings {
  val buildSettings = Seq(
  )
}

object Dependencies {
  lazy val tapestryVersion = "5.3.8"
  lazy val springVersion = "4.2.4.RELEASE"


  lazy val scalaReflect          = "org.scala-lang"          % "scala-reflect"          % _
  lazy val tapestryIoc           = "org.apache.tapestry"     % "tapestry-ioc"           % tapestryVersion
  lazy val springOrm             = "org.springframework"     % "spring-orm"             % springVersion
  lazy val springContextSupport  = "org.springframework"     % "spring-context-support" % springVersion
  lazy val springJdbc            = "org.springframework"     % "spring-jdbc"            % springVersion
  lazy val aopalliance           = "aopalliance"             % "aopalliance"            % "1.0"
  lazy val hibernateEntityManager= "org.hibernate"           % "hibernate-entitymanager"% "4.3.5.Final"
  lazy val hibernateTools        = "org.hibernate"           % "hibernate-tools"        % "4.3.1.Final"
  lazy val junit                 = "junit"                   % "junit"                  % "4.11"           % "test"
  lazy val h2                    = "com.h2database"          % "h2"                     % "1.3.176"        % "test"
  lazy val h2Runtime             = "com.h2database"          % "h2"                     % "1.3.176"        % "runtime"
  lazy val slf4jApi              = "org.slf4j"               % "slf4j-api"              % "1.7.5"
  lazy val log4jdbc              = "com.googlecode.log4jdbc" % "log4jdbc"               % "1.2"
  lazy val jmock                 = "org.jmock"               % "jmock-junit4"           % "2.5.1"          % "test"
  lazy val hamcrest              = "org.hamcrest"            % "hamcrest-core"          % "1.3"            % "test"
  lazy val scalacheck            = "org.scalacheck"          %% "scalacheck"            % "1.12.0"         %"test"
  lazy val scalatest             = "org.scalatest"           %% "scalatest"             % "2.2.6"          %"test"
  lazy val junitInterface        = "com.novocode"            % "junit-interface"        % "0.11"           % "test"
}

object StarkBuild extends Build {

  import BuildSettings._
  import Dependencies._

  lazy val ActiveRecordDeps = (sv: String) => Seq(
    tapestryIoc, springOrm, springContextSupport, aopalliance, hibernateEntityManager,
    junit,h2,junitInterface
  )
  lazy val ActiveRecordMacroDeps = (sv: String) => Seq(
    scalaReflect(sv)
  )
  lazy val ActiveRecordGeneratorDeps = (sv: String) => Seq(
    hibernateTools,springJdbc,slf4jApi,h2Runtime,junitInterface
  )
  lazy val MigrationDeps=(sv:String)=>Seq(
    log4jdbc,h2,jmock,hamcrest,junitInterface
  )
  lazy val root =
    Project("stark-project", file("."))
      .aggregate(ActiveRecordMacroProject, ActiveRecordProject,ActiveRecordGeneratorProject,MigrationProject)
      .settings(publishArtifact := false)
  lazy val ActiveRecordProject = Project(
    "activerecord",
    file("activerecord"),
    settings = buildSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => ActiveRecordDeps(sv) })

  ).dependsOn(ActiveRecordMacroProject)

  lazy val ActiveRecordMacroProject = Project(
    "activerecord-macro",
    file("activerecord-macro"),
    settings = buildSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => ActiveRecordMacroDeps(sv) })
  )

  lazy val ActiveRecordGeneratorProject = Project(
    "activerecord-generator",
    file("activerecord-generator"),
    settings = buildSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => ActiveRecordGeneratorDeps(sv) })
  )
  lazy val MigrationProject= Project(
    "migration",
    file("migration"),
    settings = buildSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => MigrationDeps(sv) })
  )

}
