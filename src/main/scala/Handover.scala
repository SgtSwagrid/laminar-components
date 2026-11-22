package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.scalajs.js

/**
  * Hands text shown for reading back to the field editing it the moment the
  * reader types. The field takes the focus while the keystroke is still being
  * handled, so that the keystroke itself lands in it.
  */
object Handover:

  /** Where the field shown next puts its caret, asked for during one event. */
  final class Pending:

    private var span: Option[(Int, Int)] = None

    /**
      * Asks for a span to be selected, until this event is handled.
      *
      * @param start
      *   Where the span starts, in the field's text.
      *
      * @param end
      *   Where the span ends, exclusive.
      */
    def ask(start: Int, end: Int): Unit =
      span = Some((start, end))
      js.timers
        .setTimeout(0):
          span = None

    /**
      * Focuses a field with the span asked for selected, if any was.
      *
      * @param editor
      *   The field.
      */
    def take(editor: dom.HTMLTextAreaElement): Unit =
      span.foreach: (start, end) =>
        span = None
        val length = editor.value.length
        editor.focus()
        editor.setSelectionRange(start.min(length), end.min(length))

  /**
    * Calls `hand` on each keystroke that would type into the text while
    * `armed`, made in this element or with nothing focused, which sends it to
    * the document's body.
    *
    * @param armed
    *   Whether typing hands the text over.
    *
    * @param hand
    *   What hands it over.
    *
    * @return
    *   The modifier of the element showing the text.
    */
  def onTyping
    (armed: Signal[Boolean], hand: () => Unit)
    : Modifier[HtmlElement] =
    val on = Var(false)
    List(
      armed --> on.writer,
      onMountUnmountCallbackWithState(
        mount = context =>
          val pane     = context.thisNode.ref
          val listener = (event: dom.KeyboardEvent) =>
            if on.now() && !event.defaultPrevented && typing(event, pane) then
              hand()
          val handler: js.Function1[dom.KeyboardEvent, Unit] = listener
          dom.document.addEventListener("keydown", handler)
          handler
        ,
        unmount = (_, handler) =>
          handler.foreach(dom.document.removeEventListener("keydown", _)),
      ),
    )

  /**
    * Whether a keystroke would type into the text: a character, a line break, a
    * deletion or a paste, made where nothing else takes it.
    */
  private def typing
    (
      event: dom.KeyboardEvent,
      pane: dom.Element,
    )
    : Boolean =
    val key       = if js.isUndefined(event.key) then "" else event.key
    val shortcut  = event.metaKey || (event.ctrlKey && !event.altKey)
    val paste     = shortcut && key.equalsIgnoreCase("v")
    val character = !shortcut && key.codePointCount(0, key.length) == 1
    target(event.target, pane) match
      case Target.Plain   => paste || character || editing(key)
      case Target.Control => paste || (character && key != " ")
      case Target.Other   => false

  /** The keys that edit text without typing a character. */
  private val editing = Set("Enter", "Backspace", "Delete")

  /** Where a keystroke is made, as far as typing goes. */
  private enum Target:

    /** Text, or nowhere at all. */
    case Plain

    /** A button, link or checkbox, which Space and Enter work. */
    case Control

    /** A field, which takes typing itself, or anywhere outside the pane. */
    case Other

  private def target(at: dom.EventTarget, pane: dom.Element): Target = at match
    case element: dom.HTMLElement =>
      if element.isContentEditable || element.matches(fields) then Target.Other
      else if element == dom.document.body then Target.Plain
      else if !pane.contains(element) then Target.Other
      else if element.matches(controls) then Target.Control
      else Target.Plain
    case _ => Target.Other

  private val fields =
    "textarea, select, input:not([type=checkbox]):not([type=radio]):not([type=button])"

  private val controls =
    "button, a[href], summary, input, [role=button], [tabindex]"
