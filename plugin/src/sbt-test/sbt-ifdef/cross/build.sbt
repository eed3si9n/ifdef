val scala212 = "2.12.19"
val scala213 = "2.13.14"
val scala3 = "3.3.3"

lazy val root = (project in file("."))
  .aggregate(a.projectRefs:_*)
  .settings(
    name := "root",
    publish / skip := true,
    crossScalaVersions := Nil,
  )

TaskKey[Unit]("check") := {
  val x = (Test / ifDefDeclarations).value
  // sys.error(x.toString)
}

lazy val a = (projectMatrix in file("a"))
  .settings(
    name := "a",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
  )
  .jvmPlatform(scalaVersions = Seq(scala212, scala213, scala3))
  .jsPlatform(scalaVersions = Seq(scala212, scala213, scala3))
  .nativePlatform(scalaVersions = Seq(scala212, scala213, scala3))
