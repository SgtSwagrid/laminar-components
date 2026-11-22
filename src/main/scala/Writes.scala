package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*
import scala.collection.mutable

/**
  * The writes this client makes to one part of the server's state, kept so that
  * a read of that part never undoes an edit made here:
  *
  *   - A read is used only if no write began while it was on its way, nor was
  *     on its way when it began. Otherwise it is read again once the writes are
  *     done ([[settled]]).
  *   - A value read replaces the one on screen only where that is what the
  *     server was last known to hold; otherwise it is an edit not yet sent.
  *
  * Values are recorded against the thing they belong to, usually the [[Var]]
  * holding them, compared by identity.
  */
final class Writes:

  private val held = mutable.Map.empty[AnyRef, Any]

  /**
    * The latest write of each thing, by its place among all writes, with its
    * value, so that an older write confirms nothing.
    */
  private val latest = mutable.Map.empty[AnyRef, (Int, Any)]

  private val sending = mutable.Map.empty[AnyRef, Int]

  private var begun = 0

  private var pending = 0

  private val done = new EventBus[Unit]

  /** Emits whenever the last write on its way has been answered. */
  val settled: EventStream[Unit] = done.events

  /** The mark to take as a read begins, to hand to [[fresh]] once it answers. */
  def stamp: Int = if pending > 0 then -1 else begun

  /**
    * Whether a read that began at a [[stamp]] may be used.
    *
    * @param stamp
    *   The mark taken as the read began.
    *
    * @return
    *   Whether the read may be used.
    */
  def fresh(stamp: Int): Boolean = stamp >= 0 && stamp == begun && pending == 0

  /** Whether no write is on its way. */
  def idle: Boolean = pending == 0

  /**
    * Records a value as the one the server holds, as when it is read or made.
    *
    * @param thing
    *   What the value belongs to.
    *
    * @param value
    *   The value.
    */
  def hold(thing: AnyRef, value: Any): Unit = held(thing) = value

  /**
    * Whether the server holds a value, or will once the writes on their way
    * land, so that writing it again is needless.
    *
    * @param thing
    *   What the value belongs to.
    *
    * @param value
    *   The value.
    *
    * @return
    *   Whether writing the value again is needless.
    */
  def holds(thing: AnyRef, value: Any): Boolean =
    if quiet(thing) then held.get(thing).contains(value)
    else lastSent(thing).contains(value)

  /**
    * Whether a value is what the server was last known to hold, with no write
    * of it on its way. A thing never recorded counts as unedited.
    *
    * @param thing
    *   What the value belongs to.
    *
    * @param value
    *   The value.
    *
    * @return
    *   Whether the value is unedited here.
    */
  def unedited(thing: AnyRef, value: Any): Boolean = quiet(thing) &&
    held.get(thing).forall(_ == value)

  /**
    * Whether the server has had its say on a value: no write of it is on its
    * way, and it is the value last held or the last written, taken or not.
    *
    * @param thing
    *   What the value belongs to.
    *
    * @param value
    *   The value.
    *
    * @return
    *   Whether the server has had its say on the value.
    */
  def answered(thing: AnyRef, value: Any): Boolean = unedited(thing, value) ||
    quiet(thing) && lastSent(thing).contains(value)

  /**
    * Sets a [[Var]] to a value read from the server, unless edited here.
    *
    * @tparam X
    *   The value.
    *
    * @param field
    *   The variable, which the value is recorded against.
    *
    * @param value
    *   The value read.
    */
  def adopt[X](field: Var[X], value: X): Unit =
    if unedited(field, field.now()) then
      held(field) = value
      if field.now() != value then field.set(value)

  /**
    * Counts a write of a value, recording it as held if the server takes it.
    *
    * The write counts from the moment this is called until `request` answers,
    * so subscribe at once ([[Requests.fire]]), and make `request` emit exactly
    * once, failures included. A write that never answers freezes every later
    * read of this part for good.
    *
    * @param thing
    *   What the value belongs to.
    *
    * @param value
    *   The value written.
    *
    * @param request
    *   The write, emitting once whether the server took it.
    *
    * @return
    *   The write, counted.
    */
  def write
    (thing: AnyRef, value: Any)
    (request: EventStream[Boolean])
    : EventStream[Boolean] =
    val mine = begin()
    latest(thing) = (mine, value)
    sending(thing) = sending.getOrElse(thing, 0) + 1
    request.map: taken =>
      if taken && latest.get(thing).exists(_._1 == mine) then
        held(thing) = value
      sending.updateWith(thing)(_.map(_ - 1).filter(_ > 0))
      end()
      taken

  /**
    * Counts a write of something with no recorded value, under the same rules
    * as [[write]].
    *
    * @tparam X
    *   The write's answer.
    *
    * @param request
    *   The write, emitting exactly once when answered, however it went.
    *
    * @return
    *   The write, counted.
    */
  def writing[X](request: EventStream[X]): EventStream[X] =
    begin()
    request.map: answer =>
      end()
      answer

  private def quiet(thing: AnyRef): Boolean = !sending.contains(thing)

  private def lastSent(thing: AnyRef): Option[Any] = latest.get(thing).map(_._2)

  private def begin(): Int =
    begun += 1
    pending += 1
    begun

  private def end(): Unit =
    pending -= 1
    if pending == 0 then done.emit(())
