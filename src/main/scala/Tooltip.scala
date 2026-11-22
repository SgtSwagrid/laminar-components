package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.{keyframes, media, rule}
import com.alecdorrington.laminarcomponents.CssProps.{
  hyphens, maxBlockSize, maxInlineSize,
}
import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.scalajs.js

/**
  * The page's one tooltip, for every hint in place of `title`, which opens late
  * and cannot be styled. A single [[host]] at the root draws it, so that no
  * ancestor can clip it. It opens at once on hover, keyboard focus or a tap,
  * beside the line of its element nearest the pointer, in the element's
  * language.
  *
  * Drawn as `.lc-tooltip` (`[data-side='below']` when below its element), its
  * content in a `.lc-tooltip-body`, a plain hint a `.lc-tooltip-text`.
  */
object Tooltip:

  /** A side a tooltip prefers, used while there is room there. */
  enum Side:

    /** Above its element. */
    case Above

    /** Below its element. */
    case Below

  private final case class Tip
    (
      content: () => HtmlElement,
      side: Side = Side.Above,
      wanted: dom.HTMLElement => Boolean = _ => true,
    )

  private final case class Point(x: Double, y: Double)

  private final case class Shown
    (
      target: dom.HTMLElement,
      tip: Tip,
      pointer: Option[Point],
      warm: Boolean,
    )

  private val shown: Var[Option[Shown]] = Var(None)

  /**
    * Whether the last input was a key, so that only keyboard focus opens a
    * hint, and a pointer at rest opens none.
    */
  private var keyboard = false

  private var closedAt = Double.NegativeInfinity

  private val settleMillis = 300.0

  private val apart = 8.0

  private val clearance = 8.0

  private val arrowInset = 14.0

  private val id = "lc-tooltip"

  private val onFocusIn  = eventProp[dom.FocusEvent]("focusin")
  private val onFocusOut = eventProp[dom.FocusEvent]("focusout")

  /**
    * Shows a hint, none if it is empty.
    *
    * @param hint
    *   The hint.
    *
    * @return
    *   The modifier giving an element the hint.
    */
  def apply(hint: String): Modifier[HtmlElement] = apply(Val(hint))

  /**
    * Shows a hint following a signal, none while it is empty.
    *
    * @param hint
    *   The hint.
    *
    * @return
    *   The modifier giving an element the hint.
    */
  def apply(hint: Signal[String]): Modifier[HtmlElement] =
    over(hint.map(text => Option.when(text.nonEmpty)(Tip(() => plain(text)))))

  /**
    * Shows the whole of a text only while the element cuts it short.
    *
    * @param text
    *   The element's whole text.
    *
    * @return
    *   The modifier giving an element the hint.
    */
  def whole(text: Signal[String]): Modifier[HtmlElement] = over(
    text.map(whole =>
      Option.when(whole.nonEmpty)(Tip(() => plain(whole), wanted = cutShort)),
    ),
  )

  /**
    * Shows content of any kind, on the side it prefers.
    *
    * @param content
    *   The content, built each time the tooltip opens.
    *
    * @param side
    *   The side preferred.
    *
    * @return
    *   The modifier giving an element the tooltip.
    */
  def rich
    (
      content: => HtmlElement,
      side: Side = Side.Above,
    )
    : Modifier[HtmlElement] = over(Val(Some(Tip(() => content, side))))

  /** The modifier closing the tooltip shown whenever its element takes input. */
  def closedOnInput: Modifier[HtmlElement] = onInput --> (_ => shown.set(None))

  /**
    * Renders the tooltip's host, mounted once at the root. Any press or Escape
    * closes the tooltip.
    *
    * @return
    *   The host.
    */
  def host: HtmlElement = div(
    styles,
    cls := "lc-tooltip-host",
    child.maybe <-- shown.signal.map(_.map(bubble)),
    documentEvents(_.onKeyDown.useCapture) --> { event =>
      keyboard = true
      if event.key == "Escape" then shown.set(None)
    },
    documentEvents(_.onPointerDown.useCapture) --> { _ =>
      keyboard = false
      shown.set(None)
    },
    documentEvents(_.onPointerMove.useCapture) --> (_ => keyboard = false),
  )

  /**
    * Opens and closes the element's tooltip: on hover for a pointer, and on a
    * tap for a finger, which cannot hover.
    */
  private def over(tips: Signal[Option[Tip]]): Modifier[HtmlElement] =
    inContext: element =>
      val target  = element.ref
      val opening = (
        event: dom.PointerEvent,
        tip: Option[Tip],
      ) => open(target, tip, Some(pointed(event)))
      List(
        tips --> (refresh(target, _)),
        // Not while typing, when what is typed redraws whatever lies under a
        // pointer at rest.
        onPointerEnter
          .filter(event => hovers(event) && !keyboard)
          .compose(_.withCurrentValueOf(tips)) --> opening.tupled,
        onPointerLeave.filter(hovers) --> (_ => close(target)),
        onPointerDown.filterNot(hovers).compose(_.withCurrentValueOf(tips)) -->
          opening.tupled,
        onFocusIn.filter(_ => keyboard).compose(_.sample(tips)) -->
          (open(target, _, None)),
        onFocusOut --> (_ => close(target)),
        onUnmountCallback(_ => close(target)),
      )

  private def open
    (
      target: dom.HTMLElement,
      tip: Option[Tip],
      pointer: Option[Point],
    )
    : Unit = tip
    .filter(_.wanted(target))
    .foreach: wanted =>
      val warm = shown.now().isDefined ||
        js.Date.now() - closedAt < settleMillis
      shown.set(Some(Shown(target, wanted, pointer, warm)))

  private def close(target: dom.HTMLElement): Unit =
    if showing(target) then shown.set(None)

  private def refresh
    (
      target: dom.HTMLElement,
      tip: Option[Tip],
    )
    : Unit = if showing(target) then
    shown.set(tip.map(Shown(
      target,
      _,
      shown.now().flatMap(_.pointer),
      true,
    )))

  private def showing(target: dom.HTMLElement): Boolean = shown
    .now()
    .exists(_.target eq target)

  private def hovers(event: dom.PointerEvent): Boolean = event.pointerType !=
    "touch"

  private def pointed(event: dom.PointerEvent): Point =
    Point(event.clientX, event.clientY)

  private def cutShort(element: dom.HTMLElement): Boolean =
    element.scrollHeight > element.clientHeight ||
    element.scrollWidth > element.clientWidth

  private def plain(text: String): HtmlElement =
    div(cls := "lc-tooltip-text", text)

  private def bubble(at: Shown): HtmlElement = div(
    cls                    := "lc-tooltip",
    cls("lc-tooltip-warm") := at.warm,
    idAttr                 := id,
    role                   := "tooltip",
    lang                   := language(at.target),
    div(
      cls := "lc-tooltip-body",
      at.tip.content(),
    ),
    inContext(tooltip =>
      List(
        windowEvents(_.onScroll.useCapture) --> (_ => place(tooltip.ref, at)),
        windowEvents(_.onResize) --> (_ => place(tooltip.ref, at)),
      ),
    ),
    onMountUnmountCallback(
      mount = context =>
        place(context.thisNode.ref, at)
        at.target.setAttribute("aria-describedby", id)
      ,
      unmount = _ =>
        closedAt = js.Date.now()
        at.target.removeAttribute("aria-describedby"),
    ),
  )

  private def language(element: dom.Element): String =
    Option(element.closest("[lang]")).fold("")(_.getAttribute("lang"))

  private def place(tooltip: dom.HTMLElement, at: Shown): Unit =
    val box    = exemplar(at)
    val size   = tooltip.getBoundingClientRect()
    val window = dom.document.documentElement
    val above  = box.top - apart - clearance
    val below  = window.clientHeight - box.bottom - apart - clearance
    val up     = at.tip.side match
      case Side.Above => size.height <= above || above > below
      case Side.Below => size.height > below && above > below
    val centre = box.left + box.width / 2
    val x      = within(
      centre - size.width / 2,
      clearance,
      window.clientWidth - clearance - size.width,
    )
    val y     = if up then box.top - apart - size.height else box.bottom + apart
    val arrow = within(
      centre - x,
      arrowInset,
      size.width - arrowInset,
    )
    tooltip
      .style
      .setProperty(
        "translate",
        s"${ x }px ${ y.max(clearance) }px",
      )
    tooltip.style.setProperty("--lc-tooltip-arrow", s"${ arrow }px")
    tooltip.setAttribute(
      "data-side",
      if up then "above" else "below",
    )

  /**
    * Finds the box to stand beside: the line of a wrapped element nearest the
    * pointer, else the whole element.
    */
  private def exemplar(at: Shown): dom.DOMRect =
    val lines = at.target.getClientRects()
    at.pointer
      .flatMap(point =>
        (0 until lines.length)
          .map(lines(_))
          .filter(_.width > 0)
          .minByOption(distance(point, _)),
      )
      .getOrElse(at.target.getBoundingClientRect())

  /** Measures the squared distance from a point to a box. */
  private def distance(point: Point, box: dom.DOMRect): Double =
    val across = (box.left - point.x).max(point.x - box.right).max(0)
    val down   = (box.top - point.y).max(point.y - box.bottom).max(0)
    across * across + down * down

  private def within(value: Double, low: Double, high: Double): Double = value
    .min(high)
    .max(low)

  private val styles = Stylesheet.layered(Stylesheet.components)(
    // Placed by `translate` from the window's top left, in physical
    // coordinates. It never takes the pointer, so hover reaches what is beneath.
    rule(".lc-tooltip")(
      position.fixed,
      top.px(0),
      left.px(0),
      zIndex        := 1000,
      maxInlineSize := "min(360px, calc(100vw - 16px))",
      padding       := "10px 14px",
      borderRadius.px(10),
      background := Theme.tooltipBackground.value,
      color      := Theme.tooltipText.value,
      fontSize   := "13.5px",
      fontWeight := "400",
      lineHeight := "1.5",
      textAlign  := "start",
      hyphens    := "manual",
      boxShadow  :=
        "0 16px 36px -10px rgba(16, 24, 40, 0.38), 0 4px 12px -4px rgba(16, 24, 40, 0.2)",
      pointerEvents.none,
      transformOrigin := "var(--lc-tooltip-arrow, 50%) 100%",
      animation       := "lc-tooltip-in 120ms cubic-bezier(0.16, 1, 0.3, 1)",
      rule("&[data-side='below']")(
        transformOrigin := "var(--lc-tooltip-arrow, 50%) 0",
      ),
      rule("&::after")(
        contentCss := "''",
        position.absolute,
        top  := "100%",
        left := "var(--lc-tooltip-arrow, 50%)",
        width.px(10),
        height.px(10),
        borderRadius := "0 0 2px 0",
        background   := "inherit",
        transform    := "translate(-50%, -50%) rotate(45deg)",
      ),
      rule("&[data-side='below']::after")(
        top.px(0),
        borderRadius := "2px 0 0 0",
      ),
    ),
    // Opening as another closes, it skips the fade, so that sweeping along
    // hints never flickers.
    rule(".lc-tooltip-warm")(animation := "none"),
    keyframes("lc-tooltip-in")(
      rule("from")(opacity := 0, transform := "scale(0.94)"),
    ),
    media("(prefers-reduced-motion: reduce)")(
      rule(".lc-tooltip")(animation := "none"),
    ),
    rule(".lc-tooltip-body")(
      maxBlockSize := "calc(100vh - 40px)",
      overflow     := "hidden",
    ),
    rule(".lc-tooltip-text")(
      whiteSpace  := "pre-line",
      unicodeBidi := "plaintext",
    ),
  )
