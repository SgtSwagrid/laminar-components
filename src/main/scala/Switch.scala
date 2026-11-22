package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.rule
import com.alecdorrington.laminarcomponents.CssProps.{
  accentColor, insetBlockStart, insetInlineStart, userSelect,
}
import com.raquo.laminar.api.L.*

/**
  * An on/off setting: a checkbox, drawn as a slider (`.lc-switch`, its slider
  * `.lc-switch-track`) with the `switch` role, or as a tick box (`.lc-check`).
  */
object Switch:

  /**
    * Renders a setting as a slider.
    *
    * @param caption
    *   The setting's name, beside it.
    *
    * @param hint
    *   The tooltip explaining it, none if empty.
    *
    * @param on
    *   Whether it is on.
    *
    * @param flips
    *   The observer told of each change, `true` for on.
    *
    * @param locked
    *   Whether it cannot be changed.
    *
    * @return
    *   The setting.
    */
  def apply
    (
      caption: String,
      hint: String,
      on: Signal[Boolean],
      flips: Observer[Boolean],
      locked: Signal[Boolean] = Val(false),
    )
    : HtmlElement = setting(
    "lc-switch",
    hint,
    on,
    flips,
    role := "switch",
    disabled <-- locked,
  )(
    span(cls := "lc-switch-track"),
    span(caption),
  )

  /**
    * Renders a setting as a tick box, as in a form.
    *
    * @param caption
    *   The setting's name, beside it.
    *
    * @param hint
    *   The tooltip explaining it, none if empty.
    *
    * @param on
    *   Whether it is on.
    *
    * @param flips
    *   The observer told of each change, `true` for on.
    *
    * @param locked
    *   Whether it cannot be changed.
    *
    * @return
    *   The setting.
    */
  def box
    (
      caption: String,
      hint: String,
      on: Signal[Boolean],
      flips: Observer[Boolean],
      locked: Signal[Boolean] = Val(false),
    )
    : HtmlElement = setting(
    "lc-check",
    hint,
    on,
    flips,
    disabled <-- locked,
  )(span(caption))

  private def setting
    (
      kind: String,
      hint: String,
      on: Signal[Boolean],
      flips: Observer[Boolean],
      marks: Modifier[HtmlElement]*,
    )
    (after: Modifier[HtmlElement]*)
    : HtmlElement = label(
    styles,
    cls := kind,
    Tooltip(hint),
    input(
      typ := "checkbox",
      marks,
      // Follows `input`, never `change`: Laminar refuses `change` for a
      // controlled `checked` at mount, leaving the box unbound.
      controlled(
        checked <-- on,
        onInput.mapToChecked --> flips,
      ),
    ),
    after,
  )

  private val styles = Stylesheet.layered(Stylesheet.components)(
    rule(".lc-check")(
      display.inlineFlex,
      alignItems.center,
      gap.px(6),
      fontSize.px(13),
      fontWeight := "500",
      color      := Theme.textMuted.value,
      cursor.pointer,
      userSelect := "none",
      rule("input")(
        accentColor := Theme.accent.value,
        cursor.pointer,
      ),
    ),
    rule(".lc-switch")(
      position.relative,
      display.inlineFlex,
      alignItems.center,
      gap.px(8),
      maxWidth := "100%",
      fontSize.px(13),
      fontWeight := "500",
      color      := Theme.textMuted.value,
      cursor.pointer,
      userSelect := "none",
      rule("input")(
        position.absolute,
        width.px(1),
        height.px(1),
        margin.px(0),
        opacity := 0,
        pointerEvents.none,
      ),
      rule("input:checked + .lc-switch-track")(
        background := Theme.accent.value,
        rule("&::after")(insetInlineStart := "16px"),
      ),
      rule("input:focus-visible + .lc-switch-track")(
        boxShadow := Theme.focusRing.value,
      ),
      rule("&:has(input:disabled)")(opacity := 0.55, cursor := "default"),
    ),
    rule(".lc-switch-track")(
      position.relative,
      flex := "0 0 auto",
      width.px(32),
      height.px(18),
      borderRadius.px(999),
      background := Theme.borderStrong.value,
      transition := "background 0.15s ease",
      rule("&::after")(
        contentCss := "\"\"",
        position.absolute,
        insetBlockStart  := "2px",
        insetInlineStart := "2px",
        width.px(14),
        height.px(14),
        borderRadius := "50%",
        background   := "#ffffff",
        boxShadow    := Theme.shadowSmall.value,
        transition   := "inset-inline-start 0.15s ease",
      ),
    ),
  )
