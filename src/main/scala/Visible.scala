package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*

/** Whether an element is on the page, as a signal decides. */
object Visible:

  /**
    * Shows an element only while a signal says so, hiding it otherwise.
    *
    * @param shown
    *   Whether the element is shown.
    *
    * @return
    *   The modifier showing and hiding the element.
    */
  def when(shown: Signal[Boolean]): Modifier[HtmlElement] = display <--
    shown.map(if _ then "" else "none")
