package com.alecdorrington.laminarcomponents

import com.raquo.laminar.modifiers.SimpleKeySetter.{
  DerivedStyleSetter, StyleSetter,
}

/**
  * Stylesheets written in Scala, with Laminar's own typed style props: rules
  * for selectors, holding what an element's inline styles cannot, its states
  * (`:hover`), its parts (`::after`), and styles for some media alone
  * (`@media`), with keyframes and font faces besides.
  *
  * A rule nested in another applies within it, its `&` standing for the outer
  * rule's selector (else a descendant of it), as in CSS nesting, but each is
  * written out whole, for any browser:
  *
  * {{{
  * import com.raquo.laminar.api.L.*
  * import com.alecdorrington.laminarcomponents.Css.{media, rule}
  *
  * val styles = Stylesheet(
  *   rule(".card")(
  *     padding.px(12),
  *     borderRadius.px(8),
  *     rule("&:hover")(backgroundColor := "#eef"),
  *     rule(".card-title")(fontWeight.bold),
  *     media("(max-width: 600px)")(padding.px(8)),
  *   ),
  * )
  * }}}
  *
  * Import the builders by name, as Laminar has a `media` of its own.
  */
object Css:

  /** A declaration: a style prop set to a value, as `top.px(0)` sets `top`. */
  type Declaration = StyleSetter[?, ?] | DerivedStyleSetter[?, ?]

  /** What a rule holds: its declarations, and rules nested in it. */
  type Part = Declaration | Block

  /** A rule, at-rule or nesting of rules, that a stylesheet holds. */
  sealed trait Block

  /**
    * A rule: declarations for whatever its selector matches, and rules nested
    * in it.
    *
    * @param selector
    *   The selector, or several, separated by commas.
    *
    * @param parts
    *   The declarations and nested rules, in order.
    */
  final case class Rule(selector: String, parts: Seq[Part]) extends Block

  /**
    * Rules applying only under a condition, as `@media` and `@supports` have,
    * or nested in a rule, declarations for its selector under the condition.
    *
    * @param prelude
    *   The at-rule's keyword and condition, as `@media (pointer: coarse)`.
    *
    * @param parts
    *   The rules, and within a rule, declarations.
    */
  final case class Conditional(prelude: String, parts: Seq[Part]) extends Block

  /**
    * An animation's keyframes.
    *
    * @param name
    *   The animation's name.
    *
    * @param frames
    *   Each keyframe, its selector `from`, `to` or a percentage.
    */
  final case class Keyframes(name: String, frames: Seq[Rule]) extends Block

  /**
    * A font face.
    *
    * @param descriptors
    *   Its descriptors, as `fontFamily := "Caveat"` and `CssProps.fontSource`.
    */
  final case class FontFace(descriptors: Seq[Declaration]) extends Block

  /**
    * Makes a rule.
    *
    * @param selector
    *   The selector, or several, separated by commas; nested, `&` stands for
    *   the outer rule's selector.
    *
    * @param parts
    *   The declarations and nested rules, in order.
    *
    * @return
    *   The rule.
    */
  def rule(selector: String)(parts: Part*): Rule = Rule(selector, parts)

  /**
    * Makes rules apply only to the media a query matches.
    *
    * @param query
    *   The media query, as `(max-width: 600px)`.
    *
    * @param parts
    *   The rules, and nested in a rule, declarations for its selector.
    *
    * @return
    *   The conditional rules.
    */
  def media(query: String)(parts: Part*): Conditional =
    Conditional(s"@media $query", parts)

  /**
    * Makes rules apply only where the browser supports a feature.
    *
    * @param condition
    *   The condition, as `(field-sizing: content)`.
    *
    * @param parts
    *   The rules, and nested in a rule, declarations for its selector.
    *
    * @return
    *   The conditional rules.
    */
  def supports(condition: String)(parts: Part*): Conditional =
    Conditional(s"@supports $condition", parts)

  /**
    * Makes an animation's keyframes.
    *
    * @param name
    *   The animation's name.
    *
    * @param frames
    *   Each keyframe, as `rule("from")(opacity := 0)`.
    *
    * @return
    *   The keyframes.
    */
  def keyframes(name: String)(frames: Rule*): Keyframes =
    Keyframes(name, frames)

  /**
    * Makes a font face.
    *
    * @param descriptors
    *   Its descriptors.
    *
    * @return
    *   The font face.
    */
  def fontFace(descriptors: Declaration*): FontFace = FontFace(descriptors)

  /**
    * Writes blocks out as CSS, every nested rule written out whole.
    *
    * @param blocks
    *   The blocks, in order.
    *
    * @return
    *   Their CSS.
    */
  def render(blocks: Seq[Block]): String = blocks
    .flatMap(written(_, Nil, Nil))
    .mkString("\n")

  /**
    * Writes a block out, within the selectors of the rules it is nested in
    * (none at the top) and the at-rules around it, outermost first.
    */
  private def written
    (
      block: Block,
      selectors: List[String],
      around: List[String],
    )
    : List[String] = block match
    case Rule(selector, parts) =>
      contents(parts, nest(selectors, selector), around)
    case Conditional(prelude, parts) =>
      contents(parts, selectors, around :+ prelude)
    case Keyframes(name, frames) => List(wrapped(
        around,
        frames
          .map(frame =>
            s"${ frame.selector } { ${ properties(split(frame.parts)._1) } }",
          )
          .mkString(s"@keyframes $name { ", " ", " }"),
      ))
    case FontFace(descriptors) => List(wrapped(
        around,
        s"@font-face { ${ properties(descriptors) } }",
      ))

  /** Writes a rule's own declarations, then the rules nested in it. */
  private def contents
    (
      parts: Seq[Part],
      selectors: List[String],
      around: List[String],
    )
    : List[String] =
    val (declarations, blocks) = split(parts)
    Option
      .when(declarations.nonEmpty && selectors.nonEmpty)(wrapped(
        around,
        s"${ selectors.mkString(", ") } { ${ properties(declarations) } }",
      ))
      .toList ++ blocks.flatMap(written(_, selectors, around))

  private def split(parts: Seq[Part]): (List[Declaration], List[Block]) = parts
    .toList
    .partitionMap:
      case block: Block                     => Right(block)
      case setter: StyleSetter[?, ?]        => Left(setter)
      case setter: DerivedStyleSetter[?, ?] => Left(setter)

  private def wrapped(around: List[String], css: String): String = around
    .foldRight(css)((prelude, inner) => s"$prelude { $inner }")

  private def properties(declarations: Seq[Declaration]): String = declarations
    .flatMap(property)
    .mkString(" ")

  /**
    * Writes a declaration, once for each of its vendor prefixes too. Its value
    * is encoded here, as Laminar's `cssValue` needs a page to run on.
    */
  private def property(declaration: Declaration): Seq[String] =
    val (name, prefixes, value) = declaration match
      case setter: StyleSetter[?, ?] =>
        (setter.key.name, setter.key.prefixes, String.valueOf(setter.value))
      case setter: DerivedStyleSetter[v, ?] => (
          setter.key.name,
          setter.key.key.prefixes,
          setter.key.encode(setter.value.asInstanceOf[v]),
        )
    (prefixes.map(_ + name) :+ name).map(named => s"$named: $value;")

  /**
    * Combines the selectors a rule is nested in with its own: each of its own
    * within each outer one, by `&`, else as a descendant.
    */
  private[laminarcomponents] def nest
    (outer: List[String], selector: String)
    : List[String] =
    val own = listed(selector)
    if outer.isEmpty then own
    else
      for
        parent <- outer
        inner  <- own
      yield
        val parts = unquoted(inner)
        if parts.lengthIs > 1 then parts.mkString(parent) else s"$parent $inner"

  /**
    * Splits a selector at each `&`, but for those within quotes, as an
    * attribute's value may hold one.
    */
  private def unquoted(selector: String): List[String] =
    parted(selector, (char, _) => char == '&')

  /**
    * Splits a selector list at its commas, but for those within brackets,
    * parentheses or quotes.
    */
  private[laminarcomponents] def listed(selectors: String): List[String] =
    parted(
      selectors,
      (char, depth) => char == ',' && depth == 0,
    ).map(_.trim).filter(_.nonEmpty)

  /**
    * Splits text at each character `at` accepts, given how deep within brackets
    * and parentheses it lies, but for those within quotes.
    */
  private def parted(text: String, at: (Char, Int) => Boolean): List[String] =
    final case class Scan
      (
        done: List[String],
        current: String,
        depth: Int,
        quote: Option[Char],
      )
    val scanned = text.foldLeft(Scan(Nil, "", 0, None)): (scan, char) =>
      scan.quote match
        case Some(quote) => scan.copy(
            current = scan.current :+ char,
            quote = Option.when(char != quote)(quote),
          )
        case None if at(char, scan.depth) =>
          scan.copy(
            done = scan.done :+ scan.current,
            current = "",
          )
        case None => scan.copy(
            current = scan.current :+ char,
            depth = scan.depth + nesting(char),
            quote = Option.when(char == '"' || char == '\'')(char),
          )
    scanned.done :+ scanned.current

  /** The change a character makes to the depth of brackets. */
  private def nesting(char: Char): Int = char match
    case '(' | '[' => 1
    case ')' | ']' => -1
    case _         => 0
