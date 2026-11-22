package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.{media, rule}
import com.alecdorrington.laminarcomponents.CssProps.{
  appearance, borderBlockStart, borderInlineStart, fontVariantNumeric,
  placeItems, strokeWidth, touchAction, userSelect, webkitTouchCallout,
  webkitUserSelect,
}
import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.Codec
import org.scalajs.dom

/**
  * A number typed into a field, or stepped by the arrows forming its end, which
  * repeat while held, in place of the browser's own, which differ from browser
  * to browser. The arrows reach the field as typing would, by `input` and
  * `change`.
  *
  * Drawn as `.lc-number-input`, the field within it, and its arrows
  * `.lc-number-step`s in `.lc-number-steps`, each icon a `.lc-number-arrow`.
  * The arrows' width is `--lc-number-steps`, which a width given the box should
  * add to the number's room: `calc(5em + var(--lc-number-steps))`.
  */
object NumberInput:

  /**
    * Renders a number field. The box it gives is what is laid out, shown or
    * hidden.
    *
    * @param field
    *   Modifiers of the field itself: its bounds, its step, its name
    *   (`aria.label`) and its binding.
    *
    * @return
    *   The field and its arrows.
    */
  def apply(field: Modifier[HtmlElement]*): HtmlElement =
    val typed = input(typ := "number", field)
    span(
      styles,
      cls := "lc-number-input",
      typed,
      span(
        cls         := "lc-number-steps",
        aria.hidden := true,
        arrow(typed, 1, up),
        arrow(typed, -1, down),
      ),
    )

  /**
    * Binds a field to a whole count from `0` to `most`. Emptied, it reads as
    * `0`, so that it can be typed afresh.
    *
    * @param shown
    *   The count.
    *
    * @param set
    *   The callback given each count typed or stepped to.
    *
    * @param most
    *   The greatest count, if any.
    *
    * @return
    *   The field's modifiers.
    */
  def count
    (
      shown: Signal[Int],
      set: Int => Unit,
      most: Option[Int] = None,
    )
    : Modifier[HtmlElement] = Seq(
    minAttr := "0",
    most.fold(emptyMod)(top => maxAttr := top.toString),
    controlled(
      value <-- shown.map(_.toString),
      onInput.mapToValue -->
        (raw => counted(raw).foreach(n => set(most.fold(n)(n.min)))),
    ),
  )

  /**
    * Reads a count as typed: `0` while emptied, never below `0`.
    *
    * @param raw
    *   The field's text.
    *
    * @return
    *   The count, if the text is one.
    */
  def counted(raw: String): Option[Int] =
    (if raw.isEmpty then Some(0) else raw.toIntOption).map(_.max(0))

  /**
    * Writes a number as typed, without a decimal point when it is whole.
    *
    * @param value
    *   The number.
    *
    * @return
    *   Its text.
    */
  def written(value: Double): String =
    if value.isWhole then value.toLong.toString else value.toString

  /**
    * Steps a number, within bounds, as precise as the step and the number are
    * written. An empty or unreadable number reads as `0`, as a browser's own
    * arrows read it.
    *
    * @param from
    *   The number as typed.
    *
    * @param step
    *   The field's step, `1` if empty or not positive.
    *
    * @param steps
    *   How many steps, down where negative.
    *
    * @param low
    *   The field's least value, none if empty.
    *
    * @param high
    *   The field's greatest value, none if empty.
    *
    * @return
    *   The number stepped to, as typed.
    */
  def stepped
    (
      from: String,
      step: String,
      steps: Int,
      low: String,
      high: String,
    )
    : String =
    val size   = step.toDoubleOption.filter(_ > 0).getOrElse(1.0)
    val places = decimals(from).max(decimals(step))
    val moved  = from.toDoubleOption.getOrElse(0.0) + steps * size
    val within = high
      .toDoubleOption
      .fold(moved)(moved.min)
      .max(low.toDoubleOption.getOrElse(Double.NegativeInfinity))
    written(math.round(within * math.pow(10, places)) / math.pow(10, places))

  private def decimals(number: String): Int = number
    .dropWhile(_ != '.')
    .drop(1)
    .takeWhile(_.isDigit)
    .length

  /**
    * An arrow stepping the field `steps` steps, once pressed and again while
    * held.
    */
  private def arrow(typed: Input, steps: Int, icon: SvgElement): HtmlElement =
    button(
      typ      := "button",
      cls      := "lc-number-step",
      tabIndex := -1,
      icon,
      // Keeps the focus where it was, as pressing a button would take it, and a
      // finger held on it from opening a menu.
      onMouseDown.preventDefault --> Observer.empty,
      onContextMenu.preventDefault --> Observer.empty,
      inContext(self =>
        EventStream
          .merge(
            self
              .events(onPointerDown.filter(_.button == 0).preventDefault)
              .mapTo(true),
            self.events(onPointerUp).mapTo(false),
            self.events(onPointerLeave).mapTo(false),
            self.events(onPointerCancel).mapTo(false),
          )
          .flatMapSwitch(if _ then repeats else EventStream.empty) -->
          (_ => step(typed.ref, steps)),
      ),
    )

  /** Once at once, then, after a pause, again and again until released. */
  private def repeats: EventStream[Unit] = EventStream.merge(
    EventStream.fromValue(()),
    EventStream
      .delay(400)
      .flatMapSwitch(_ => EventStream.periodic(60))
      .mapToUnit,
  )

  private def step(field: dom.html.Input, steps: Int): Unit =
    if !field.disabled && !field.readOnly then
      field.value = stepped(
        field.value,
        field.step,
        steps,
        field.min,
        field.max,
      )
      fire(field, "input")
      fire(field, "change")

  private def fire(field: dom.html.Input, kind: String): Unit = field
    .dispatchEvent(new dom.Event(
      kind,
      new dom.EventInit:
        bubbles = true,
    ))

  private def up: SvgElement = chevron("m6.5 14.5 5.5-5.5 5.5 5.5")

  private def down: SvgElement = chevron("m6.5 9.5 5.5 5.5 5.5-5.5")

  private def chevron(path: String): SvgElement = svg.svg(
    svg.cls            := "lc-number-arrow",
    svg.viewBox        := "0 0 24 24",
    svg.fill           := "none",
    svg.stroke         := "currentColor",
    svg.strokeLineCap  := "round",
    svg.strokeLineJoin := "round",
    ariaHidden         := "true",
    svg.path(svg.d := path),
  )

  private val ariaHidden = svg.svgAttr("aria-hidden", Codec.stringAsIs, None)

  /** The width of the arrows' column. */
  private val stepsWidth = styleProp[String]("--lc-number-steps")

  private val styles = Stylesheet.layered(Stylesheet.components)(
    // The number, centred in its room, then a column of two arrows forming the
    // box's end, edge to edge.
    rule(".lc-number-input")(
      stepsWidth := "20px",
      display.inlineFlex,
      alignItems.stretch,
      width := "calc(64px + var(--lc-number-steps))",
      minWidth.px(0),
      fontSize.px(13),
      color        := Theme.text.value,
      background   := Theme.field.value,
      border       := s"1px solid ${ Theme.border.value }",
      borderRadius := Theme.radiusSmall.value,
      overflow     := "hidden",
      transition   := "border-color 0.15s ease, box-shadow 0.15s ease",
      rule("&:focus-within")(
        borderColor := Theme.accent.value,
        boxShadow   := Theme.focusRing.value,
        background  := Theme.surface.value,
      ),
      rule("&:has(> input:disabled)")(
        opacity := 0.55,
        cursor  := "not-allowed",
        rule(".lc-number-step")(pointerEvents.none),
      ),
      rule("& > input")(
        flex := "1 1 auto",
        width.px(0),
        minWidth.px(0),
        padding            := "4px 8px",
        font               := "inherit",
        fontVariantNumeric := "tabular-nums",
        textAlign          := "center",
        color              := "inherit",
        background         := "transparent",
        border             := "0",
        outline            := "none",
        appearance         := "textfield",
      ),
      rule("& > input::-webkit-inner-spin-button")(
        margin.px(0),
        appearance := "none",
      ),
      rule("& > input::-webkit-outer-spin-button")(
        margin.px(0),
        appearance := "none",
      ),
      // A finger needs wider arrows than a pointer.
      media("(pointer: coarse)")(stepsWidth := "28px"),
    ),
    rule(".lc-number-steps")(
      display.flex,
      flex := "0 0 auto",
      flexDirection.column,
      width             := "var(--lc-number-steps)",
      borderInlineStart := s"1px solid ${ Theme.border.value }",
    ),
    rule(".lc-number-step")(
      display.grid,
      flex       := "1 1 0",
      placeItems := "center",
      minHeight.px(0),
      padding.px(0),
      color      := Theme.textMuted.value,
      background := "transparent",
      border     := "0",
      cursor.pointer,
      touchAction        := "manipulation",
      userSelect         := "none",
      webkitUserSelect   := "none",
      webkitTouchCallout := "none",
      transition         := "background-color 0.1s ease, color 0.1s ease",
      rule("& + .lc-number-step")(borderBlockStart := s"1px solid ${ Theme
          .border
          .value }"),
      rule("&:hover")(
        color      := Theme.accent.value,
        background := Theme.accentSoft.value,
      ),
      rule("&:active")(
        color      := "#ffffff",
        background := Theme.accent.value,
      ),
    ),
    rule(".lc-number-arrow")(
      width.px(11),
      height.px(11),
      strokeWidth := "3",
    ),
  )
