organization in ThisBuild          := "com.ganshane.stark"

version in ThisBuild               := "0.2-SNAPSHOT"

homepage in ThisBuild              := Some(url("http://www.ganshane.com"))

licenses in ThisBuild              += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

startYear in ThisBuild             := Some(2016)

organizationName in ThisBuild      := "Ganshane Network"

scalaVersion in ThisBuild          := "2.11.7"

crossScalaVersions in ThisBuild    := Seq("2.11.7")

// Settings for Sonatype compliance
pomIncludeRepository in ThisBuild  := { _ => false }

publishTo in ThisBuild            <<= isSnapshot(if (_) Some(Opts.resolver.mavenLocalFile) else Some(Opts.resolver.sonatypeStaging))

scmInfo in ThisBuild               := Some(ScmInfo(url("https://github.com/ganshane/stark"), "scm:git:https://github.com/ganshane/stark.git"))

pomExtra in ThisBuild              :=  Developers.toXml

/*
resolvers  in ThisBuild           ++= Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "releases"      at "http://central.maven.org/maven2",
  "snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots"
)
*/
resolvers in ThisBuild += Resolver.mavenLocal


