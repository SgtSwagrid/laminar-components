package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom.window

/**
  * The current route of a page, kept in step with the address bar and history.
  * Change it only through [[navigate]] and [[redirect]].
  *
  * @tparam R
  *   A route.
  *
  * @param parse
  *   The route a URL path names.
  *
  * @param path
  *   The path of a route's URL.
  */
final class Router[R](parse: String => R, path: R => String):

  private def here: R = parse(window.location.pathname)

  private val moves = new EventBus[R]

  /** The route on display, followed for as long as the page is open. */
  val route: StrictSignal[R] = moves
    .events
    .mergeWith(windowEvents(_.onPopState).mapTo(here))
    .startWith(here)
    .observe(using unsafeWindowOwner)

  // Rewrite an unusually spelled path (`/a//3/`) to its route's own, so each
  // place has one URL.
  if path(route.now()) != window.location.pathname then
    window.history.replaceState(null, "", path(route.now()))

  /**
    * Moves to a route, as a new entry in the browser's history.
    *
    * @param to
    *   The route.
    */
  def navigate(to: R): Unit =
    move(to)(window.history.pushState(null, "", path(to)))

  /**
    * Moves to a route in place of the current entry in the browser's history.
    *
    * @param to
    *   The route.
    */
  def redirect(to: R): Unit =
    move(to)(window.history.replaceState(null, "", path(to)))

  /**
    * Moves to a route unless the address bar already shows it. Compares the
    * address bar, not [[route]], which reads stale within the transaction that
    * moved it and would swallow a second move straight after the first.
    */
  private def move(to: R)(record: => Unit): Unit = if to != here then
    record
    moves.emit(to)
