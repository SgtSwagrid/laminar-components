package com.alecdorrington.laminarcomponents

import org.scalajs.dom
import scala.scalajs.js
import scala.util.control.NonFatal

/**
  * A short chime for something arriving, two soft notes made on the spot. A
  * page may make sound only once someone has pressed a key or clicked on it, so
  * it is silent until then ([[listen]]); it chimes at most once in a while, and
  * for any one arrival, only in one of the reader's tabs.
  *
  * @param key
  *   The key in local storage under which the reader's tabs agree which of them
  *   chimes.
  */
final class Chime(key: String):

  private var context: Option[dom.AudioContext] = None

  private var lastAt = 0.0

  private val quietMillis = 1500

  /**
    * Readies the sound at someone's first click or key press, which a page must
    * wait for. Call it as the page starts.
    */
  def listen(): Unit =
    val wake = (_: dom.Event) =>
      if context.isEmpty then context = Some(new dom.AudioContext())
      context.foreach(sound =>
        if sound.state == "suspended" then sound.resume(),
      )
    dom.window.addEventListener("pointerdown", wake)
    dom.window.addEventListener("keydown", wake)

  /**
    * Chimes for an arrival, unless it chimed a moment ago or another tab has
    * already chimed for it.
    *
    * @param arrival
    *   What arrived, greater for each later arrival, as an id is, so that the
    *   tabs hearing of it agree which chimes.
    */
  def play(arrival: Long): Unit =
    val now = js.Date.now()
    if now - lastAt >= quietMillis && claimed(arrival) then
      lastAt = now
      context.filter(_.state == "running").foreach(notes)

  /**
    * Whether this tab is the first to chime for an arrival; any tab chimes
    * where storage is unavailable.
    */
  private def claimed(arrival: Long): Boolean =
    try
      val last = Option(dom.window.localStorage.getItem(key))
        .flatMap(_.toLongOption)
        .getOrElse(0L)
      if arrival > last then
        dom.window.localStorage.setItem(key, arrival.toString)
      arrival > last
    catch case NonFatal(_) => true

  private def notes(sound: dom.AudioContext): Unit =
    val start = sound.currentTime
    List(880.0 -> 0.0, 1318.5 -> 0.12).foreach: (pitch, after) =>
      val at   = start + after
      val tone = sound.createOscillator()
      val fade = sound.createGain()
      tone.`type` = "sine"
      tone.frequency.value = pitch
      fade.gain.setValueAtTime(0.0001, at)
      fade.gain.exponentialRampToValueAtTime(0.15, at + 0.015)
      fade.gain.exponentialRampToValueAtTime(0.0001, at + 0.28)
      tone.connect(fade)
      fade.connect(sound.destination)
      tone.start(at)
      tone.stop(at + 0.3)
