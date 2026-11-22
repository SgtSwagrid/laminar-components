package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*

/** A drop-down of options, each sent under its code, one of them chosen. */
object Dropdown:

  /**
    * Renders a `select` offering each option in its wording, telling `choose`
    * of every pick.
    *
    * @tparam A
    *   An option.
    *
    * @param options
    *   The options, each with its wording, in order.
    *
    * @param code
    *   The code each option is sent under, unique among them.
    *
    * @param chosen
    *   The option chosen.
    *
    * @param choose
    *   The callback given each option picked.
    *
    * @param offered
    *   Modifiers of each option's element, as to disable some.
    *
    * @return
    *   The drop-down.
    */
  def apply[A]
    (
      options: List[(A, String)],
      code: A => String,
      chosen: Signal[A],
      choose: A => Unit,
      offered: A => Modifier[HtmlElement] = (_: A) => emptyMod,
    )
    : HtmlElement = select(
    onChange.mapToValue -->
      (picked =>
        options
          .collectFirst { case (one, _) if code(one) == picked => one }
          .foreach(choose)
      ),
    options.map((one, wording) =>
      option(
        value := code(one),
        wording,
        selected <-- chosen.map(_ == one),
        offered(one),
      ),
    ),
  )
