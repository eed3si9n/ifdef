package com.eed3si9n.ifdef

import scala.annotation.{ experimental, MacroAnnotation }
import scala.quoted.*

@experimental
class ifdef(key: String) extends MacroAnnotation:
  private final val macroSetting = "com.eed3si9n.ifdef.declare:"

  def transform(using Quotes)(tree: quotes.reflect.Definition): List[quotes.reflect.Definition] =
    import quotes.reflect.*
    tree match
      case cls @ ClassDef(className, ctr, parents, self, body) =>
        val keys = (CompilationInfo.XmacroSettings.collect:
          case x if x.startsWith(macroSetting) => x.drop(macroSetting.size)
        ).toSet
        if keys(key) then List(tree)
        else
          val trees = List(ClassDef.copy(tree)(
            className,
            DefDef(cls.symbol.primaryConstructor, _ => None),
            parents = Nil,
            None,
            Nil))
          println(trees.map(_.show))
          trees
      case _ =>
        report.error("annotation only supports `class`")
        List(tree)
