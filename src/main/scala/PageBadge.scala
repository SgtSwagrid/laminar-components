package com.alecdorrington.laminarcomponents

import org.scalajs.dom
import scala.scalajs.js

/**
  * A count of what awaits the reader, shown beyond the page while it is not
  * `0`: before the page's title, in red on the tab's icon, and on an installed
  * app's icon in the taskbar or dock.
  *
  * @param icon
  *   The URL of the page's icon, which the count is drawn over.
  */
final class PageBadge(icon: String):

  private lazy val title = dom.document.title

  private val size = 64

  /** The attributes of an icon link that drawing the count changes. */
  private val swapped = List("href", "type", "sizes")

  /** The page's icon links, each with the attributes to restore. */
  private lazy val links: List[(dom.Element, Map[String, String])] =
    val found = dom.document.querySelectorAll("link[rel~='icon']")
    (0 until found.length)
      .toList
      .map(found(_))
      .map(link =>
        link ->
          swapped
            .flatMap(name => Option(link.getAttribute(name)).map(name -> _))
            .toMap,
      )

  /** The page's own icon, drawn once loaded. */
  private lazy val image: dom.html.Image =
    val loading = dom.document.createElement("img").asInstanceOf[dom.html.Image]
    loading.onload = _ => draw(wanted)
    loading.src = icon
    loading

  private var wanted = 0

  /**
    * Shows a count.
    *
    * @param count
    *   The count, none shown if `0`.
    */
  def show(count: Int): Unit =
    dom.document.title = if count == 0 then title else s"($count) $title"
    wanted = count
    if count == 0 then restore()
    else if image.complete && image.naturalWidth > 0 then draw(count)
    appBadge(count)

  /**
    * Badges an installed app's icon, where the browser can (the App Badging
    * API); a page in an ordinary tab has no icon of its own to badge.
    */
  private def appBadge(count: Int): Unit =
    val navigator = dom.window.navigator.asInstanceOf[js.Dynamic]
    if !js.isUndefined(navigator.setAppBadge) then
      val badged =
        if count == 0 then navigator.clearAppBadge()
        else navigator.setAppBadge(count)
      badged.`catch`((_: js.Any) => ())

  /** Draws the count over the icon, swapped in for every icon link. */
  private def draw(count: Int): Unit = if count > 0 then
    val canvas = dom
      .document
      .createElement("canvas")
      .asInstanceOf[dom.html.Canvas]
    canvas.width = size
    canvas.height = size
    val pen = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    pen.drawImage(image, 0, 0, size, size)
    pen.beginPath()
    pen.arc(size - 20, 20, 19, 0, 2 * math.Pi)
    pen.fillStyle = "#d92d20"
    pen.fill()
    pen.lineWidth = 3
    pen.strokeStyle = "#ffffff"
    pen.stroke()
    pen.fillStyle = "#ffffff"
    pen.font = "bold 26px sans-serif"
    pen.textAlign = "center"
    pen.textBaseline = "middle"
    pen.fillText(
      if count > 9 then "9+" else count.toString,
      size - 20,
      21,
    )
    val drawn = canvas.toDataURL("image/png")
    links.foreach: (link, _) =>
      link.setAttribute("href", drawn)
      link.setAttribute("type", "image/png")
      link.removeAttribute("sizes")

  private def restore(): Unit = links.foreach: (link, original) =>
    swapped.foreach(name =>
      original
        .get(name)
        .fold(link.removeAttribute(name))(link.setAttribute(name, _)),
    )
