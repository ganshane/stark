import sbt.Keys._
import sbt._

object BuildSettings {
  val buildSettings = Seq (
  )
}
object Dependencies {
  val scalaReflect = "org.scala-lang" % "scala-reflect" % _
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

  val commonDeps = Seq (
    //   logbackcore,
    //   logbackclassic,
    //   jacksonjson,
    //   scalatest
  )
  val starkOrmMacroDeps =  Seq(
    scalaReflect
  )


  lazy val root =
    Project("stark-project", file("."))
      .aggregate(starkOrmMacro)
      .settings(publishArtifact := false)


  lazy val starkOrmMacro= Project (
    "stark-orm-macro",
    file("stark-orm-macro"),
    settings = buildSettings ++ Seq (libraryDependencies <++= scalaVersion {sv =>Seq(scalaReflect(sv))})
  )
}