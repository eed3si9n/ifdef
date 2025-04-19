ifdef
=====

`@ifdef` is an annotation that implements conditional compilation in Scala.

See https://eed3si9n.com/ifdef-in-scala-via-pre-typer-processing/ for details.

setup
-----

```scala
addSbtPlugin("com.eed3si9n.ifdef" % "sbt-ifdef" % "x.y.z")
```

usage
-----

```scala
package example

import com.eed3si9n.ifdef.ifdef

class A {
  def foo: Int = 42
}

@ifdef("test")
class ATest extends munit.FunSuite {
  test("hello") {
    val actual = new A().foo
    val expected = 42
    assertEquals(actual, expected)
  }
}
```

custom definitions
----------------

You can add custom definitions in your build.sbt:

```scala
// Add a custom definition for early access features
ThisBuild / ifDefDeclarations += "eap"
```

Then use them in your code:

```scala
// This method will only be available when "eap" is defined
@ifdef("eap")
def earlyAccessFeature(): Unit = {
  // Implementation of early access feature
}
```

license
-------
ifdef is released under Apache License Version 2.0.
