package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.styleProp
import com.raquo.laminar.keys.StyleProp
import com.raquo.laminar.modifiers.SimpleKeySetter.StyleSetter

/**
  * The custom properties the components are coloured and shaped by, each with a
  * default. A page sets them to its own, usually on `:root`:
  *
  * {{{
  * Stylesheet(rule(":root")(Theme.accent := "#0b7a75", Theme.radius := "4px"))
  * }}}
  */
object Theme:

  /**
    * A custom property of the theme.
    *
    * @param name
    *   Its name, as `--lc-accent`.
    *
    * @param fallback
    *   Its value wherever the page leaves it unset.
    */
  final case class Token(name: String, fallback: String):

    /** The property itself. */
    val prop: StyleProp[String] = styleProp(name)

    /** Its value, or the fallback, as a declaration's value reads it. */
    val value: String = s"var($name, $fallback)"

    /**
      * Sets the property.
      *
      * @param to
      *   The value, as `#0b7a75` or another property's `var(--brand)`.
      *
      * @return
      *   The declaration.
      */
    def := (to: String): StyleSetter[String, String] = prop := to

  /** The colour of popovers, menus and dialogs. */
  val surface: Token = Token("--lc-surface", "#ffffff")

  /** The colour of a plain button under the pointer. */
  val surfaceHover: Token = Token("--lc-surface-hover", "#f9fafb")

  /** The colour of a field to type in, until focused. */
  val field: Token = Token("--lc-field", "#fafbfc")

  /** The colour of text. */
  val text: Token = Token("--lc-text", "#1c2333")

  /** The colour of text of less note. */
  val textMuted: Token = Token("--lc-text-muted", "#667085")

  /** The colour of text of least note, and of borders under the pointer. */
  val textFaint: Token = Token("--lc-text-faint", "#98a2b3")

  /** The colour of borders. */
  val border: Token = Token("--lc-border", "#e4e7ec")

  /** The colour of borders that stand out, as a field's. */
  val borderStrong: Token = Token("--lc-border-strong", "#d0d5dd")

  /** The colour of what is chosen or to be pressed. */
  val accent: Token = Token("--lc-accent", "#4f46e5")

  /** The accent under the pointer. */
  val accentHover: Token = Token("--lc-accent-hover", "#4338ca")

  /** The accent, faint, behind what it marks. */
  val accentSoft: Token = Token("--lc-accent-soft", "#eef0fd")

  /** The colour of what deletes or cannot be undone. */
  val danger: Token = Token("--lc-danger", "#d92d20")

  /** The danger colour under the pointer. */
  val dangerHover: Token = Token("--lc-danger-hover", "#b42318")

  /** The rounding of the corners of dialogs and popovers. */
  val radius: Token = Token("--lc-radius", "12px")

  /** The rounding of the corners of buttons, fields and menus. */
  val radiusSmall: Token = Token("--lc-radius-small", "8px")

  /** The shadow beneath buttons. */
  val shadowSmall: Token = Token(
    "--lc-shadow-small",
    "0 1px 2px rgba(16, 24, 40, 0.05)",
  )

  /** The shadow beneath menus and dialogs. */
  val shadow: Token = Token(
    "--lc-shadow",
    "0 4px 12px rgba(16, 24, 40, 0.08)",
  )

  /** The ring around what has the keyboard's focus. */
  val focusRing: Token = Token(
    "--lc-focus-ring",
    "0 0 0 3px rgba(79, 70, 229, 0.18)",
  )

  /** The colour of a tooltip. */
  val tooltipBackground: Token = Token("--lc-tooltip-background", "#1c2333")

  /** The colour of a tooltip's text. */
  val tooltipText: Token = Token("--lc-tooltip-text", "#eef1f7")

  /** Every property of the theme. */
  val tokens: List[Token] = List(
    surface,
    surfaceHover,
    field,
    text,
    textMuted,
    textFaint,
    border,
    borderStrong,
    accent,
    accentHover,
    accentSoft,
    danger,
    dangerHover,
    radius,
    radiusSmall,
    shadowSmall,
    shadow,
    focusRing,
    tooltipBackground,
    tooltipText,
  )
