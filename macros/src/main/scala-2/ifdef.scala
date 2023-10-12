package com.eed3si9n.ifdef

import scala.annotation.{ meta, StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros._

@compileTimeOnly("enable -Ymacro-annotations to expand macro annotations")
@meta.getter @meta.setter
class ifdef(key: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro IfDefMacro.impl
}

object IfDefMacro {
  private final val macroSetting = "com.eed3si9n.ifdef.declare:"
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val keys = (c.settings.collect {
      case x if x.startsWith(macroSetting) => x.drop(macroSetting.size)
    }).toSet
    def extractAnnotationArg(apply: Tree): String = {
      val q"""new $x($arg).$y(..$args2)""" = apply
      arg match {
        case Literal(Constant(x: String)) => x
      }
    }
    def extractClassName(classDecl: ClassDef) = {
      val q"..$mod class $className(..$fields) extends ..$bases { ..$body }" = classDecl
      className
    }
    val arg = extractAnnotationArg(c.macroApplication)
    annottees.map(_.tree) match {
      case (decl: ClassDef) :: Nil =>
        if (keys(arg)) c.Expr(decl)
        else {
          val className = extractClassName(decl)
          c.Expr(q"""
            private class $className
          """)
        }
      case _ => c.abort(c.enclosingPosition, "invalid annottee")
    }
  }
}
