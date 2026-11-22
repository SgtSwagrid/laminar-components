package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*

/**
  * Something that opens and closes, such as a menu or a popover, folded from
  * every cause of either.
  */
object Toggle:

  /**
    * Folds openings and closings into what is open: an opening's value while
    * closed, and `None` after a second opening or any closing.
    *
    * @tparam A
    *   What an opening opens.
    *
    * @param openings
    *   The openings, each with what it opens.
    *
    * @param closings
    *   The closings.
    *
    * @return
    *   What is open, if anything.
    */
  def apply[A]
    (
      openings: EventStream[A],
      closings: EventStream[Any],
    )
    : Signal[Option[A]] = openings
    .map[Option[A]](Some(_))
    .mergeWith(closings.mapTo(None))
    .scanLeft(Option.empty[A])((open, next) => next.filter(_ => open.isEmpty))
    .distinct

  /**
    * Folds flips and closings into whether something is open.
    *
    * @param flips
    *   The flips, each opening it if closed, else closing it.
    *
    * @param closings
    *   The closings.
    *
    * @return
    *   Whether it is open.
    */
  def flag
    (
      flips: EventStream[Any],
      closings: EventStream[Any] = EventStream.empty,
    )
    : Signal[Boolean] = apply(flips.mapToUnit, closings).map(_.isDefined)
