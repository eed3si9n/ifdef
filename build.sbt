val scala212 = "2.12.18"
val scala213 = "2.13.12"
val scala3 = "3.4.0-RC1-bin-20231010-7dc9798-NIGHTLY"

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.eed3si9n.ifdef"
ThisBuild / scalaVersion := scala212

lazy val root = (project in file("."))
  .aggregate(plugin, macros)
  .settings(
    name := "ifdef root",
    publish / skip := true,
    crossScalaVersions := Nil,
  )

lazy val plugin = project
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-ifdef",
    scalaVersion := scala212,
    crossScalaVersions := List(scala212),
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
      }
    },
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.eed3si9n.ifdef.sbtifdef",
  )

lazy val macros = project
  .settings(
    name := "ifdef-macro",
    crossScalaVersions := List(scala213, scala212, scala3),
    Compile / scalacOptions ++= {
      if (scalaVersion.value.startsWith("2.13")) List("-Ymacro-annotations")
      else Nil
    },
    libraryDependencies ++= {
      if (scalaVersion.value.startsWith("2.")) List("org.scala-lang" % "scala-reflect" % scalaVersion.value)
      else Nil
    },
    libraryDependencies ++= {
      if (scalaVersion.value.startsWith("2.12.")) List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      else Nil
    },
  )

ThisBuild / organizationName := "eed3si9n"
ThisBuild / organizationHomepage := Some(url("http://eed3si9n.com/"))
ThisBuild / homepage := Some(url("https://github.com/eed3si9n/ifdef"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/eed3si9n/ifdef"), "git@github.com:eed3si9n/ifdef.git"))
ThisBuild / developers := List(
  Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n"))
)
ThisBuild / description := "@ifdef is an experimental macro that implements conditional compilation"
ThisBuild / licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
