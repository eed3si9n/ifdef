package example

import com.eed3si9n.ifdef.ifdef

class A {
  @ifdef("scalaBinaryVersion:2.13")
  def foo: String = "2.13"

  @ifdef("scalaBinaryVersion:3")
  def foo: String = "3"
}

@ifdef("test")
class ATest extends munit.FunSuite {
  test("foo") {
    val actual = new A().foo
    assert(Set("3", "2.13")(actual))
  }
}
