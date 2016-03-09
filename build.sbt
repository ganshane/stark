organization in ThisBuild          := "com.ganshane.stark"

version in ThisBuild               := "0.1"

homepage in ThisBuild              := Some(url("http://www.ganshane.com"))

licenses in ThisBuild              += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

startYear in ThisBuild             := Some(2016)

organizationName in ThisBuild      := "Ganshane Network"

scalaVersion in ThisBuild          := "2.11.7"

crossScalaVersions in ThisBuild    := Seq("2.11.7")

// Settings for Sonatype compliance
pomIncludeRepository in ThisBuild  := { _ => false }

publishTo in ThisBuild            <<= isSnapshot(if (_) Some(Opts.resolver.sonatypeSnapshots) else Some(Opts.resolver.sonatypeStaging))

scmInfo in ThisBuild               := Some(ScmInfo(url("https://github.com/ganshane/stark"), "scm:git:https://github.com/ganshane/stark.git"))

pomExtra in ThisBuild              :=  Developers.toXml

resolvers  in ThisBuild           ++= Seq(
  "snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"      at "https://oss.sonatype.org/content/repositories/releases"
)
