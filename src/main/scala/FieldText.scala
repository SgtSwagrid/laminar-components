package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.*

/**
  * A field's text: whichever came last, the typing or a change of `put`, so
  * that a field may be cleared or filled from outside without a [[Var]].
  *
  * @param put
  *   The text put in the field from outside, as when it is cleared once sent.
  */
final class FieldText(put: Signal[String] = Val("")):

  private val typing = new EventBus[String]

  /** The text in the field. */
  val text: Signal[String] = put.updates(_.mergeWith(typing.events))

  /** The binder making a field show and follow [[text]]. */
  val control: Binder[HtmlElement] = controlled(
    value <-- text,
    onInput.mapToValue --> typing,
  )

object FieldText:

  /**
    * Makes a field's text, emptied by every clearing.
    *
    * @param clearings
    *   The events emptying the field.
    *
    * @return
    *   The field's text.
    */
  def clearedBy(clearings: EventStream[Any]): FieldText =
    FieldText(clearings.mapTo("").startWith(""))
