package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/** Requests whose response is read line by line as it arrives. */
object Streaming:

  /** An event of a streamed response. */
  enum Event:

    /**
      * One non-blank line of the response, trimmed.
      *
      * @param text
      *   The line.
      */
    case Line(text: String)

    /**
      * The request was refused or the connection failed. Lines already read
      * stand.
      *
      * @param reason
      *   The refusal's body, or the failure's message.
      *
      * @param refused
      *   Whether the server refused the request, rather than the connection
      *   failing.
      */
    case Failed(reason: String, refused: Boolean)

    /** The response has ended, however it ended. Always the last event. */
    case Done

  /**
    * Posts a JSON body when first followed, then emits each line of the
    * response as it arrives, then [[Event.Done]]. Losing its last subscriber
    * aborts the request. Cookies go with it to the page's own origin alone.
    *
    * @param url
    *   Where to post.
    *
    * @param json
    *   The body, as JSON.
    *
    * @return
    *   The response's events.
    */
  def post(url: String, json: String): EventStream[Event] =
    var inFlight = Option.empty[dom.AbortController]
    EventStream.fromCustomSource[Event](
      start = (fire, _, _, _) =>
        val controller = new dom.AbortController
        inFlight = Some(controller)
        val init = new dom.RequestInit:
          method = dom.HttpMethod.POST
          headers = js.Dictionary("Content-Type" -> "application/json")
          body = json
          credentials = dom.RequestCredentials.`same-origin`
          signal = controller.signal
        dom
          .fetch(url, init)
          .toFuture
          .flatMap(response =>
            if response.status >= 400 then
              response
                .text()
                .toFuture
                .map(reason => fire(Event.Failed(reason, refused = true)))
            else read(response, fire),
          )
          .recover:
            case _ if controller.signal.aborted => ()
            case error => fire(Event.Failed(error.getMessage, refused = false))
          .foreach(_ => if !controller.signal.aborted then fire(Event.Done))
      ,
      stop = _ =>
        inFlight.foreach(_.abort())
        inFlight = None,
    )

  private def read
    (
      response: dom.Response,
      fire: Event => Unit,
    )
    : Future[Unit] =
    val reader  = response.asInstanceOf[js.Dynamic].body.getReader()
    val decoder = js.Dynamic.newInstance(js.Dynamic.global.TextDecoder)()
    def loop(pending: String): Future[Unit] = reader
      .read()
      .asInstanceOf[js.Promise[js.Dynamic]]
      .toFuture
      .flatMap: chunk =>
        if chunk.done.asInstanceOf[Boolean] then
          emit(fire, pending)
          Future.unit
        else
          val decoded = decoder
            .decode(
              chunk.value,
              js.Dynamic.literal(stream = true),
            )
            .asInstanceOf[String]
          val lines = (pending + decoded).split("\n", -1)
          lines.init.foreach(emit(fire, _))
          loop(lines.last)
    loop("")

  private def emit(fire: Event => Unit, line: String): Unit =
    val text = line.trim
    if text.nonEmpty then fire(Event.Line(text))
