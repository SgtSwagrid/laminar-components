package com.alecdorrington.laminarcomponents

import com.raquo.airstream.ownership.ManualOwner
import com.raquo.laminar.api.L.*

/** Requests made from event handlers, each followed only until it answers. */
object Requests:

  /**
    * Runs a request from a handler, releasing its subscription once it answers.
    *
    * Always write the second argument list, even empty: `fire(request)()`. A
    * bare `fire(request)` is a function awaiting its handler; discarded, it
    * compiles silently and sends nothing.
    *
    * @tparam X
    *   The answer.
    *
    * @param request
    *   The request, sent when followed, emitting its answer once.
    *
    * @param handle
    *   The handler of its answer.
    */
  def fire[X](request: EventStream[X])(handle: X => Unit = (_: X) => ()): Unit =
    val owner   = new ManualOwner
    given Owner = owner
    request.foreach: answer =>
      handle(answer)
      owner.killSubscriptions()

  /**
    * Fires each key's changes one at a time, so that two quick changes never
    * apply out of order. One made while its key is in flight waits, replacing
    * any already waiting, as only the latest matters.
    *
    * @tparam K
    *   What a change is to.
    *
    * @tparam V
    *   A change.
    *
    * @param send
    *   The request making a change, emitting once when answered.
    */
  final class Serial[K, V](send: (K, V) => EventStream[Unit]):

    private var running = Set.empty[K]

    private var waiting = Map.empty[K, V]

    /**
      * Makes a change, at once or once the key's last change has answered.
      *
      * @param key
      *   What the change is to.
      *
      * @param change
      *   The change.
      */
    def apply(key: K, change: V): Unit =
      if running(key) then waiting = waiting.updated(key, change)
      else start(key, change)

    private def start(key: K, change: V): Unit =
      running += key
      fire(send(key, change))(_ => next(key))

    private def next(key: K): Unit =
      running -= key
      waiting
        .get(key)
        .foreach: change =>
          waiting -= key
          start(key, change)
