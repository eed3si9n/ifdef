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
  override lazy val globalSettings: Seq[Def.Setting[_]] = List(
    ifDefDeclations := Nil,
  )
  override lazy val projectSettings: Seq[Def.Setting[_]] = List(
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

  lazy val configurationSettings: Seq[Def.Setting[_]] = List(
    ifDefDeclations := {
      val sbv = scalaBinaryVersion.value
      List(
        configuration.value.name,
        s"scalaBinaryVersion:$sbv",
      )
    },
    scalacOptions --= {
      val sv = scalaVersion.value
      val c = configuration.value
      val decls = (Compile / ifDefDeclations).value
      c match {
        case Test => toMacroSettings(sv, decls.toList)
        case _    => Nil
      }
    },
    scalacOptions ++= {
      val sv = scalaVersion.value
      val decls = ifDefDeclations.value
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
  lazy val ifDefDeclations = taskKey[Seq[String]]("Declarations")
}
