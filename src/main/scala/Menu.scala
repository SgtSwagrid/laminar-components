package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.rule
import com.alecdorrington.laminarcomponents.CssProps.{
  insetInlineEnd, overscrollBehavior,
}
import com.raquo.laminar.api.L.*

/**
  * A drop-down menu, closed by a choice, a click elsewhere, or Escape. Its own
  * clicks never reach the document, so opening one never closes it.
  *
  * Drawn as `.lc-menu`, its button `.lc-menu-toggle`, the list `.lc-menu-list`
  * and its choices `.lc-menu-item`.
  */
object Menu:

  /**
    * Renders a button opening a menu beneath it.
    *
    * @param toggle
    *   The button's content and modifiers.
    *
    * @param items
    *   The choices, built when the menu opens, given a function closing it.
    *
    * @return
    *   The button and its menu.
    */
  def apply
    (toggle: Modifier[HtmlElement]*)
    (items: (() => Unit) => List[HtmlElement])
    : HtmlElement =
    val flips    = new EventBus[Unit]
    val choices  = new EventBus[Unit]
    val closings = EventStream.merge(
      choices.events,
      documentEvents(_.onClick),
      documentEvents(_.onKeyDown).filter(_.key == "Escape"),
    )
    div(
      styles,
      cls := "lc-menu",
      button(
        cls := "lc-menu-toggle",
        toggle,
        onClick.stopPropagation.mapToUnit --> flips,
      ),
      child.maybe <--
        Toggle
          .flag(flips.events, closings)
          .map(shown =>
            Option.when(shown)(div(
              cls := "lc-menu-list",
              onClick --> (_.stopPropagation()),
              items(() => choices.emit(())),
            )),
          ),
    )

  /**
    * Renders a choice of a menu.
    *
    * @param close
    *   The function closing the menu, as [[apply]] gives it.
    *
    * @param act
    *   What choosing it does, after the menu closes.
    *
    * @param contents
    *   The choice's content and modifiers.
    *
    * @return
    *   The choice.
    */
  def item
    (close: () => Unit, act: () => Unit)
    (contents: Modifier[HtmlElement]*)
    : HtmlElement = button(
    cls := "lc-menu-item",
    contents,
    onClick --> { _ =>
      close()
      act()
    },
  )

  private val styles = Stylesheet.layered(Stylesheet.components)(
    rule(".lc-menu")(position.relative),
    rule(".lc-menu-toggle")(font := "inherit", cursor.pointer),
    rule(".lc-menu-list")(
      position.absolute,
      top            := "calc(100% + 4px)",
      insetInlineEnd := "0",
      zIndex         := 20,
      minWidth.px(160),
      maxHeight          := "min(420px, max(160px, calc(100vh - 80px)))",
      overflow           := "hidden auto",
      overscrollBehavior := "contain",
      padding.px(6),
      background   := Theme.surface.value,
      border       := s"1px solid ${ Theme.border.value }",
      borderRadius := Theme.radiusSmall.value,
      boxShadow    := Theme.shadow.value,
    ),
    rule(".lc-menu-item")(
      display.flex,
      alignItems.center,
      gap.px(8),
      width   := "100%",
      padding := "8px 10px",
      font    := "inherit",
      fontSize.px(13),
      textAlign  := "start",
      color      := Theme.text.value,
      background := "transparent",
      border     := "none",
      borderRadius.px(6),
      cursor.pointer,
      rule("&:hover")(
        color      := Theme.accent.value,
        background := Theme.accentSoft.value,
      ),
    ),
  )
