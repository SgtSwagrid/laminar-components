package com.alecdorrington.laminarcomponents

import org.scalajs.dom

/**
  * Where an element's text stands on its baseline. Fonts differ by platform and
  * script, so the baseline is measured rather than assumed, as to draw a rule
  * beneath each line of text.
  */
object Baseline:

  /**
    * Measures how far below a line's top an element's text stands on its
    * baseline, in its font, size and line height, which a hidden, empty inline
    * block shows by standing on it. The block is laid beside the element, so
    * that it serves a field too.
    *
    * @param element
    *   The element, laid out on the page.
    *
    * @return
    *   The distance, in pixels, or `0` for an element laid out nowhere.
    */
  def of(element: dom.HTMLElement): Double =
    val style = dom.window.getComputedStyle(element)
    val probe = dom.document.createElement("div").asInstanceOf[dom.HTMLElement]
    val stand = dom.document.createElement("span").asInstanceOf[dom.HTMLElement]
    Option(element.closest("[lang]")).foreach(written =>
      probe.setAttribute("lang", written.getAttribute("lang")),
    )
    probe.style.position = "absolute"
    probe.style.visibility = "hidden"
    probe.style.whiteSpace = "pre"
    probe.style.fontFamily = style.fontFamily
    probe.style.fontSize = style.fontSize
    probe.style.fontWeight = style.fontWeight
    probe.style.fontStyle = style.fontStyle
    probe.style.lineHeight = style.lineHeight
    stand.style.display = "inline-block"
    probe.appendChild(stand)
    probe.appendChild(dom.document.createTextNode("x"))
    element.parentNode.appendChild(probe)
    val at = stand.getBoundingClientRect().bottom -
      probe.getBoundingClientRect().top
    element.parentNode.removeChild(probe)
    at

  /**
    * Moves an offset on the screen onto the nearest device pixel.
    *
    * @param at
    *   The offset, in CSS pixels.
    *
    * @return
    *   The offset moved.
    */
  def snapped(at: Double): Double =
    val ratio = dom.window.devicePixelRatio
    math.round(at * ratio) / ratio
