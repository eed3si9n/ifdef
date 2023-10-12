val scala212 = "2.12.18"
val scala213 = "2.13.12"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := List(scala213, scala212)
libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
