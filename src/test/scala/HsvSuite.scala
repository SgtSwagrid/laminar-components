package com.alecdorrington.laminarcomponents

import munit.FunSuite

class HsvSuite extends FunSuite:

  private def hsv(code: String): Hsv = Hsv.parse(code).get

  test("Every code survives being read and written back."):
    val levels = (0 to 255 by 15) ++ Seq(1, 127, 128, 254)
    val codes  =
      for
        r <- levels
        g <- levels
        b <- levels
      yield f"#$r%02x$g%02x$b%02x"
    codes.foreach(code => assertEquals(hsv(code).code, code))

  test(
    "The primaries have their hues, a grey no saturation and black no value.",
  ):
    assertEquals(hsv("#ff0000"), Hsv(0, 1, 1))
    assertEquals(hsv("#00ff00"), Hsv(120, 1, 1))
    assertEquals(hsv("#0000ff"), Hsv(240, 1, 1))
    assertEquals(hsv("#ff00ff"), Hsv(300, 1, 1))
    assertEquals(hsv("#808080").saturation, 0.0)
    assertEquals(hsv("#000000"), Hsv(0, 0, 0))

  test(
    "A hue is kept when drained to grey, wraps at a full turn, and is pure fully saturated and bright.",
  ):
    val grey = Hsv(200, 0, 1)
    assertEquals(grey.code, "#ffffff")
    assertEquals(
      grey.copy(saturation = 1).code,
      "#00aaff",
    )
    assertEquals(Hsv(360, 1, 1).code, Hsv(0, 1, 1).code)
    assertEquals(Hsv(120, 0.2, 0.3).pure.code, "#00ff00")

  test(
    "A code is read with or without its hash, in either case, and anything but six hex digits is none.",
  ):
    assertEquals(
      Hsv.parse("E5484D").map(_.code),
      Some("#e5484d"),
    )
    assertEquals(
      Hsv.parse(" #e5484d ").map(_.code),
      Some("#e5484d"),
    )
    List(
      "",
      "#",
      "#abc",
      "#abcdefa",
      "#gggggg",
      "red",
    ).foreach(text => assertEquals(Hsv.parse(text), None, text))
