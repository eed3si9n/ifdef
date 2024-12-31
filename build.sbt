val scala212 = "2.12.19"
val scala213 = "2.13.14"
val scala3 = "3.3.3"
val scala3_sbt2 = "3.6.2"

ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / organization := "com.eed3si9n.ifdef"
ThisBuild / scalaVersion := scala213
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .aggregate(annotation.projectRefs ++
    `compiler-plugin`.projectRefs ++
    macros.projectRefs ++
    plugin.projectRefs:_*)
  .settings(
    name := "ifdef root",
    publish / skip := true,
    crossScalaVersions := Nil,
  )

lazy val annotation = (projectMatrix in file("annotation"))
  .settings(
    name := "ifdef-annotation",
  )
  .jvmPlatform(scalaVersions = Seq(scala212, scala213, scala3))
  .jsPlatform(scalaVersions = Seq(scala212, scala213, scala3))
  .nativePlatform(scalaVersions = Seq(scala212, scala213, scala3))

lazy val `compiler-plugin` = (projectMatrix in file("compiler-plugin"))
  .settings(
    name := "ifdef-plugin",
    libraryDependencies ++= {
      val sv = scalaVersion.value
      if (scalaVersion.value.startsWith("3.")) List("org.scala-lang" %% "scala3-compiler" % sv % Provided)
      else List("org.scala-lang" % "scala-compiler" % sv % Provided)
    },
  )
  .jvmPlatform(scalaVersions = Seq(scala212, scala213, scala3))

lazy val plugin = (projectMatrix in file("plugin"))
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .settings(
    name := "sbt-ifdef",
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
        case _      => "2.0.0-M3"
      }
    },
    scriptedSbt := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.10.7"
        case _      => (pluginCrossBuild / sbtVersion).value
      }
    },
    scriptedBufferLog := false,
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
    },
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.eed3si9n.ifdef.sbtifdef",
  )
  .jvmPlatform(scalaVersions = Seq(scala212, scala3_sbt2))

lazy val macros = (projectMatrix in file("macros"))
  .settings(
    name := "ifdef-macro",
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
  .jvmPlatform(scalaVersions = Seq(scala212, scala213))

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
  WorkflowStep.Sbt(List("publishLocal")),
  WorkflowStep.Sbt(List("test", "scripted sbt-ifdef/*", "plugin2_12/scripted sbt1/*")),
)
