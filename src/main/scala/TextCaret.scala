package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.TextCaret.{Selection, Spot}
import org.scalajs.dom
import scala.scalajs.js

/**
  * Offsets in the text of an element, read from points on the screen and from
  * the page's selection, and where a caret at one stands: what a field drawn
  * over rendered text, or a highlighter, needs to know where the reader is. The
  * text of elements matching `aside`, such as notes drawn between the words, is
  * no part of the element's own, so it is skipped, a point within it counting
  * as where it stands.
  *
  * @param aside
  *   The selector of the elements whose text is skipped.
  */
class TextCaret(aside: String):

  /**
    * Finds the offset in an element's text nearest a point on the screen.
    *
    * @param root
    *   The element.
    *
    * @param x
    *   The point's distance from the window's left edge, in pixels.
    *
    * @param y
    *   The point's distance from the window's top edge, in pixels.
    *
    * @return
    *   The offset, if the point lies in the element.
    */
  def within(root: dom.Element, x: Double, y: Double): Option[Int] = TextCaret
    .point(x, y)
    .flatMap(place(root, _, _))

  /**
    * Finds where a caret at an offset of an element's text stands: before the
    * character there, else after the one before it, so that a caret at a wrap
    * stands at the start of the next line, as a field's does. A line break is
    * drawn at the end of its line, so a caret after the last one stands a line
    * below it.
    *
    * @param root
    *   The element.
    *
    * @param text
    *   The element's own text.
    *
    * @param offset
    *   The caret's offset in the text.
    *
    * @return
    *   Where the caret stands.
    */
  def spot
    (
      root: dom.Element,
      text: String,
      offset: Int,
    )
    : Option[Spot] =
    val ahead  = dom.window.getComputedStyle(root).direction != "rtl"
    val start  = (box: dom.DOMRect) => if ahead then box.left else box.right
    val end    = (box: dom.DOMRect) => if ahead then box.right else box.left
    val edge   = root.getBoundingClientRect()
    val margin = if ahead then edge.left else edge.right
    def drawn(at: Int) = Option
      .when(at >= 0 && at < text.length)(at)
      .flatMap(at => rects(root, at, at + 1).headOption)
    drawn(offset)
      .map(box => Spot(start(box), box.top, box.height))
      .orElse(drawn(offset - 1).map(box =>
        if text(offset - 1) == '\n' then
          Spot(
            margin,
            box.top + TextCaret.lineHeight(root),
            box.height,
          )
        else Spot(end(box), box.top, box.height),
      ))
      .orElse:
        val line = TextCaret.lineHeight(root)
        Some(Spot(margin, edge.top + line / 4, line / 2))

  /**
    * Finds the boxes on the screen of a span of an element's text.
    *
    * @param root
    *   The element.
    *
    * @param from
    *   Where the span starts.
    *
    * @param until
    *   Where the span ends, exclusive.
    *
    * @return
    *   The span's boxes, a line or part of one each.
    */
  def rects(root: dom.Element, from: Int, until: Int): List[dom.DOMRect] =
    val texts = owned(root)
    texts
      .zip(texts.scanLeft(0)(_ + _.textContent.length))
      .filter((text, done) =>
        done < until && done + text.textContent.length > from,
      )
      .flatMap: (text, done) =>
        val range = dom.document.createRange()
        range.setStart(text, (from - done).max(0))
        range.setEnd(
          text,
          (until - done).min(text.textContent.length),
        )
        range.getClientRects().toList

  /**
    * Finds which line of an element a caret at an offset of its text stands on,
    * its line breaks' boxes standing for its empty lines.
    *
    * @param root
    *   The element.
    *
    * @param text
    *   The element's own text.
    *
    * @param offset
    *   The caret's offset in the text.
    *
    * @return
    *   The line, counted from naught.
    */
  def lineOf
    (
      root: dom.Element,
      text: String,
      offset: Int,
    )
    : Int =
    val half = TextCaret.lineHeight(root) / 2
    val tops = rects(root, 0, text.length)
      .map(_.top)
      .sorted
      .foldLeft(List.empty[Double])((lines, top) =>
        if lines.headOption.exists(top - _ < half) then lines else top :: lines,
      )
    spot(root, text, offset).fold(0)(at => tops.count(_ < at.top - half))

  /**
    * Reads the page's selection as far as an element's text goes.
    *
    * @param root
    *   The element.
    *
    * @return
    *   The span selected, a selection collapsed within the text, or one
    *   elsewhere.
    */
  def selection(root: dom.Element): Selection =
    Option(dom.window.getSelection()).filter(_.rangeCount > 0) match
      case Some(selected)
        if root.contains(selected.anchorNode) &&
        root.contains(selected.focusNode) =>
        if selected.isCollapsed then Selection.Cleared
        else
          ends(root, selected)
            .filter(_ != _)
            .fold(Selection.Cleared)(Selection.Span(_, _))
      case _ => Selection.Elsewhere

  /**
    * Reads the page's selection within an element's text, collapsed where the
    * reader last clicked in it.
    *
    * @param root
    *   The element.
    *
    * @return
    *   The offsets where the selection starts and ends, if it lies in the
    *   element.
    */
  def range(root: dom.Element): Option[(Int, Int)] = Option(
    dom.window.getSelection(),
  ).filter(_.rangeCount > 0).flatMap(ends(root, _))

  /** The offsets of a selection's ends, the earlier first. */
  private def ends
    (
      root: dom.Element,
      selected: dom.Selection,
    )
    : Option[(Int, Int)] =
    for
      one <- place(
        root,
        selected.anchorNode,
        selected.anchorOffset,
      )
      other <- place(
        root,
        selected.focusNode,
        selected.focusOffset,
      )
    yield (one.min(other), one.max(other))

  /** The element's own text nodes, in order, without those aside. */
  private def owned(root: dom.Element): List[dom.Node] = TextCaret
    .texts(root)
    .filterNot(skipped)

  /**
    * Finds the offset of a point in the DOM, a point in a text skipped counting
    * as where that text stands.
    */
  private def place
    (
      root: dom.Element,
      node: dom.Node,
      offset: Int,
    )
    : Option[Int] = Option.when(root.contains(node)):
    val point = dom.document.createRange()
    point.setStart(node, offset)
    point.collapse(true)
    val texts = TextCaret.texts(root)
    // How much of the element's own text lies before each text node, then in
    // all.
    val before = texts.scanLeft(0)(_ + own(_))
    texts
      .zip(before)
      .collectFirst:
        case (text, done) if text == node =>
          if skipped(text) then done else done + offset
        case (text, done) if TextCaret.precedes(point, text) => done
      .getOrElse(before.last)

  private def own(text: dom.Node): Int =
    if skipped(text) then 0 else text.textContent.length

  private def skipped(text: dom.Node): Boolean = Option(text.parentNode)
    .collect { case parent: dom.Element => parent }
    .exists(_.closest(aside) != null)

object TextCaret:

  /** A reader's selection, as far as an element's text goes. */
  enum Selection:

    /**
      * A span of the text.
      *
      * @param from
      *   Where it starts.
      *
      * @param until
      *   Where it ends, exclusive.
      */
    case Span(from: Int, until: Int)

    /** A selection collapsed within the text. */
    case Cleared

    /** A selection elsewhere, which leaves the text's selection as it was. */
    case Elsewhere

  /**
    * Where a caret stands on the screen.
    *
    * @param x
    *   Its distance from the window's left edge, in pixels.
    *
    * @param top
    *   Its line's top, from the window's top edge, in pixels.
    *
    * @param height
    *   Its line's height, in pixels.
    */
  final case class Spot(x: Double, top: Double, height: Double)

  /**
    * Reads an element's line height, before any room is made in a line.
    *
    * @param root
    *   The element.
    *
    * @return
    *   Its line height, in pixels, or `0` where it has none in pixels.
    */
  def lineHeight(root: dom.Element): Double = dom
    .window
    .getComputedStyle(root)
    .lineHeight
    .trim
    .stripSuffix("px")
    .toDoubleOption
    .getOrElse(0)

  /** Empties the page's selection. */
  def clear(): Unit =
    Option(dom.window.getSelection()).foreach(_.removeAllRanges())

  private def texts(root: dom.Element): List[dom.Node] =
    val walker = dom
      .document
      .createTreeWalker(
        root,
        dom.NodeFilter.SHOW_TEXT,
        null,
        false,
      )
    Iterator.continually(walker.nextNode()).takeWhile(_ != null).toList

  /** Finds the node and offset in it nearest a point on the screen. */
  private def point(x: Double, y: Double): Option[(dom.Node, Int)] =
    val document = dom.document.asInstanceOf[js.Dynamic]
    if !js.isUndefined(document.caretPositionFromPoint) then
      Option(document.caretPositionFromPoint(x, y))
        .filterNot(js.isUndefined)
        .map(caret =>
          (
            caret.offsetNode.asInstanceOf[dom.Node],
            caret.offset.asInstanceOf[Int],
          ),
        )
    else if !js.isUndefined(document.caretRangeFromPoint) then
      Option(document.caretRangeFromPoint(x, y).asInstanceOf[dom.Range])
        .filterNot(js.isUndefined)
        .map(range => (range.startContainer, range.startOffset))
    else None

  private def precedes(point: dom.Range, node: dom.Node): Boolean = point
    .asInstanceOf[js.Dynamic]
    .comparePoint(node, 0)
    .asInstanceOf[Int] >= 0
