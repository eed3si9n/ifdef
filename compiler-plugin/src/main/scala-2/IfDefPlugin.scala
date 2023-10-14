package com.eed3si9n.ifdef.ifdefplugin

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

class IfDefPlugin(val global: Global) extends Plugin {
  import global._

  val name = "ifdef"
  val description = "checks for division by zero"
  val components = List[PluginComponent](Component)

  private object Component extends PluginComponent {
    private final val macroSetting = "com.eed3si9n.ifdef.declare:"
    val global: IfDefPlugin.this.global.type = IfDefPlugin.this.global
    val runsAfter = List[String]("parser")
    val phaseName = IfDefPlugin.this.name
    def newPhase(_prev: Phase) = new IfDefPhase(_prev)
    val keys = (settings.XmacroSettings.value.collect {
      case x if x.startsWith(macroSetting) => x.drop(macroSetting.size)
    }).toSet
    class IfDefPhase(prev: Phase) extends StdPhase(prev) {
      override def name = IfDefPlugin.this.name
      def apply(unit: CompilationUnit): Unit =
        new M().transformUnit(unit)
    }
    class M() extends Transformer {
      override def transform(tree: Tree): Tree =
        tree match {
          case tree: ClassDef => transformDefn(tree.mods.annotations)(tree)
          case _              => super.transform(tree)
        }

      def transformDefn(annots: List[Tree])(tree: Tree): Tree =
        annots.map(extractArg).collectFirst {
          case Some(arg) =>
            if (keys(arg)) super.transform(tree)
            else EmptyTree
        }.getOrElse(super.transform(tree))

      def extractArg(annot: Tree): Option[String] =
        annot match {
          case q"""new ifdef($arg)""" =>
            arg match {
              case Literal(Constant(x: String)) => Some(x)
              case _ => None
            }
          case _ => None
        }
    }
  }
}
