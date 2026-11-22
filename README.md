<div align="center">

  <h1>🧩 Laminar Components</h1>
  <p>Components for <a href="https://laminar.dev/">Laminar</a>, styled in Scala: tooltips, dialogs, menus, tabs, switches, a colour picker and more.</p>

  <span>
    <a href="https://github.com/SgtSwagrid/laminar-components/actions/workflows/build-integrity.yml"><img src="https://github.com/SgtSwagrid/laminar-components/actions/workflows/build-integrity.yml/badge.svg" alt="Build status" /></a>
    <a href="https://search.maven.org/artifact/com.alecdorrington/laminar-components_sjs1_3"><img src="https://img.shields.io/maven-central/v/com.alecdorrington/laminar-components_sjs1_3.svg" alt="Maven Central" /></a>
  </span>

</div>

> [!WARNING]
> Laminar Components is still a prototype. Anything could be broken or change between minor versions.

A handful of the components every Laminar application ends up writing for itself, each styled by stylesheets
written in Scala rather than in CSS files, so that a component's look lives beside the view that draws it.
The stylesheets are built from Laminar's own typed style props, and hold what inline styles cannot:
states such as `:hover`, parts such as `::after`, media queries, keyframes and font faces.
It is built on Laminar alone.

## ⬇️ Installation

The following applies for [Scala.js](https://www.scala-js.org/) projects in [Scala 3](https://www.scala-lang.org/), built with [sbt 2](https://www.scala-sbt.org/).
Add the library to your `build.sbt` file, with the desired version number (e.g. [latest](https://github.com/SgtSwagrid/laminar-components/releases/latest)):

```scala
libraryDependencies += "com.alecdorrington" %% "laminar-components" % "{version}"
```

## 🎨 Styling in Scala

A [`Stylesheet`](src/main/scala/Stylesheet.scala) is a modifier: give it to the element of the view it styles,
and the first element given it adds it to the page's head. Its rules are written with [`Css`](src/main/scala/Css.scala),
in Laminar's style props, and those Laminar lacks (logical properties, grid layout and more) are in [`CssProps`](src/main/scala/CssProps.scala).

```scala
import com.alecdorrington.laminarcomponents.Css.{media, rule}
import com.alecdorrington.laminarcomponents.CssProps.{gridTemplateColumns, paddingInline}
import com.alecdorrington.laminarcomponents.Stylesheet
import com.raquo.laminar.api.L.*

object Shelf:

  def apply(books: List[Book]): HtmlElement =
    div(styles, cls := "shelf", books.map(book => div(cls := "book", book.title)))

  private val styles = Stylesheet(
    rule(".shelf")(
      display.grid,
      gridTemplateColumns := "repeat(auto-fill, minmax(160px, 1fr))",
      gap.px(12),
      media("(max-width: 600px)")(gap.px(8)),
    ),
    rule(".book")(
      paddingInline := "12px",
      borderRadius.px(8),
      rule("&:hover")(backgroundColor := "#f4f4f8"),
    ),
  )
```

A rule nested in another applies within it, `&` standing for the outer rule's selector (else a descendant of it), as
in CSS nesting, but each is written out whole, for any browser. Import `media` by name, as Laminar has one of its own.

The components' own stylesheets are in a cascade layer (`Stylesheet.components`), which any rule in no layer outweighs,
so a page restyles a component by its classes, whatever their selectors. Each component says which classes it draws,
all beginning `lc-`. Their colours and shapes come from custom properties, each with a default, listed in
[`Theme`](src/main/scala/Theme.scala):

```scala
Stylesheet(rule(":root")(Theme.accent := "#0b7a75", Theme.radius := "4px"))
```

## 🧱 Components

| | Is | Worth knowing |
| --- | --- | --- |
| [`Tooltip`](src/main/scala/Tooltip.scala) | A hint shown on hover, keyboard focus or a tap, for any element | Mount `Tooltip.host` once at the root, so that no ancestor clips it. `Tooltip.whole` shows a text only while its element cuts it short. |
| [`ConfirmDialog`](src/main/scala/ConfirmDialog.scala) | A question asked before something cannot be undone, or with several answers | Mount `ConfirmDialog.host` once at the root. Cancelling is the default; focus stays within it. |
| [`Menu`](src/main/scala/Menu.scala) | A button opening a list of choices | Closed by a choice, a click elsewhere, or Escape. |
| [`Dropdown`](src/main/scala/Dropdown.scala) | A `select` of typed options | Each option is sent under a code of yours. |
| [`Tabs`](src/main/scala/Tabs.scala) | Tabs over one pane at a time | Each pane is built when first opened and kept, so that a half-filled form survives. |
| [`Switch`](src/main/scala/Switch.scala) | An on/off setting, as a slider or a tick box | |
| [`NumberInput`](src/main/scala/NumberInput.scala) | A number field with arrows of its own, which repeat while held | The same in every browser. `NumberInput.count` binds it to a whole count. |
| [`ColourPicker`](src/main/scala/ColourPicker.scala) | A swatch opening a popover of swatches, shades, hues and a code | A colour is chosen only once a gesture settles, never mid-drag. [`Hsv`](src/main/scala/Hsv.scala) keeps a grey's hue. |
| [`Glide`](src/main/scala/Glide.scala) | Elements sliding into new places after a change, rather than jumping there | Matched by keys of yours, so that an element drawn afresh slides as the one it replaced. Nothing moves for a reader who prefers reduced motion. |
| [`Reordering`](src/main/scala/Reordering.scala) | A row dragged to a new place in a list, the rows it passes moving aside | Pure: it says where the row lands and where each row is drawn. |
| [`Handover`](src/main/scala/Handover.scala) | Typing on text shown for reading handed to the field editing it | The keystroke itself lands in the field. |
| [`TextCaret`](src/main/scala/TextCaret.scala) | Offsets in an element's text, from points, the selection and back | Skips the text of elements you name, such as notes drawn between the words. |
| [`Baseline`](src/main/scala/Baseline.scala) | Where an element's text stands on its baseline | Measured, as fonts differ by platform and script. |
| [`PageBadge`](src/main/scala/PageBadge.scala) | A count shown before the page's title, on the tab's icon and on an installed app's | |
| [`Chime`](src/main/scala/Chime.scala) | A short chime as something arrives | Silent until the reader first clicks or types; one tab chimes for each arrival. |

## 🔁 Reactive helpers

| | Is |
| --- | --- |
| [`Toggle`](src/main/scala/Toggle.scala) | Something open or closed, folded from every cause of either |
| [`Visible`](src/main/scala/Visible.scala) | An element shown only while a signal says so |
| [`FieldText`](src/main/scala/FieldText.scala) | A field's text, whichever came last, the typing or a change from outside |
| [`Requests`](src/main/scala/Requests.scala) | A request fired from a handler and followed only until it answers, and changes to one thing sent one at a time |
| [`Router`](src/main/scala/Router.scala) | The page's route, kept in step with the address bar and history |
| [`Streaming`](src/main/scala/Streaming.scala) | A response read line by line as it arrives |
| [`Writes`](src/main/scala/Writes.scala) | The writes a client makes, kept so that a read never undoes an edit not yet sent |
