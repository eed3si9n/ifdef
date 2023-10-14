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

  class M extends UntypedTreeMap:
    override def transform(tree: Tree)(using Context): Tree =
      tree match
        case tree: TypeDef => transformTypeDef(tree)
        case _             => super.transform(tree)

    def transformTypeDef(tree: TypeDef)(using Context): Tree =
      val annots = tree.mods.annotations
      annots.map(extractArg).collectFirst {
        case Some(arg) =>
          if keys(arg) then super.transform(tree)
          else EmptyTree
      }.getOrElse(super.transform(tree))

  def extractArg(annot: Tree)(using Context): Option[String] =
    val IfDef = Names.termName("ifdef").toTypeName
    annot match
      case Apply(Select(New(Ident(IfDef)), _), List(Literal(Constant(arg)))) =>
        Some(arg.toString)
      case _ => None
end IfDefPhase
