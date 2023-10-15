val scala213 = "2.13.12"
val scala3 = "3.3.0"
ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := List(scala213, scala3)
libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
TaskKey[Unit]("check") := {
  val x = (Test / ifDefDeclations).value
  // sys.error(x.toString)
}
