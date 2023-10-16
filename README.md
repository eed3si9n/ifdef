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

license
-------
ifdef is released under Apache License Version 2.0.
