val scala212 = "2.12.18"
val scala213 = "2.13.12"
val scala3 = "3.3.0"

ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / organization := "com.eed3si9n.ifdef"
ThisBuild / scalaVersion := scala213
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .aggregate(plugin, macros, `compiler-plugin`)
  .settings(
    name := "ifdef root",
    publish / skip := true,
    crossScalaVersions := Nil,
  )

lazy val annotation = project
  .settings(
    name := "ifdef-annotation",
    scalaVersion := scala213,
    crossScalaVersions := List(scala212, scala213, scala3),
  )

lazy val `compiler-plugin` = project
  .settings(
    name := "ifdef-plugin",
    crossScalaVersions := List(scala212, scala213, scala3),
    libraryDependencies ++= {
      val sv = scalaVersion.value
      if (scalaVersion.value.startsWith("3.")) List("org.scala-lang" %% "scala3-compiler" % sv % Provided)
      else List("org.scala-lang" % "scala-compiler" % sv % Provided)
    },
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
    scriptedBufferLog := false,
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.eed3si9n.ifdef.sbtifdef",
  )

lazy val macros = project
  .settings(
    name := "ifdef-macro",
    crossScalaVersions := List(scala213, scala212),
    Compile / scalacOptions ++= {
      if (scalaVersion.value.startsWith("2.13")) List("-Ymacro-annotations")
      else if (scalaVersion.value.startsWith("3.")) List("-Xcheck-macros", "-Ycheck:all")
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
ThisBuild / githubWorkflowPublishTargetBranches := Nil
ThisBuild / githubWorkflowBuildSbtStepPreamble := List("-v")
ThisBuild / githubWorkflowBuild := List(
  WorkflowStep.Sbt(List("+publishLocal")),
  WorkflowStep.Sbt(List("test", "scripted")),
)
