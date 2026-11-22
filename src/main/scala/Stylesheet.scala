package com.alecdorrington.laminarcomponents

import com.raquo.laminar.modifiers.Modifier
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom

/**
  * A stylesheet written in Scala ([[Css]]), kept beside the view it styles and
  * given to the view's element as a modifier: the first element given it adds
  * it to the page's head, where it stays.
  *
  * @param layer
  *   The cascade layer its rules go in, if any. Rules in no layer outweigh
  *   those in any layer, whatever their selectors, so that a page restyles a
  *   layered component without matching its selectors.
  *
  * @param blocks
  *   Its rules, in order.
  */
final class Stylesheet private (
  val layer: Option[String],
  val blocks: Seq[Css.Block],
) extends Modifier[ReactiveElement.Base]:

  /** The stylesheet's CSS. */
  lazy val css: String =
    val rules = Css.render(blocks)
    layer.fold(rules)(name => s"@layer $name {\n$rules\n}")

  /** The stylesheet's element in the page's head, added when first read. */
  private lazy val added: dom.Node =
    val style = dom.document.createElement("style")
    layer.foreach(style.setAttribute("data-layer", _))
    style.textContent = css
    dom.document.head.appendChild(style)

  /** Adds the stylesheet to the page's head, unless it is there already. */
  def add(): Unit = added: Unit

  /**
    * Adds the stylesheet as its element is made.
    *
    * @param element
    *   The element styled.
    */
  override def apply(element: ReactiveElement.Base): Unit = add()

object Stylesheet:

  /**
    * Makes a stylesheet whose rules are in no cascade layer.
    *
    * @param blocks
    *   Its rules, in order.
    *
    * @return
    *   The stylesheet.
    */
  def apply(blocks: Css.Block*): Stylesheet = new Stylesheet(None, blocks)

  /**
    * Makes a stylesheet whose rules are in a cascade layer.
    *
    * @param layer
    *   The layer's name.
    *
    * @param blocks
    *   Its rules, in order.
    *
    * @return
    *   The stylesheet.
    */
  def layered(layer: String)(blocks: Css.Block*): Stylesheet =
    new Stylesheet(Some(layer), blocks)

  /**
    * Settles the order of cascade layers, the later outweighing the earlier,
    * before any stylesheet names one. A layer otherwise takes its place when
    * first named.
    *
    * @param layers
    *   The layers' names, weakest first.
    */
  def order(layers: String*): Unit =
    val style = dom.document.createElement("style")
    style.textContent = layers.mkString("@layer ", ", ", ";")
    dom.document.head.prepend(style)

  /** The layer this library's own components are styled in. */
  val components: String = "laminar-components"
