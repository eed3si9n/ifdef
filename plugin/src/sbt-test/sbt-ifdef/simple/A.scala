package example

import com.eed3si9n.ifdef.ifdef

class A {
  def foo: Int = 42

  @ifdef("compile")
  def bar: Int = 1

  @ifdef("test")
  def bar: Int = 2
}

@ifdef("test")
class ATest extends munit.FunSuite {
  test("foo") {
    val actual = new A().foo
    val expected = 42
    assertEquals(actual, expected)
  }

  test("bar") {
    val actual = new A().bar
    val expected = 2
    assertEquals(actual, expected)
  }
}
