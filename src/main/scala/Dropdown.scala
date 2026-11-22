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
    : HtmlElement = changing(
    Val(options),
    code,
    chosen,
    choose,
    offered,
  )

  /**
    * Renders a `select` as [[apply]] does, offering the options a signal gives
    * as they change. Each option is matched by its code, so that one whose
    * wording changes keeps its element, and stays chosen while `chosen` gives
    * an option of its code. While `chosen` gives none of those offered, the
    * browser shows the first as chosen.
    *
    * @tparam A
    *   An option.
    *
    * @param options
    *   The options, each with its wording, in order, as they change.
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
    *   Modifiers of each option's element, as to disable some, given the option
    *   as first offered under its code.
    *
    * @return
    *   The drop-down.
    */
  def changing[A]
    (
      options: Signal[List[(A, String)]],
      code: A => String,
      chosen: Signal[A],
      choose: A => Unit,
      offered: A => Modifier[HtmlElement] = (_: A) => emptyMod,
    )
    : HtmlElement = select(
    onChange.mapToValue.compose(_.withCurrentValueOf(options)) -->
      ((picked, listed) =>
        listed
          .collectFirst { case (one, _) if code(one) == picked => one }
          .foreach(choose)
      ),
    children <-- options.splitSeq((one, _) => code(one))(entry =>
      option(
        value := entry.key,
        child.text <-- entry.map(_._2),
        selected <-- chosen.map(code(_) == entry.key).distinct,
        offered(entry.now()._1),
      ),
    ),
  )
