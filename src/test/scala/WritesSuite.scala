package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*
import munit.FunSuite

class WritesSuite extends FunSuite:

  private given Owner = new Owner {}

  /** Starts a write, yielding a function answering it with whether it took. */
  private def writing
    (writes: Writes, thing: AnyRef, value: Any)
    : Boolean => Unit =
    val answer = new EventBus[Boolean]
    writes.write(thing, value)(answer.events).foreach(_ => ())
    answer.emit

  test(
    "A read that a write began during, or was on its way for, is stale, and one no write crossed fresh.",
  ):
    val writes = Writes()
    assert(writes.fresh(writes.stamp))
    val field  = Var("a")
    val before = writes.stamp
    val answer = writing(writes, field, "b")
    val during = writes.stamp
    answer(true)
    assert(!writes.fresh(before))
    assert(!writes.fresh(during))
    assert(writes.fresh(writes.stamp))

  test("A value read replaces one unedited, and not one edited or on its way."):
    val writes = Writes()
    val field  = Var("stored")
    writes.hold(field, "stored")
    writes.adopt(field, "elsewhere")
    assertEquals(field.now(), "elsewhere")
    field.set("typed")
    writes.adopt(field, "again")
    assertEquals(field.now(), "typed")
    val answer = writing(writes, field, "typed")
    writes.adopt(field, "again")
    assertEquals(field.now(), "typed")
    answer(true)
    writes.adopt(field, "again")
    assertEquals(field.now(), "again")

  test("A value is held once the server takes it, or once on its way."):
    val writes = Writes()
    val field  = Var("stored")
    writes.hold(field, "stored")
    assert(writes.holds(field, "stored"))
    val answer = writing(writes, field, "next")
    assert(writes.holds(field, "next"))
    assert(!writes.holds(field, "stored"))
    answer(false)
    assert(writes.holds(field, "stored"))
    assert(!writes.holds(field, "next"))

  test(
    "A value is answered once held, or once written and answered, taken or not.",
  ):
    val writes = Writes()
    val field  = Var("stored")
    writes.hold(field, "stored")
    assert(writes.answered(field, "stored"))
    assert(!writes.answered(field, "typed"))
    val taken = writing(writes, field, "typed")
    assert(!writes.answered(field, "typed"))
    taken(true)
    assert(writes.answered(field, "typed"))
    val refused = writing(writes, field, "again")
    refused(false)
    assert(writes.answered(field, "again"))
    assert(!writes.answered(field, "never sent"))

  test("An older write taken after a newer one confirms nothing."):
    val writes = Writes()
    val field  = Var("stored")
    val older  = writing(writes, field, "older")
    val newer  = writing(writes, field, "newer")
    newer(true)
    older(true)
    assert(writes.holds(field, "newer"))

  test("The writes settle once the last is answered."):
    val writes  = Writes()
    var settled = 0
    writes.settled.foreach(_ => settled += 1)
    val first  = writing(writes, Var(0), 1)
    val second = writing(writes, Var(0), 2)
    first(true)
    assertEquals(settled, 0)
    second(true)
    assertEquals(settled, 1)
    assert(writes.idle)
