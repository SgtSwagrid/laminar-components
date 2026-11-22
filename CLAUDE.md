# CLAUDE.md

This file provides guidance to [Claude Code](https://claude.com/product/claude-code) when working with code in this repository.
It is not intended for human eyes.

### Maintenance

You (robot or human) have standing permission to update this file without asking.
Add important patterns, gotchas, or context that would help future sessions.
Keep it concise and actionable.

## Project overview

This is Laminar Components, a Scala.js library of components for [Laminar](https://laminar.dev/), styled in Scala. It is
still a prototype.

It is a single Scala.js module in `com.alecdorrington.laminarcomponents`, depending on Laminar alone.

- Styling is Scala, never CSS files. `Css` builds rules (`rule`, `media`, `supports`, `keyframes`, `fontFace`) from
  Laminar's own typed style setters, and `CssProps` holds the props Laminar lacks. `Css.render` writes nested rules out
  whole (`&` for the outer selector, else a descendant; selector lists crossed), so no browser needs CSS nesting. A
  `Stylesheet` is a `Modifier`: the first element given it adds it to the page's head, once, for good. `added` adds
  it at once instead, as a value of a view's object, when the object is first used, and does nothing without a
  `document` (Node), so that tests touching the object still run.
- Each component keeps its stylesheet beside its view, as a `private val styles`, in the `Stylesheet.components`
  cascade layer, so that any unlayered rule of the host's outweighs it. Its classes all begin `lc-`, and its scaladoc
  lists them, as hosts restyle by them. Colours and shapes come from `Theme`'s tokens (`--lc-*`, each with a fallback),
  read in rules as `Theme.accent.value`.
- A component object's private vals must not take a style prop's name (`gap`, `margin`, `top`...): inside the object
  they shadow Laminar's, and its stylesheet then fails to compile.
- Values are encoded by `Css` itself (`String.valueOf`, a derived prop's `encode`), never by Laminar's `cssValue`,
  which initialises `DomApi` and so needs a page: the tests run on Node, with no `document`.
- Hosts supply all wording (`ConfirmDialog.host`'s cancel, `ColourPicker.Labels`), so that nothing here is in one
  language.
- Avoid names Laminar's `L` already has: they clash where both are imported whole. `CssProps.translation` and
  `fontSource` are named apart from Laminar's `translate` and `src` attributes; `Css.media` is imported by name.

See [README.md](README.md) for usage.

### Where this code lives

This repository is a mirror. The library is developed inside a larger private project, beneath `laminar-components/`,
and every file here is copied from there by [GitHub Graph](https://github.com/SgtSwagrid/github-graph) whenever that
project's `main` changes, overwriting whatever is here. So make changes there, never here. The shared configuration
(workflows, Scalafmt, IDE settings, `project/plugins-common.sbt` and `project/plugins-library.sbt`) comes from further
upstream still, in [Scala Library Config](https://github.com/SgtSwagrid/scala-library-config), which syncs into the
private project's `laminar-components/` first. `build.sbt`, `release.sbt`, `project/Dependencies.scala`,
`project/plugins-scalajs.sbt`, `README.md` and this file belong to the library.

### Build

- The root project `laminar-components` is the library itself, published as `laminar-components`, for Scala.js alone.
  Its id is the library's name because the private project includes this build by reference
  (`ProjectRef(file("laminar-components"), "laminar-components")`), alongside projects of its own.
- The library must never depend on anything in the project that includes it.
- Versions come from git tags (`sbt-ci-release`); publishing a GitHub release publishes to Maven Central.

## Instructions

### Compilation and Diagnostics

- When the user asks for help with a compilation or type error, start by running `sbt compile` to see the error for yourself.
  If there are many errors, making it unclear which one the user is referring to, ask them to clarify, and then focus only on that issue.
- IntelliJ MCP integration is active. When a request seems to implicitly refer to something the user is looking at, always check
  `mcp__ide__getDiagnostics` first to see which file(s) are open and get associated diagnostics (errors, warnings, and info hints with line numbers).

#### Testing

- After making code changes, always run `sbt compile` to verify that issues are fixed and no new ones are introduced.
- Repeatedly retry upon failure until the build succeeds. If you are unsure how to fix an issue, ask for help or refer to existing code for examples.
- Before trying to fix an error, make sure you first understand it fully.
- You should never report that a feature is complete without testing it first.

### Code Style

- You must read the [Code Style Guidelines](docs/STYLE_GUIDE.md).
- Document every public type and member: a summary, then `@param` for each explicit parameter,
  `@tparam` for each type parameter, and `@return` for any result but `Unit`, each one short
  sentence. Summaries read: types "A ...", values "The ...", Booleans "Whether ...", methods a
  third-person verb ("Sends ..."), never "Returns ...". Private members get a comment only for a
  non-obvious contract or gotcha, usually in one sentence.

### Pull Requests

When asked to publish the code changes, your task is to open one or more pull requests (PRs) to merge the changes into `main` on GitHub:

- Use `git` to check what has changed as compared to the `main` branch on `origin`.
- If the changes are thematically linked, they can be published as a single PR.
- Otherwise, you'll need to divide the changes into multiple PRs using your own judgement.
- Each PR should have a singular focus, shouldn't break anything, and should be able to be merged independently.
- Ensure that all code is staged, committed and pushed. Ensure no new files are left uncommitted, and no debug code is left in the codebase.
- When creating a PR, ensure that the title and description are clear, informative, and comprehensive.
- All feature/bugfix/etc branch names should be formatted as "feature_<short description>" or "fix_<short description>" or similar.
- All PR titles should be formatted as "[<scope>] <Short summary>", e.g. "[renderer] Fixed colour inversion bug."
- Use the `gh` CLI to do the above.
