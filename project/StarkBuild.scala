import sbt.Keys._
import sbt._

object BuildSettings {
  val buildSettings = Seq(
  )
}

object Dependencies {
  lazy val tapestryVersion = "5.3.8"
  lazy val springVersion = "4.2.4.RELEASE"
  val scalaReflect = "org.scala-lang" % "scala-reflect" % _
  val tapestryIoc = "org.apache.tapestry" % "tapestry-ioc" % tapestryVersion
  val springOrm = "org.springframework" % "spring-orm" % springVersion
  val springContextSupport = "org.springframework" % "spring-context-support" % springVersion
  val aopalliance = "aopalliance" % "aopalliance" % "1.0"
  val hibernateEntityManager = "org.hibernate" % "hibernate-entitymanager" % "4.3.5.Final"
  val jooq = "org.jooq" % "jooq" % "3.7.3"
  val junit = "junit" % "junit" % "4.8.2" % "test"
  val h2 = "com.h2database" % "h2" % "1.3.176" % "test"


  //   val logbackVer = "0.9.16"
  //   val grizzlyVer = "1.9.19"

  //   val logbackcore    = "ch.qos.logback" % "logback-core"     % logbackVer
  //   val logbackclassic = "ch.qos.logback" % "logback-classic"  % logbackVer

  //   val jacksonjson = "org.codehaus.jackson" % "jackson-core-lgpl" % "1.7.2"

  //   val grizzlyframwork = "com.sun.grizzly" % "grizzly-framework" % grizzlyVer
  //   val grizzlyhttp     = "com.sun.grizzly" % "grizzly-http"      % grizzlyVer
  //   val grizzlyrcm      = "com.sun.grizzly" % "grizzly-rcm"       % grizzlyVer
  //   val grizzlyutils    = "com.sun.grizzly" % "grizzly-utils"     % grizzlyVer
  //   val grizzlyportunif = "com.sun.grizzly" % "grizzly-portunif"  % grizzlyVer

  //   val sleepycat = "com.sleepycat" % "je" % "4.0.92"

  //   val apachenet   = "commons-net"   % "commons-net"   % "2.0"
  //   val apachecodec = "commons-codec" % "commons-codec" % "1.4"

  //   val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
}

object StarkBuild extends Build {

  import BuildSettings._
  import Dependencies._

  lazy val ActiveRecordMacroDeps = (sv: String) => Seq(
    scalaReflect(sv)
  )
  lazy val ActiveRecordDeps = (sv: String) => Seq(
    tapestryIoc, springOrm, springContextSupport, aopalliance, hibernateEntityManager,jooq,
    junit,h2
  )
  lazy val root =
    Project("stark-project", file("."))
      .aggregate(ActiveRecordMacroProject, ActiveRecordProject)
      .settings(publishArtifact := false)
  lazy val ActiveRecordMacroProject = Project(
    "activerecord-macro",
    file("activerecord-macro"),
    settings = buildSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => ActiveRecordMacroDeps(sv) })
  )
  lazy val ActiveRecordProject = Project(
    "activerecord",
    file("activerecord"),
    settings = buildSettings ++ Seq(libraryDependencies <++= scalaVersion { sv => ActiveRecordDeps(sv) })

  ).dependsOn(ActiveRecordMacroProject)

}
