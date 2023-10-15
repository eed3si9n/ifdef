package com.eed3si9n.ifdef.ifdefplugin

import dotty.tools.dotc.CompilationUnit
import dotty.tools.dotc.core.Annotations.Annotation
import dotty.tools.dotc.core.Constants.Constant
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Names
import dotty.tools.dotc.plugins.{ PluginPhase, StandardPlugin }
import dotty.tools.dotc.parsing.Parser

class IfDefPlugin extends StandardPlugin:
  val name: String = "ifdef"
  override val description: String = "ifdef preprocessor"
  def init(options: List[String]): List[PluginPhase] =
    (new IfDefPhase) :: Nil
end IfDefPlugin

class IfDefPhase extends PluginPhase:
  private final val macroSetting = "com.eed3si9n.ifdef.declare:"
  val phaseName = "ifDef"
  var keys: Set[String] = Set.empty
  override def runsAfter: Set[String] = Set(Parser.name)
  override def runOn(units: List[CompilationUnit])(using ctx: Context): List[CompilationUnit] =
    val unitContexts =
      for unit <- units
      yield ctx.fresh.setPhase(this.start).setCompilationUnit(unit)
    keys = (ctx.settings.XmacroSettings.value.collect:
      case x if x.startsWith(macroSetting) => x.drop(macroSetting.size)
    ).toSet
    unitContexts.foreach(preprocess(using _))
    unitContexts.map(_.compilationUnit)

  def preprocess(using ctx: Context): Unit =
    val unit = ctx.compilationUnit
    try
      if !unit.suspended then
        unit.untpdTree = (new M).transform(unit.untpdTree)
    catch case _: CompilationUnit.SuspendException => ()

  import dotty.tools.dotc.ast.untpd
  import untpd.*

  val IfDefName = Names.termName("ifdef").toTypeName
  val IfNDefName = Names.termName("ifndef").toTypeName
  class M extends UntypedTreeMap:
    val eval = IfDefExpr.eval(keys)
    override def transform(tree: Tree)(using Context): Tree =
      tree match
        case tree: DefTree => transformDefn(tree.mods.annotations)(tree)
        case _             => super.transform(tree)

    // transform any definitions, includinng classes and `def`s
    def transformDefn(annots: List[Tree])(tree: Tree)(using Context): Tree =
      annots.iterator.map(extractAnnotation).collectFirst {
        case Some(expr) =>
          if eval(expr) then super.transform(tree)
          else EmptyTree
      }.getOrElse(super.transform(tree))

  def extractAnnotation(annot: Tree)(using ctx: Context): Option[IfDefExpr] =
    annot match
      case Apply(Select(New(Ident(IfDefName)), _), List(arg)) =>
        Some(IfDefExpr.IfDef(extractLiteral(arg)))
      case Apply(Select(New(Ident(IfNDefName)), _), List(arg)) =>
        Some(IfDefExpr.IfNDef(extractLiteral(arg)))
      case _ => None

  def extractLiteral(arg: Tree): String =
    arg match
      case Literal(Constant(x)) => x.toString
      case _                    => sys.error(s"invalid arg $arg")
end IfDefPhase

enum IfDefExpr:
  case IfDef(arg: String)
  case IfNDef(arg: String)

object IfDefExpr:
  def eval(env: Set[String])(expr: IfDefExpr): Boolean =
    expr match
      case IfDefExpr.IfDef(arg)  => env(arg)
      case IfDefExpr.IfNDef(arg) => !env(arg)
end IfDefExpr
