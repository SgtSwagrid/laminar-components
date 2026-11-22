package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.rule
import com.raquo.laminar.api.L.*
import scala.collection.mutable

/**
  * Tabs over one pane at a time, each pane built when its tab is first opened
  * and kept, so that a half-filled form survives a look at another tab. Which
  * tab is open is a mode of the page, not where it is.
  *
  * Drawn as `.lc-tabs`, its tabs `.lc-tab` (`.lc-tab-current` while open) in a
  * `.lc-tab-strip`, each name a `.lc-tab-label`.
  */
object Tabs:

  /**
    * A tab and the pane it opens.
    *
    * @param label
    *   The tab's name.
    *
    * @param content
    *   The pane's builder, run once, when the tab is first opened.
    *
    * @param icon
    *   An icon drawn before its name, if any.
    *
    * @param mark
    *   Anything drawn after its name, such as a count.
    */
  final case class Tab
    (
      label: String,
      content: () => HtmlElement,
      icon: Option[SvgElement] = None,
      mark: Modifier[HtmlElement] = emptyMod,
    )

  /**
    * Renders tabs, opening each as it is chosen, the first to begin with.
    *
    * @param tabs
    *   The tabs, at least one.
    *
    * @return
    *   The tabs and the open tab's pane.
    */
  def apply(tabs: List[Tab]): HtmlElement =
    val choices = new EventBus[Int]
    apply(
      tabs,
      choices.events.startWith(0),
      choices.writer,
    )

  /**
    * Renders tabs, the open one decided by the caller.
    *
    * @param tabs
    *   The tabs, at least one.
    *
    * @param current
    *   The open tab's place.
    *
    * @param choose
    *   The observer told the place of each tab chosen.
    *
    * @return
    *   The tabs and the open tab's pane.
    */
  def apply
    (
      tabs: List[Tab],
      current: Signal[Int],
      choose: Observer[Int],
    )
    : HtmlElement =
    val built = mutable.Map.empty[Int, HtmlElement]
    val open  = current.distinct
    div(
      styles,
      cls := "lc-tabs",
      div(
        cls  := "lc-tab-strip",
        role := "tablist",
        tabs.zipWithIndex.map((tab, index) => handle(tab, index, open, choose)),
      ),
      child <--
        open.map(index => built.getOrElseUpdate(index, tabs(index).content())),
    )

  private def handle
    (
      tab: Tab,
      index: Int,
      current: Signal[Int],
      choose: Observer[Int],
    )
    : HtmlElement =
    val open = current.map(_ == index)
    button(
      cls  := "lc-tab",
      role := "tab",
      cls("lc-tab-current") <-- open,
      aria.selected <-- open,
      onClick.mapTo(index) --> choose,
      tab.icon,
      span(tab.label, cls := "lc-tab-label"),
      tab.mark,
    )

  private val styles = Stylesheet.layered(Stylesheet.components)(
    rule(".lc-tab-strip")(
      display.flex,
      flexWrap.wrap,
      gap.px(4),
      marginBottom.px(16),
      borderBottom := s"1px solid ${ Theme.border.value }",
    ),
    rule(".lc-tab")(
      padding := "8px 14px",
      font    := "inherit",
      fontSize.px(13),
      fontWeight   := "500",
      color        := Theme.textMuted.value,
      background   := "none",
      border       := "none",
      borderBottom := "2px solid transparent",
      marginBottom.px(-1),
      cursor.pointer,
      rule("&:hover")(color := Theme.text.value),
    ),
    rule(".lc-tab-current")(
      color             := Theme.accent.value,
      borderBottomColor := Theme.accent.value,
    ),
  )
