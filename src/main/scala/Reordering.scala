package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Reordering.Extent

/**
  * A row of a list being dragged into a new place, against the rows as they lay
  * when the drag began, the rows it passes moving aside to make room.
  *
  * @param rows
  *   Each row's extent down the page, in order.
  *
  * @param from
  *   The dragged row's place.
  *
  * @param offset
  *   How far down it has been dragged, in pixels (up where negative).
  */
final case class Reordering
  (
    rows: Vector[Extent],
    from: Int,
    offset: Double,
  ):

  /**
    * Where the dragged row lands: past every row whose middle its leading edge
    * has crossed.
    */
  val to: Int = from +
    rows
      .indices
      .count(at => at > from && rows(at).middle < rows(from).bottom + offset) -
    rows
      .indices
      .count(at => at < from && rows(at).middle > rows(from).top + offset)

  /**
    * Measures how far down a row is drawn from its place: the dragged row as
    * far as it was dragged, and each row it passed by as much room as it takes.
    *
    * @param at
    *   The row's place.
    *
    * @return
    *   How far down it is drawn, in pixels (up where negative).
    */
  def shift(at: Int): Double =
    if at == from then offset
    else if from < at && at <= to then rows(from).top - rows(from + 1).top
    else if to <= at && at < from then rows(from).bottom - rows(from - 1).bottom
    else 0

object Reordering:

  /**
    * Where a row lies down the page, in pixels.
    *
    * @param top
    *   Where its top lies.
    *
    * @param height
    *   How tall it is.
    */
  final case class Extent(top: Double, height: Double):

    /** Where its bottom lies. */
    def bottom: Double = top + height

    /** Where its middle lies. */
    def middle: Double = top + height / 2
