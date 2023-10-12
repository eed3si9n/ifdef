package example

import scala.annotation.experimental
import com.eed3si9n.ifdef.ifdef

class A:
  def foo: Int = 42

@experimental
@ifdef("test")
class ATest extends munit.FunSuite:
  test("hello"):
    val actual = new A().foo
    val expected = 42
    assertEquals(actual, expected)
