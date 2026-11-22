package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.rule
import com.alecdorrington.laminarcomponents.CssProps.{
  appearance, aspectRatio, gridTemplateColumns, insetInlineStart, touchAction,
  userSelect,
}
import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.Codec
import org.scalajs.dom

/**
  * A swatch opening a popover that chooses a colour from given swatches, by
  * shade and hue, or by its `#rrggbb` code. The popover is `position: fixed`
  * and placed again beside its swatch on every scroll, so that a scrolling list
  * cannot clip it. A colour is chosen only once a gesture settles (a drag let
  * go), never mid-drag, so that choosing may ask a question or save.
  *
  * Drawn as `.lc-colour-picker`, its swatch `.lc-colour-swatch`, and its
  * popover `.lc-colour-popover`, holding the `.lc-colour-presets`
  * (`.lc-colour-preset`), the `.lc-colour-field` of shades (its thumb
  * `.lc-colour-field-thumb`), the `.lc-colour-hue` slider and the
  * `.lc-colour-code` field.
  */
object ColourPicker:

  /**
    * What a colour picker's controls are called, as screen readers say.
    *
    * @param colour
    *   The swatch, the popover and its code field, as "Colour".
    *
    * @param shade
    *   The field of shades, as "Shade".
    *
    * @param hue
    *   The hue slider, as "Hue".
    */
  final case class Labels(colour: String, shade: String, hue: String)

  /**
    * Renders a colour picker.
    *
    * @param colour
    *   The `#rrggbb` code of the colour chosen.
    *
    * @param choose
    *   The callback given each newly chosen `#rrggbb` code, never the colour
    *   already chosen.
    *
    * @param swatches
    *   The `#rrggbb` codes offered first, in order.
    *
    * @param labels
    *   What its controls are called.
    *
    * @return
    *   The picker.
    */
  def apply
    (
      colour: Signal[String],
      choose: String => Unit,
      swatches: Seq[String],
      labels: Labels,
    )
    : HtmlElement =
    val draft   = Var(Hsv(0, 0, 0))
    val flips   = new EventBus[Unit]
    val commits = new EventBus[String]
    div(
      styles,
      cls := "lc-colour-picker",
      commits.events.withCurrentValueOf(colour) -->
        { (code, chosen) => if code != chosen then choose(code) },
      inContext: picker =>
        val open = Toggle.flag(
          flips.events,
          EventStream.merge(
            documentEvents(_.onClick).filterNot(event =>
              picker.ref.contains(event.target.asInstanceOf[dom.Node]),
            ),
            documentEvents(_.onKeyDown).filter(_.key == "Escape"),
          ),
        )
        List(
          swatch(colour, open, draft, flips.writer, labels),
          child.maybe <-- open.map(Option.when(_)(popover(
            picker.ref,
            draft,
            commits.writer,
            swatches,
            labels,
          ))),
        ),
    )

  private def swatch
    (
      colour: Signal[String],
      open: Signal[Boolean],
      draft: Var[Hsv],
      flip: Observer[Unit],
      labels: Labels,
    )
    : HtmlElement = button(
    typ        := "button",
    cls        := "lc-colour-swatch",
    aria.label := labels.colour,
    hasPopup   := "dialog",
    aria.expanded <-- open,
    backgroundColor <--
      open
        .combineWith(draft.signal, colour)
        .mapN((shown, chosen, code) => if shown then chosen.code else code),
    onClick.compose(_.withCurrentValueOf(open, colour)) --> {
      (_, shown, code) =>
        if !shown then Hsv.parse(code).foreach(draft.set)
        flip.onNext(())
    },
  )

  private def popover
    (
      exemplar: dom.Element,
      draft: Var[Hsv],
      choose: Observer[String],
      swatches: Seq[String],
      labels: Labels,
    )
    : HtmlElement =
    val pick = (colour: Hsv) =>
      draft.set(colour)
      choose.onNext(colour.code)
    val settle = () => choose.onNext(draft.now().code)
    div(
      cls        := "lc-colour-popover",
      role       := "dialog",
      aria.label := labels.colour,
      onMountCallback(context => place(context.thisNode.ref, exemplar)),
      inContext(popover =>
        List(
          documentEvents(_.onScroll.useCapture),
          windowEvents(_.onResize),
        ).map(_ --> (_ => place(popover.ref, exemplar))),
      ),
      presets(draft, pick, swatches),
      field(draft, pick, settle, labels),
      hue(draft, settle, labels),
      code(draft, pick, labels),
    )

  private def presets
    (
      draft: Var[Hsv],
      pick: Hsv => Unit,
      swatches: Seq[String],
    )
    : HtmlElement = div(
    cls := "lc-colour-presets",
    swatches
      .flatMap(preset => Hsv.parse(preset).map(colour => (preset, colour)))
      .map((preset, colour) =>
        button(
          typ := "button",
          cls := "lc-colour-preset",
          Tooltip(preset),
          aria.label := preset,
          aria.pressed <-- draft.signal.map(c => (c.code == preset).toString),
          backgroundColor := preset,
          color           := preset,
          onClick --> (_ => pick(colour)),
        ),
      ),
  )

  private def field
    (
      draft: Var[Hsv],
      pick: Hsv => Unit,
      settle: () => Unit,
      labels: Labels,
    )
    : HtmlElement = div(
    cls        := "lc-colour-field",
    dir        := "ltr",
    tabIndex   := 0,
    role       := "slider",
    aria.label := labels.shade,
    aria.valueText <-- draft.signal.map(_.code),
    backgroundColor <-- draft.signal.map(_.pure.code),
    inContext(field =>
      List(
        onPointerDown --> { event =>
          field.ref.setPointerCapture(event.pointerId)
          draft.update(pointed(field.ref, event))
        },
        onPointerMove.filter(event =>
          field.ref.hasPointerCapture(event.pointerId),
        ) --> (event => draft.update(pointed(field.ref, event))),
        onPointerUp.filter(event =>
          field.ref.hasPointerCapture(event.pointerId),
        ) --> (_ => settle()),
      ),
    ),
    onKeyDown.filter(event => arrows.contains(event.key)).preventDefault -->
      (event => pick(nudged(draft.now(), event))),
    div(
      cls := "lc-colour-field-thumb",
      insetInlineStart <-- draft.signal.map(c => percent(c.saturation)),
      top <-- draft.signal.map(c => percent(1 - c.value)),
    ),
  )

  private def pointed
    (
      field: dom.Element,
      event: dom.PointerEvent,
    )
    (colour: Hsv)
    : Hsv =
    val box = field.getBoundingClientRect()
    colour.copy(
      saturation = unit((event.clientX - box.left) / box.width),
      value = 1 - unit((event.clientY - box.top) / box.height),
    )

  private val arrows: Map[String, (Int, Int)] = Map(
    "ArrowLeft"  -> (-1, 0),
    "ArrowRight" -> (1, 0),
    "ArrowUp"    -> (0, 1),
    "ArrowDown"  -> (0, -1),
  )

  private def nudged(colour: Hsv, event: dom.KeyboardEvent): Hsv =
    val step     = if event.shiftKey then 0.1 else 0.01
    val (ds, dv) = arrows(event.key)
    colour.copy(
      saturation = unit(colour.saturation + ds * step),
      value = unit(colour.value + dv * step),
    )

  private def hue
    (
      draft: Var[Hsv],
      settle: () => Unit,
      labels: Labels,
    )
    : HtmlElement = input(
    typ        := "range",
    cls        := "lc-colour-hue",
    dir        := "ltr",
    minAttr    := "0",
    maxAttr    := "360",
    stepAttr   := "1",
    aria.label := labels.hue,
    controlled(
      value <-- draft.signal.map(_.hue.round.toString),
      onInput.mapToValue -->
        (raw =>
          raw.toDoubleOption.foreach(hue => draft.update(_.copy(hue = hue)))
        ),
    ),
    onChange --> (_ => settle()),
  )

  private def code
    (
      draft: Var[Hsv],
      pick: Hsv => Unit,
      labels: Labels,
    )
    : HtmlElement = input(
    typ        := "text",
    cls        := "lc-colour-code",
    dir        := "ltr",
    maxLength  := 7,
    spellCheck := false,
    aria.label := labels.colour,
    value <-- draft.signal.map(_.code),
    onInput.mapToValue --> (text => Hsv.parse(text).foreach(pick)),
    inContext(field => onBlur --> (_ => field.ref.value = draft.now().code)),
  )

  private def place
    (
      popover: dom.HTMLElement,
      exemplar: dom.Element,
    )
    : Unit =
    val box  = exemplar.getBoundingClientRect()
    val page = dom.document.documentElement
    // Fixed positions are physical in either direction, so this sets `left`,
    // and the direction picks only which edge of the anchor to start from.
    val start =
      if dom.window.getComputedStyle(exemplar).direction == "rtl" then
        box.right - popover.offsetWidth
      else box.left
    val height = popover.offsetHeight
    popover.style.left = pixels(within(
      start,
      popover.offsetWidth,
      page.clientWidth,
    ))
    popover.style.top = pixels(within(
      beside(box, height, page.clientHeight),
      height,
      page.clientHeight,
    ))

  private def beside
    (
      box: dom.DOMRect,
      height: Double,
      room: Double,
    )
    : Double =
    val below = box.bottom + apart
    if below + height <= room - clearance || room - box.bottom >= box.top then
      below
    else box.top - apart - height

  private def pixels(length: Double): String = s"${ length }px"

  private def within(at: Double, size: Double, room: Double): Double = at min
    (room - size - clearance) max clearance

  private val apart = 6.0

  private val clearance = 8.0

  private def unit(fraction: Double): Double = fraction max 0 min 1

  private def percent(fraction: Double): String = s"${ fraction * 100 }%"

  private val hasPopup: HtmlAttr[String] =
    htmlAttr("aria-haspopup", Codec.stringAsIs)

  /** The ring of a swatch, beneath any other shadow it is given. */
  private val ring = "inset 0 0 0 1px rgba(16, 24, 40, 0.12)"

  private val thumbShadow =
    "0 0 0 1px rgba(16, 24, 40, 0.3), 0 1px 3px rgba(16, 24, 40, 0.3)"

  private val styles = Stylesheet.layered(Stylesheet.components)(
    rule(".lc-colour-picker")(display.inlineFlex, flex := "0 0 auto"),
    rule(".lc-colour-swatch")(
      width.px(26),
      height.px(26),
      padding.px(0),
      border       := "none",
      borderRadius := "50%",
      boxShadow    := ring,
      cursor.pointer,
      transition := "box-shadow 0.15s ease",
      rule("&:hover, &[aria-expanded='true']")(
        boxShadow := s"$ring, 0 0 0 3px ${ Theme.accentSoft.value }",
      ),
      rule("&:focus-visible")(
        outline   := "none",
        boxShadow := s"$ring, ${ Theme.focusRing.value }",
      ),
    ),
    rule(".lc-colour-popover")(
      position.fixed,
      zIndex := 30,
      display.flex,
      flexDirection.column,
      gap.px(12),
      width.px(232),
      maxWidth := "calc(100vw - 16px)",
      padding.px(12),
      background   := Theme.surface.value,
      border       := s"1px solid ${ Theme.border.value }",
      borderRadius := Theme.radius.value,
      boxShadow    := "0 12px 32px rgba(16, 24, 40, 0.16)",
    ),
    rule(".lc-colour-presets")(
      display.grid,
      gridTemplateColumns := "repeat(8, 1fr)",
      gap.px(6),
    ),
    // Its text colour is its own, so that its ring when chosen matches it.
    rule(".lc-colour-preset")(
      aspectRatio := "1",
      padding.px(0),
      border       := "none",
      borderRadius := "50%",
      boxShadow    := ring,
      cursor.pointer,
      transition := "transform 0.1s ease, box-shadow 0.1s ease",
      rule("&:hover")(transform                := "scale(1.15)"),
      rule("&[aria-pressed='true']")(boxShadow := s"0 0 0 2px ${ Theme
          .surface
          .value }, 0 0 0 4px currentColor"),
      rule("&:focus-visible")(
        outline   := "none",
        boxShadow := s"0 0 0 2px ${ Theme.surface.value }, 0 0 0 4px ${ Theme
            .accent
            .value }",
      ),
    ),
    // White across to the hue, then black down over both; the hue is the
    // background colour, set from the script.
    rule(".lc-colour-field")(
      position.relative,
      height.px(140),
      borderRadius    := Theme.radiusSmall.value,
      backgroundImage :=
        "linear-gradient(to top, #000, transparent), linear-gradient(to right, #fff, transparent)",
      cursor      := "crosshair",
      touchAction := "none",
      userSelect  := "none",
      rule("&:focus-visible")(
        outline   := "none",
        boxShadow := Theme.focusRing.value,
      ),
    ),
    rule(".lc-colour-field-thumb")(
      position.absolute,
      width.px(14),
      height.px(14),
      border       := "2px solid #fff",
      borderRadius := "50%",
      boxShadow    := thumbShadow,
      transform    := "translate(-50%, -50%)",
      pointerEvents.none,
    ),
    rule(".lc-colour-hue")(
      width := "100%",
      height.px(12),
      margin.px(0),
      borderRadius.px(6),
      background :=
        "linear-gradient(to right, #f00 0%, #ff0 16.67%, #0f0 33.33%, #0ff 50%, #00f 66.67%, #f0f 83.33%, #f00 100%)",
      appearance := "none",
      cursor.pointer,
      rule("&::-webkit-slider-thumb")(
        width.px(16),
        height.px(16),
        border       := "2px solid #fff",
        borderRadius := "50%",
        background   := "transparent",
        boxShadow    := thumbShadow,
        appearance   := "none",
      ),
      rule("&::-moz-range-thumb")(
        width.px(12),
        height.px(12),
        border       := "2px solid #fff",
        borderRadius := "50%",
        background   := "transparent",
        boxShadow    := thumbShadow,
      ),
      rule("&:focus-visible")(
        outline   := "none",
        boxShadow := Theme.focusRing.value,
      ),
    ),
    rule(".lc-colour-code")(
      width      := "100%",
      padding    := "6px 10px",
      fontFamily := "ui-monospace, 'Cascadia Mono', Consolas, monospace",
      fontSize.px(13),
      color        := Theme.text.value,
      background   := Theme.field.value,
      border       := s"1px solid ${ Theme.border.value }",
      borderRadius := Theme.radiusSmall.value,
      outline      := "none",
      transition   := "border-color 0.15s ease, box-shadow 0.15s ease",
      rule("&:focus")(
        borderColor := Theme.accent.value,
        boxShadow   := Theme.focusRing.value,
        background  := Theme.surface.value,
      ),
    ),
  )
