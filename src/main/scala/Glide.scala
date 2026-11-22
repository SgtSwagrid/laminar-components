package com.alecdorrington.laminarcomponents

import org.scalajs.dom
import scala.scalajs.js

/**
  * Slides the elements a change moves from where they were drawn into their new
  * places, rather than letting them jump there: where each stood is read before
  * the change and again once it is laid out, and each that moved is drawn from
  * where it stood and slid home. Elements are matched by keys of the caller's,
  * so that one drawn afresh slides as the one it replaced. A block slides by
  * `transform`; an inline element, which takes none, by its offsets, so it must
  * be `position: relative`. Nothing slides for a reader who would rather
  * nothing moved of itself.
  */
object Glide:

  /** Where keyed elements stood on the page, as drawn. */
  opaque type Drawn = Map[String, (Double, Double)]

  /**
    * Reads where elements stand now.
    *
    * @param elements
    *   The elements, each under its key.
    *
    * @return
    *   Where each stands.
    */
  def drawn(elements: Iterable[(String, dom.Element)]): Drawn = elements
    .map((key, element) => key -> corner(element))
    .toMap

  /**
    * Slides each of the given elements that stood elsewhere `before` from there
    * into its place, saying whether any did. Only those moved by as much as
    * `slides` accepts, across and down, slide; the rest stand where they now
    * lie at once.
    *
    * @param before
    *   Where the elements stood before the change.
    *
    * @param elements
    *   The elements as they now are, each under its key.
    *
    * @param slides
    *   Whether an element moved so far across and down slides.
    *
    * @return
    *   Whether any element slid.
    */
  def settle
    (
      before: Drawn,
      elements: Iterable[(String, dom.Element)],
      slides: (Double, Double) => Boolean = (_, _) => true,
    )
    : Boolean = !still &&
    elements
      .flatMap((key, element) =>
        before
          .get(key)
          .map((left, top) =>
            val (x, y) = corner(element)
            (element, left - x, top - y),
          ),
      )
      .filter((_, dx, dy) => (dx.abs >= 0.5 || dy.abs >= 0.5) && slides(dx, dy))
      .map((element, dx, dy) => slide(element, dx, dy))
      .nonEmpty

  /**
    * Makes a change, then slides the elements `keyed` finds within `root` into
    * their new places once it is laid out, in the next frame.
    *
    * @param root
    *   The element holding those that may move.
    *
    * @param keyed
    *   The elements within the root that may move, each under its key.
    *
    * @param change
    *   The change.
    */
  def apply
    (
      root: dom.Element,
      keyed: dom.Element => Iterable[(String, dom.Element)],
    )
    (change: () => Unit)
    : Unit =
    val before = drawn(keyed(root))
    change()
    if !still then
      dom.window.requestAnimationFrame(_ => settle(before, keyed(root))): Unit

  /** How long a slide takes, in milliseconds. */
  val duration: Double = 200

  /** Whether the reader would rather nothing moved of itself. */
  private def still: Boolean = dom
    .window
    .matchMedia("(prefers-reduced-motion: reduce)")
    .matches

  private def corner(element: dom.Element): (Double, Double) =
    val box = element.getBoundingClientRect()
    (box.left, box.top)

  private def slide
    (
      element: dom.Element,
      dx: Double,
      dy: Double,
    )
    : Unit =
    val inline       = dom.window.getComputedStyle(element).display == "inline"
    val (from, home) =
      if inline then
        (
          js.Dynamic.literal(left = s"${ dx }px", top = s"${ dy }px"),
          js.Dynamic.literal(left = "0px", top = "0px"),
        )
      else
        (
          js.Dynamic.literal(transform = s"translate(${ dx }px, ${ dy }px)"),
          js.Dynamic.literal(transform = "none"),
        )
    element
      .asInstanceOf[js.Dynamic]
      .animate(
        js.Array(from, home),
        js.Dynamic.literal(duration = duration, easing = "ease"),
      ): Unit
