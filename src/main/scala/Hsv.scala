package com.alecdorrington.laminarcomponents

import scala.util.matching.Regex

/**
  * A colour by hue, saturation and value. Unlike a `#rrggbb` code, it keeps its
  * hue while grey.
  *
  * @param hue
  *   The hue, in degrees, from `0` to `360`.
  *
  * @param saturation
  *   The saturation, from `0` to `1`.
  *
  * @param value
  *   The value, from `0` (black) to `1`.
  */
final case class Hsv
  (
    hue: Double,
    saturation: Double,
    value: Double,
  ):

  /** The `#rrggbb` code. */
  def code: String =
    f"#${ channel(5) }%02x${ channel(3) }%02x${ channel(1) }%02x"

  /** The same hue at full saturation and value. */
  def pure: Hsv = Hsv(hue, 1, 1)

  /** Computes a channel as a byte: red (`5`), green (`3`) or blue (`1`). */
  private def channel(n: Int): Int =
    val k      = (n + hue / 60) % 6
    val amount = value - value * saturation * (k min (4 - k) min 1 max 0)
    math.round(amount * 255).toInt

object Hsv:

  private val digits: Regex = "[0-9a-fA-F]{6}".r

  /**
    * Parses a `#rrggbb` code, with or without its `#`.
    *
    * @param code
    *   The code.
    *
    * @return
    *   The colour, if the code is one.
    */
  def parse(code: String): Option[Hsv] = Some(code.trim.stripPrefix("#"))
    .filter(digits.matches)
    .map(_.grouped(2).map(Integer.parseInt(_, 16) / 255.0).toVector)
    .map(rgb => of(rgb(0), rgb(1), rgb(2)))

  private def of(r: Double, g: Double, b: Double): Hsv =
    val high   = r max g max b
    val chroma = high - (r min g min b)
    Hsv(
      hue(r, g, b, high, chroma),
      if high == 0 then 0 else chroma / high,
      high,
    )

  private def hue
    (
      r: Double,
      g: Double,
      b: Double,
      high: Double,
      chroma: Double,
    )
    : Double =
    if chroma == 0 then 0
    else if high == r then 60 * (((g - b) / chroma + 6) % 6)
    else if high == g then 60 * ((b - r) / chroma + 2)
    else 60 * ((r - g) / chroma + 4)
