ifdef
=====

`@ifdef` is an **experimental** macro that implements conditional compilation.

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
