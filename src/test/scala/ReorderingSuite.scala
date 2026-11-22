package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Reordering.Extent
import munit.FunSuite

class ReorderingSuite extends FunSuite:

  // Three rows 6 apart: 0 to 100, 106 to 156 and 162 to 228.
  private val rows = Vector(
    Extent(0, 100),
    Extent(106, 50),
    Extent(162, 66),
  )

  private def shifts(order: Reordering): Vector[Double] = rows
    .indices
    .toVector
    .map(order.shift)

  /** Where each row is drawn, by its place, top first. */
  private def drawn(order: Reordering): Vector[Int] = rows
    .indices
    .toVector
    .sortBy(at => rows(at).top + order.shift(at))

  test(
    "A row lands past every middle its edge crossed, and no further, staying put until it crosses one.",
  ):
    assertEquals(Reordering(rows, 0, 30).to, 0)
    assertEquals(Reordering(rows, 0, 32).to, 1)
    assertEquals(Reordering(rows, 1, -55).to, 1)
    assertEquals(Reordering(rows, 1, -57).to, 0)
    assertEquals(Reordering(rows, 0, 1000).to, 2)
    assertEquals(Reordering(rows, 2, -1000).to, 0)
    assertEquals(Reordering(rows, 0, -1000).to, 0)
    assertEquals(Reordering(rows, 2, 1000).to, 2)

  test(
    "The dragged row follows the pointer, and the rest stay until passed, as does a row appearing mid-drag.",
  ):
    assertEquals(
      shifts(Reordering(rows, 1, 20)),
      Vector(0.0, 20.0, 0.0),
    )
    assertEquals(Reordering(rows, 0, 1000).shift(3), 0.0)

  test(
    "Rows passed move by the dragged row and its gap, closing up where it was.",
  ):
    // Going down, they move up; going up, down.
    assertEquals(
      shifts(Reordering(rows, 0, 150)),
      Vector(150.0, -106.0, -106.0),
    )
    assertEquals(
      shifts(Reordering(rows, 2, -200)),
      Vector(72.0, 72.0, -200.0),
    )

  test("Every row is drawn in the order it lands in."):
    List(
      Reordering(rows, 0, 40),
      Reordering(rows, 0, 150),
      Reordering(rows, 2, -200),
      Reordering(rows, 1, 70),
    ).foreach(order =>
      assertEquals(
        drawn(order),
        moved(
          rows.indices.toVector,
          order.from,
          order.to,
        ),
      ),
    )

  /** The rows with the one at `from` taken out and put back at `to`. */
  private def moved(rows: Vector[Int], from: Int, to: Int): Vector[Int] = rows
    .patch(from, Nil, 1)
    .patch(to, Vector(rows(from)), 0)
