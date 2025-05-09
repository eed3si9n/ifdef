package com.eed3si9n.ifdef.sbtifdef

import sbt.*
import Keys.*

object IfDefPlugin extends AutoPlugin {
  private final val macroSetting = "com.eed3si9n.ifdef.declare:"

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends IfDefKeys
  import autoImport._
  lazy val ifDefVersion = BuildInfo.version
  override lazy val globalSettings: Seq[Def.Setting[?]] = List(
    ifDefDeclarations := Nil,
  )
  override lazy val projectSettings: Seq[Def.Setting[?]] = List(
    libraryDependencies += "com.eed3si9n.ifdef" %% "ifdef-annotation" % ifDefVersion % Provided,
    libraryDependencies += compilerPlugin("com.eed3si9n.ifdef" %% "ifdef-plugin" % ifDefVersion),
    Test / managedSources ++= (Compile / sources).value,
    Test / internalDependencyClasspath := {
      val orig = (Test / internalDependencyClasspath).value
      val compileOut = (Compile / classDirectory).value
      orig.filter { x =>
        x.data != compileOut
      }
    },
  ) ++
    inConfig(Compile)(configurationSettings) ++
    inConfig(Test)(configurationSettings)

  lazy val configurationSettings: Seq[Def.Setting[?]] = List(
    ifDefDeclarations := {
      val sbv = scalaBinaryVersion.value
      val defaults = List(
        configuration.value.name,
        s"scalaBinaryVersion:$sbv",
      )
      defaults ++ (ThisBuild / ifDefDeclarations).?.value.getOrElse(Nil)
    },
    scalacOptions := {
      val orig = scalacOptions.value
      val sv = scalaVersion.value
      val c = configuration.value
      val decls = (Compile / ifDefDeclarations).value
      c match {
        case Test => orig diff toMacroSettings(sv, decls.toList)
        case _    => orig
      }
    },
    scalacOptions ++= {
      val sv = scalaVersion.value
      val decls = ifDefDeclarations.value
      toMacroSettings(sv, decls.toList)
    },
  )

  def toMacroSettings(sv: String, decls: List[String]): List[String] = {
    if (sv.startsWith("2."))
      decls.flatMap { decl =>
        List("-Xmacro-settings", s"$macroSetting$decl")
      }
    else
      decls.flatMap { decl =>
        List(s"-Xmacro-settings:$macroSetting$decl")
      }
  }

  private def ancestorConfigs(config: Configuration) = {
    def ancestors(configs: Vector[Configuration]): Vector[Configuration] =
      configs ++ configs.flatMap(conf => ancestors(conf.extendsConfigs))
    ancestors(config.extendsConfigs)
  }
}

trait IfDefKeys {
  lazy val ifDefDeclarations = taskKey[Seq[String]]("Declarations")
}
