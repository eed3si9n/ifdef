package example

import com.eed3si9n.ifdef.{ ifdef, ifndef }

class B {
  @ifndef("test")
  def b: Int = 3
}

@ifdef("test")
class BTest extends munit.FunSuite {
  test("this should not compile") {
    // this should not be able to see `b` method in Test configuration.
    val actual = new B().b
    val expected = 3
    assertEquals(actual, expected)
  }
}
