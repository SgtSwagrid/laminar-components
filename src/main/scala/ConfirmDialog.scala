package com.alecdorrington.laminarcomponents

import com.alecdorrington.laminarcomponents.Css.{media, rule}
import com.alecdorrington.laminarcomponents.CssProps.inset
import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.Codec
import org.scalajs.dom

/**
  * The page's one confirmation dialog, drawn by a single [[host]] at the root
  * so that no ancestor of the asking control can clip it. Cancelling is the
  * default.
  *
  * Drawn as `.lc-confirm-dialog` over a `.lc-confirm-backdrop`, its question a
  * `.lc-confirm-subject` over a `.lc-confirm-consequence`, and its buttons
  * `.lc-confirm-button`s in `.lc-confirm-actions`: `.lc-confirm-cancel`, and
  * each answer `.lc-confirm-answer`, or `.lc-confirm-danger` if it destroys.
  */
object ConfirmDialog:

  /**
    * A question asked before something is done that cannot be undone, such as
    * deleting it for good.
    *
    * @param question
    *   The question, as "Delete the report?".
    *
    * @param consequence
    *   What answering yes does, as "It cannot be brought back.".
    *
    * @param verb
    *   The button answering yes, as "Delete".
    */
  final case class Confirmation
    (
      question: String,
      consequence: String,
      verb: String,
    )

  /**
    * An answer a question offers besides cancelling.
    *
    * @param caption
    *   The answer's button.
    *
    * @param act
    *   What answering so does.
    *
    * @param danger
    *   Whether the answer destroys something, which its button shows.
    */
  final case class Answer
    (
      caption: String,
      act: () => Unit,
      danger: Boolean = false,
    )

  private final case class Request
    (
      subject: String,
      consequence: String,
      choices: List[Answer],
      cancelled: () => Unit,
      opener: Option[dom.HTMLElement],
    )

  private val pending: Var[Option[Request]] = Var(None)

  /**
    * Asks a question, running the action only if it is confirmed.
    *
    * @param confirmation
    *   The question.
    *
    * @param confirmed
    *   The action.
    */
  def ask
    (
      confirmation: Confirmation,
      confirmed: () => Unit,
    )
    : Unit = choose(
    confirmation.question,
    confirmation.consequence,
    List(Answer(
      confirmation.verb,
      confirmed,
      danger = true,
    )),
  )

  /**
    * Asks a question, running the answer taken, or `cancelled`.
    *
    * @param subject
    *   The question.
    *
    * @param consequence
    *   What follows from answering it.
    *
    * @param choices
    *   The answers offered besides cancelling.
    *
    * @param cancelled
    *   What cancelling does.
    */
  def choose
    (
      subject: String,
      consequence: String,
      choices: List[Answer],
      cancelled: () => Unit = () => (),
    )
    : Unit = pending.set(Some(Request(
    subject,
    consequence,
    choices,
    cancelled,
    focused,
  )))

  /**
    * Renders the dialog's host, mounted once at the root.
    *
    * @param active
    *   Whether questions may be shown. An open question is withdrawn when it
    *   turns `false`, so that none outlives, say, the session it was asked in.
    *
    * @param cancel
    *   The button cancelling, as "Cancel".
    *
    * @return
    *   The host.
    */
  def host(active: Signal[Boolean], cancel: String): HtmlElement = div(
    styles,
    cls := "lc-confirm-host",
    child.maybe <--
      // Distinct: a repeated `true` would rebuild an open dialog, refocusing
      // Cancel.
      pending
        .signal
        .combineWith(active.distinct)
        .mapN((asked, open) => asked.filter(_ => open).map(dialog(_, cancel))),
    documentEvents(_.onKeyDown) --> keyDown,
    active.updates.filterNot(identity) --> (_ => pending.set(None)),
  )

  private def answer(taken: Option[Answer]): Unit = pending
    .now()
    .foreach: request =>
      pending.set(None)
      request
        .opener
        .filter(opener => dom.document.contains(opener))
        .foreach(_.focus())
      taken.fold(request.cancelled())(_.act())

  /**
    * Handles keys on the whole document, as a click on the dialog's text leaves
    * the focus outside it.
    */
  private def keyDown(event: dom.KeyboardEvent): Unit =
    if pending.now().isDefined then
      event.key match
        case "Escape" => answer(None)
        case "Tab"    => trapFocus(event)
        case _        => ()

  private def trapFocus(event: dom.KeyboardEvent): Unit =
    Option(dom.document.querySelector(".lc-confirm-dialog")).foreach: shown =>
      val buttons = shown.querySelectorAll("button")
      val first   = buttons(0).asInstanceOf[dom.HTMLElement]
      val last    = buttons(buttons.length - 1).asInstanceOf[dom.HTMLElement]
      val current = dom.document.activeElement
      val entry   = if event.shiftKey then last else first
      val target  =
        if !shown.contains(current) then Some(entry)
        else if event.shiftKey && current == first then Some(last)
        else if !event.shiftKey && current == last then Some(first)
        else None
      target.foreach: element =>
        event.preventDefault()
        element.focus()

  private def dialog(request: Request, cancel: String): HtmlElement =
    // Only a press and release both on the backdrop cancel, so that a drag
    // begun inside the dialog does not.
    val presses = new EventBus[Boolean]
    div(
      cls := "lc-confirm-backdrop",
      onMouseDown.map(beside) --> presses,
      onClick
        .filter(beside)
        .compose(_.sample(presses.events.startWith(false)).filter(identity)) -->
        (_ => answer(None)),
      div(
        cls                                      := "lc-confirm-dialog",
        role                                     := "alertdialog",
        tabIndex                                 := -1,
        htmlAttr("aria-modal", Codec.stringAsIs) := "true",
        aria.labelledBy                          := subjectId,
        aria.describedBy                         := consequenceId,
        p(
          idAttr := subjectId,
          cls    := "lc-confirm-subject",
          request.subject,
        ),
        p(
          idAttr := consequenceId,
          cls    := "lc-confirm-consequence",
          request.consequence,
        ),
        div(
          cls := "lc-confirm-actions",
          button(
            cancel,
            cls := "lc-confirm-button lc-confirm-cancel",
            onMountFocus,
            onClick --> (_ => answer(None)),
          ),
          request
            .choices
            .map(choice =>
              button(
                choice.caption,
                cls :=
                  (if choice.danger then "lc-confirm-button lc-confirm-danger"
                   else "lc-confirm-button lc-confirm-answer"),
                onClick --> (_ => answer(Some(choice))),
              ),
            ),
        ),
      ),
    )

  private def beside(event: dom.MouseEvent): Boolean = event.target ==
    event.currentTarget

  private def focused: Option[dom.HTMLElement] =
    dom.document.activeElement match
      case element: dom.HTMLElement => Some(element)
      case _                        => None

  private val subjectId = "lc-confirm-subject"

  private val consequenceId = "lc-confirm-consequence"

  private val styles = Stylesheet.layered(Stylesheet.components)(
    // Mounted among the page's columns, but adds no gap after them.
    rule(".lc-confirm-host")(display.contents),
    rule(".lc-confirm-backdrop")(
      position.fixed,
      inset  := "0",
      zIndex := 100,
      display.flex,
      alignItems.center,
      justifyContent.center,
      background := "rgba(16, 24, 40, 0.35)",
    ),
    rule(".lc-confirm-dialog")(
      width        := "min(480px, calc(100vw - 48px))",
      padding      := "20px 22px 18px",
      background   := Theme.surface.value,
      border       := s"1px solid ${ Theme.border.value }",
      borderRadius := Theme.radius.value,
      boxShadow    := Theme.shadow.value,
      media("(max-width: 960px)")(width := "calc(100vw - 32px)"),
    ),
    rule(".lc-confirm-subject")(
      margin := "0 0 6px",
      fontSize.px(15),
      fontWeight := "600",
      color      := Theme.text.value,
    ),
    rule(".lc-confirm-consequence")(
      margin := "0 0 18px",
      fontSize.px(13),
      lineHeight := "1.5",
      color      := Theme.textMuted.value,
    ),
    rule(".lc-confirm-actions")(
      display.flex,
      flexWrap.wrap,
      justifyContent.flexEnd,
      gap.px(8),
    ),
    // A transparent border keeps every kind of button the same height.
    rule(".lc-confirm-button")(
      maxWidth := "100%",
      padding  := "9px 20px",
      fontSize.px(14),
      fontWeight   := "600",
      fontFamily   := "inherit",
      borderRadius := Theme.radiusSmall.value,
      border       := "1px solid transparent",
      cursor.pointer,
      transition :=
        "background-color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease, transform 0.05s ease",
      rule("&:active")(transform := "translateY(1px)"),
    ),
    rule(".lc-confirm-cancel")(
      color      := Theme.text.value,
      background := Theme.surface.value,
      border     := s"1px solid ${ Theme.borderStrong.value }",
      rule("&:hover")(
        background  := Theme.surfaceHover.value,
        borderColor := Theme.textFaint.value,
      ),
    ),
    rule(".lc-confirm-answer")(
      color      := "#ffffff",
      background := Theme.accent.value,
      boxShadow  := Theme.shadowSmall.value,
      rule("&:hover")(background := Theme.accentHover.value),
    ),
    rule(".lc-confirm-danger")(
      color      := "#ffffff",
      background := Theme.danger.value,
      boxShadow  := Theme.shadowSmall.value,
      rule("&:hover")(background := Theme.dangerHover.value),
    ),
  )
