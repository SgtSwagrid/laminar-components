package com.alecdorrington.laminarcomponents

import munit.FunSuite

class NumberInputSuite extends FunSuite:

  private def up
    (
      from: String,
      step: String = "",
      low: String = "",
      high: String = "",
    ) = NumberInput.stepped(from, step, 1, low, high)

  private def down
    (
      from: String,
      step: String = "",
      low: String = "",
      high: String = "",
    ) = NumberInput.stepped(from, step, -1, low, high)

  test("A whole number steps by one unless its step says otherwise."):
    assertEquals(up("7"), "8")
    assertEquals(down("7"), "6")
    assertEquals(up("10", step = "5"), "15")
    assertEquals(up("3", step = "any"), "4")

  test(
    "Stepping stops at the bounds, and brings a number beyond one within it.",
  ):
    assertEquals(up("100", low = "0", high = "100"), "100")
    assertEquals(down("0", low = "0"), "0")
    assertEquals(up("500", high = "100"), "100")
    assertEquals(down("-3", low = "0"), "0")

  test("An empty or unreadable field steps from 0, within the bounds."):
    assertEquals(up(""), "1")
    assertEquals(down(""), "-1")
    assertEquals(down("", low = "1"), "1")
    assertEquals(up("abc", low = "1"), "1")

  test(
    "A decimal keeps the places it and its step are written to, and no more.",
  ):
    assertEquals(up("0.1", step = "0.2"), "0.3")
    assertEquals(up("1.234"), "2.234")
    assertEquals(down("1", step = "0.01"), "0.99")
    assertEquals(up("2.5", step = "0.5"), "3")

  test(
    "A count is 0 while emptied, never negative, and nothing while unreadable.",
  ):
    assertEquals(NumberInput.counted(""), Some(0))
    assertEquals(NumberInput.counted("12"), Some(12))
    assertEquals(NumberInput.counted("-4"), Some(0))
    assertEquals(NumberInput.counted("1.5"), None)
