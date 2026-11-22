package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.{
  fontFace, keyframes, media, rule, supports,
}
import com.alecdorrington.laminarcomponents.CssProps.{
  fontSource, insetInlineStart,
}
import com.raquo.laminar.api.L.*
import munit.FunSuite

class CssSuite extends FunSuite:

  test("A rule writes each declaration, typed or set, with its unit."):
    assertEquals(
      Css.render(List(rule(".card")(
        position.relative,
        top.px(4),
        color            := "red",
        opacity          := 0.5,
        insetInlineStart := "0",
      ))),
      ".card { position: relative; top: 4px; color: red; opacity: 0.5; inset-inline-start: 0; }",
    )

  test("A nested rule stands within its outer one, by & or as a descendant."):
    assertEquals(
      Css.render(List(rule(".card")(
        color := "red",
        rule("&:hover, &.picked")(color := "blue"),
        rule(".title")(fontWeight.bold),
      ))),
      List(
        ".card { color: red; }",
        ".card:hover, .card.picked { color: blue; }",
        ".card .title { font-weight: bold; }",
      ).mkString("\n"),
    )

  test("Outer selector lists are crossed with inner ones."):
    assertEquals(
      Css.render(List(rule(".a, .b")(rule("&:hover, .c")(color := "red")))),
      ".a:hover, .a .c, .b:hover, .b .c { color: red; }",
    )

  test("An & within quotes stands for itself, not the outer selector."):
    assertEquals(
      Css.render(List(rule(".a")(
        rule("&[title='x & y']")(color := "red"),
        rule("[title=\"&\"]")(color    := "blue"),
      ))),
      List(
        ".a[title='x & y'] { color: red; }",
        ".a [title=\"&\"] { color: blue; }",
      ).mkString("\n"),
    )

  test("Commas within brackets, parentheses or quotes part no selectors."):
    assertEquals(
      Css.listed(":is(.a, .b) > .c, [title='x, y'], .d"),
      List(
        ":is(.a, .b) > .c",
        "[title='x, y']",
        ".d",
      ),
    )

  test("Media wrap rules, and nested in a rule, its own declarations."):
    assertEquals(
      Css.render(List(
        rule(".card")(
          padding.px(12),
          media("(max-width: 600px)")(padding.px(8)),
        ),
        media("(pointer: coarse)")(
          supports("(display: grid)")(rule(".card")(display.grid)),
        ),
      )),
      List(
        ".card { padding: 12px; }",
        "@media (max-width: 600px) { .card { padding: 8px; } }",
        "@media (pointer: coarse) { @supports (display: grid) { .card { display: grid; } } }",
      ).mkString("\n"),
    )

  test("Keyframes and font faces are written whole."):
    assertEquals(
      Css.render(List(
        keyframes("fade")(
          rule("from")(opacity := 0),
          rule("to")(opacity   := 1),
        ),
        fontFace(
          fontFamily := "Hand",
          fontSource := "url(hand.ttf)",
        ),
      )),
      List(
        "@keyframes fade { from { opacity: 0; } to { opacity: 1; } }",
        "@font-face { font-family: Hand; src: url(hand.ttf); }",
      ).mkString("\n"),
    )

  test(
    "A stylesheet added where there is no page, as on Node, is left as it was.",
  ):
    val sheet = Stylesheet(rule(".a")(color := "red"))
    assert(sheet.added eq sheet)

  test("A layered stylesheet puts its rules in its layer."):
    assertEquals(
      Stylesheet.layered("parts")(rule(".a")(color := "red")).css,
      "@layer parts {\n.a { color: red; }\n}",
    )

  test("A theme's token reads with its fallback and sets its own property."):
    val token = Theme.Token("--brand", "teal")
    assertEquals(token.value, "var(--brand, teal)")
    assertEquals(
      Css.render(List(rule(":root")(token := "navy"))),
      ":root { --brand: navy; }",
    )
