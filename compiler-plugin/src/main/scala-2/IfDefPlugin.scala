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
    class IfDefPhase(prev: Phase) extends StdPhase(prev) {
      override def name = IfDefPlugin.this.name
      def apply(unit: CompilationUnit): Unit =
        new M().transformUnit(unit)
    }
    class M() extends Transformer {
      val keys = (settings.XmacroSettings.value.collect {
        case x if x.startsWith(macroSetting) => x.drop(macroSetting.size)
      }).toSet
      val eval = IfDefExpr.eval(keys)(_)
      override def transform(tree: Tree): Tree =
        tree match {
          case tree: MemberDef => transformDefn(tree.mods.annotations)(tree)
          case _               => super.transform(tree)
        }

      // transform any definitions, including classes and `def`s
      def transformDefn(annots: List[Tree])(tree: Tree): Tree =
        annots.iterator.map(extractAnnotation).collectFirst {
          case Some(expr) =>
            if (eval(expr)) super.transform(tree)
            else EmptyTree
        }.getOrElse(super.transform(tree))

      def extractAnnotation(annot: Tree): Option[IfDefExpr] =
        annot match {
          case q"""new ifdef($arg)""" =>
            Some(IfDefExpr.IfDef(extractLiteral(arg)))
          case q"""new ifndef($arg)""" =>
            Some(IfDefExpr.IfNDef(extractLiteral(arg)))
          case _ => None
        }

      def extractLiteral(arg: Tree): String =
        arg match {
          case Literal(Constant(x: String)) => x
          case _                            => sys.error(s"invalid arg $arg")
        }
    }
  }
}

sealed trait IfDefExpr
object IfDefExpr {
  case class IfDef(arg: String) extends IfDefExpr
  case class IfNDef(arg: String) extends IfDefExpr
  def eval(env: Set[String])(expr: IfDefExpr): Boolean =
    expr match {
      case IfDef(arg)  => env(arg)
      case IfNDef(arg) => !env(arg)
    }
}
