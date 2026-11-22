package com.alecdorrington.laminarcomponents

import com.raquo.laminar.api.L.styleProp
import com.raquo.laminar.keys.StyleProp

/**
  * The style props Laminar lacks, among them the logical properties (which
  * follow the text's direction), grid layout, and the font face descriptors
  * ([[Css.fontFace]]), each set as Laminar's own are: `insetInlineStart.px(0)`
  * will not do, as these take any value, so `insetInlineStart := "0"`.
  *
  * Import them by name or all at once (`CssProps.*`): if Laminar gains one, the
  * two clash where it is used, and this one should go. Two are named apart from
  * attributes Laminar has: `translation` and `fontSource`.
  */
object CssProps:

  /** The `margin-block` property. */
  lazy val marginBlock: StyleProp[String] = styleProp("margin-block")

  /** The `margin-block-start` property. */
  lazy val marginBlockStart: StyleProp[String] = styleProp("margin-block-start")

  /** The `margin-block-end` property. */
  lazy val marginBlockEnd: StyleProp[String] = styleProp("margin-block-end")

  /** The `margin-inline` property. */
  lazy val marginInline: StyleProp[String] = styleProp("margin-inline")

  /** The `margin-inline-start` property. */
  lazy val marginInlineStart: StyleProp[String] =
    styleProp("margin-inline-start")

  /** The `margin-inline-end` property. */
  lazy val marginInlineEnd: StyleProp[String] = styleProp("margin-inline-end")

  /** The `padding-block` property. */
  lazy val paddingBlock: StyleProp[String] = styleProp("padding-block")

  /** The `padding-block-start` property. */
  lazy val paddingBlockStart: StyleProp[String] =
    styleProp("padding-block-start")

  /** The `padding-block-end` property. */
  lazy val paddingBlockEnd: StyleProp[String] = styleProp("padding-block-end")

  /** The `padding-inline` property. */
  lazy val paddingInline: StyleProp[String] = styleProp("padding-inline")

  /** The `padding-inline-start` property. */
  lazy val paddingInlineStart: StyleProp[String] =
    styleProp("padding-inline-start")

  /** The `padding-inline-end` property. */
  lazy val paddingInlineEnd: StyleProp[String] = styleProp("padding-inline-end")

  /** The `inset` property. */
  lazy val inset: StyleProp[String] = styleProp("inset")

  /** The `inset-block` property. */
  lazy val insetBlock: StyleProp[String] = styleProp("inset-block")

  /** The `inset-block-start` property. */
  lazy val insetBlockStart: StyleProp[String] = styleProp("inset-block-start")

  /** The `inset-block-end` property. */
  lazy val insetBlockEnd: StyleProp[String] = styleProp("inset-block-end")

  /** The `inset-inline` property. */
  lazy val insetInline: StyleProp[String] = styleProp("inset-inline")

  /** The `inset-inline-start` property. */
  lazy val insetInlineStart: StyleProp[String] = styleProp("inset-inline-start")

  /** The `inset-inline-end` property. */
  lazy val insetInlineEnd: StyleProp[String] = styleProp("inset-inline-end")

  /** The `inline-size` property. */
  lazy val inlineSize: StyleProp[String] = styleProp("inline-size")

  /** The `block-size` property. */
  lazy val blockSize: StyleProp[String] = styleProp("block-size")

  /** The `min-inline-size` property. */
  lazy val minInlineSize: StyleProp[String] = styleProp("min-inline-size")

  /** The `min-block-size` property. */
  lazy val minBlockSize: StyleProp[String] = styleProp("min-block-size")

  /** The `max-inline-size` property. */
  lazy val maxInlineSize: StyleProp[String] = styleProp("max-inline-size")

  /** The `max-block-size` property. */
  lazy val maxBlockSize: StyleProp[String] = styleProp("max-block-size")

  /** The `border-block` property. */
  lazy val borderBlock: StyleProp[String] = styleProp("border-block")

  /** The `border-block-start` property. */
  lazy val borderBlockStart: StyleProp[String] = styleProp("border-block-start")

  /** The `border-block-end` property. */
  lazy val borderBlockEnd: StyleProp[String] = styleProp("border-block-end")

  /** The `border-inline` property. */
  lazy val borderInline: StyleProp[String] = styleProp("border-inline")

  /** The `border-inline-start` property. */
  lazy val borderInlineStart: StyleProp[String] =
    styleProp("border-inline-start")

  /** The `border-inline-end` property. */
  lazy val borderInlineEnd: StyleProp[String] = styleProp("border-inline-end")

  /** The `border-block-color` property. */
  lazy val borderBlockColor: StyleProp[String] = styleProp("border-block-color")

  /** The `border-inline-color` property. */
  lazy val borderInlineColor: StyleProp[String] =
    styleProp("border-inline-color")

  /** The `border-block-width` property. */
  lazy val borderBlockWidth: StyleProp[String] = styleProp("border-block-width")

  /** The `border-inline-width` property. */
  lazy val borderInlineWidth: StyleProp[String] =
    styleProp("border-inline-width")

  /** The `border-inline-start-color` property. */
  lazy val borderInlineStartColor: StyleProp[String] =
    styleProp("border-inline-start-color")

  /** The `border-inline-end-color` property. */
  lazy val borderInlineEndColor: StyleProp[String] =
    styleProp("border-inline-end-color")

  /** The `border-block-start-color` property. */
  lazy val borderBlockStartColor: StyleProp[String] =
    styleProp("border-block-start-color")

  /** The `border-block-end-color` property. */
  lazy val borderBlockEndColor: StyleProp[String] =
    styleProp("border-block-end-color")

  /** The `border-inline-start-width` property. */
  lazy val borderInlineStartWidth: StyleProp[String] =
    styleProp("border-inline-start-width")

  /** The `border-inline-end-width` property. */
  lazy val borderInlineEndWidth: StyleProp[String] =
    styleProp("border-inline-end-width")

  /** The `border-block-start-width` property. */
  lazy val borderBlockStartWidth: StyleProp[String] =
    styleProp("border-block-start-width")

  /** The `border-block-end-width` property. */
  lazy val borderBlockEndWidth: StyleProp[String] =
    styleProp("border-block-end-width")

  /** The `border-start-start-radius` property. */
  lazy val borderStartStartRadius: StyleProp[String] =
    styleProp("border-start-start-radius")

  /** The `border-start-end-radius` property. */
  lazy val borderStartEndRadius: StyleProp[String] =
    styleProp("border-start-end-radius")

  /** The `border-end-start-radius` property. */
  lazy val borderEndStartRadius: StyleProp[String] =
    styleProp("border-end-start-radius")

  /** The `border-end-end-radius` property. */
  lazy val borderEndEndRadius: StyleProp[String] =
    styleProp("border-end-end-radius")

  /** The `border-image` property. */
  lazy val borderImage: StyleProp[String] = styleProp("border-image")

  /** The `border-image-source` property. */
  lazy val borderImageSource: StyleProp[String] =
    styleProp("border-image-source")

  /** The `border-image-slice` property. */
  lazy val borderImageSlice: StyleProp[String] = styleProp("border-image-slice")

  /** The `border-image-width` property. */
  lazy val borderImageWidth: StyleProp[String] = styleProp("border-image-width")

  /** The `border-image-outset` property. */
  lazy val borderImageOutset: StyleProp[String] =
    styleProp("border-image-outset")

  /** The `border-image-repeat` property. */
  lazy val borderImageRepeat: StyleProp[String] =
    styleProp("border-image-repeat")

  /** The `grid` property. */
  lazy val grid: StyleProp[String] = styleProp("grid")

  /** The `grid-area` property. */
  lazy val gridArea: StyleProp[String] = styleProp("grid-area")

  /** The `grid-template` property. */
  lazy val gridTemplate: StyleProp[String] = styleProp("grid-template")

  /** The `grid-template-columns` property. */
  lazy val gridTemplateColumns: StyleProp[String] =
    styleProp("grid-template-columns")

  /** The `grid-template-rows` property. */
  lazy val gridTemplateRows: StyleProp[String] = styleProp("grid-template-rows")

  /** The `grid-template-areas` property. */
  lazy val gridTemplateAreas: StyleProp[String] =
    styleProp("grid-template-areas")

  /** The `grid-auto-columns` property. */
  lazy val gridAutoColumns: StyleProp[String] = styleProp("grid-auto-columns")

  /** The `grid-auto-rows` property. */
  lazy val gridAutoRows: StyleProp[String] = styleProp("grid-auto-rows")

  /** The `grid-auto-flow` property. */
  lazy val gridAutoFlow: StyleProp[String] = styleProp("grid-auto-flow")

  /** The `grid-column` property. */
  lazy val gridColumn: StyleProp[String] = styleProp("grid-column")

  /** The `grid-column-start` property. */
  lazy val gridColumnStart: StyleProp[String] = styleProp("grid-column-start")

  /** The `grid-column-end` property. */
  lazy val gridColumnEnd: StyleProp[String] = styleProp("grid-column-end")

  /** The `grid-row` property. */
  lazy val gridRow: StyleProp[String] = styleProp("grid-row")

  /** The `grid-row-start` property. */
  lazy val gridRowStart: StyleProp[String] = styleProp("grid-row-start")

  /** The `grid-row-end` property. */
  lazy val gridRowEnd: StyleProp[String] = styleProp("grid-row-end")

  /** The `place-items` property. */
  lazy val placeItems: StyleProp[String] = styleProp("place-items")

  /** The `place-content` property. */
  lazy val placeContent: StyleProp[String] = styleProp("place-content")

  /** The `place-self` property. */
  lazy val placeSelf: StyleProp[String] = styleProp("place-self")

  /** The `justify-items` property. */
  lazy val justifyItems: StyleProp[String] = styleProp("justify-items")

  /** The `justify-self` property. */
  lazy val justifySelf: StyleProp[String] = styleProp("justify-self")

  /**
    * The `translate` property (named apart from Laminar's `translate`
    * attribute).
    */
  lazy val translation: StyleProp[String] = styleProp("translate")

  /** The `rotate` property. */
  lazy val rotate: StyleProp[String] = styleProp("rotate")

  /** The `scale` property. */
  lazy val scale: StyleProp[String] = styleProp("scale")

  /** The `user-select` property. */
  lazy val userSelect: StyleProp[String] = styleProp("user-select")

  /** The `touch-action` property. */
  lazy val touchAction: StyleProp[String] = styleProp("touch-action")

  /** The `hyphens` property. */
  lazy val hyphens: StyleProp[String] = styleProp("hyphens")

  /** The `appearance` property. */
  lazy val appearance: StyleProp[String] = styleProp("appearance")

  /** The `overscroll-behavior` property. */
  lazy val overscrollBehavior: StyleProp[String] =
    styleProp("overscroll-behavior")

  /** The `overscroll-behavior-x` property. */
  lazy val overscrollBehaviorX: StyleProp[String] =
    styleProp("overscroll-behavior-x")

  /** The `overscroll-behavior-y` property. */
  lazy val overscrollBehaviorY: StyleProp[String] =
    styleProp("overscroll-behavior-y")

  /** The `aspect-ratio` property. */
  lazy val aspectRatio: StyleProp[String] = styleProp("aspect-ratio")

  /** The `accent-color` property. */
  lazy val accentColor: StyleProp[String] = styleProp("accent-color")

  /** The `caret-color` property. */
  lazy val caretColor: StyleProp[String] = styleProp("caret-color")

  /** The `field-sizing` property. */
  lazy val fieldSizing: StyleProp[String] = styleProp("field-sizing")

  /** The `font-variant-numeric` property. */
  lazy val fontVariantNumeric: StyleProp[String] =
    styleProp("font-variant-numeric")

  /** The `text-wrap` property. */
  lazy val textWrap: StyleProp[String] = styleProp("text-wrap")

  /** The `container` property. */
  lazy val container: StyleProp[String] = styleProp("container")

  /** The `container-type` property. */
  lazy val containerType: StyleProp[String] = styleProp("container-type")

  /** The `container-name` property. */
  lazy val containerName: StyleProp[String] = styleProp("container-name")

  /** The `object-fit` property. */
  lazy val objectFit: StyleProp[String] = styleProp("object-fit")

  /** The `object-position` property. */
  lazy val objectPosition: StyleProp[String] = styleProp("object-position")

  /** The `tab-size` property. */
  lazy val tabSize: StyleProp[String] = styleProp("tab-size")

  /** The `line-clamp` property. */
  lazy val lineClamp: StyleProp[String] = styleProp("line-clamp")

  /** The `text-decoration-line` property. */
  lazy val textDecorationLine: StyleProp[String] =
    styleProp("text-decoration-line")

  /** The `text-decoration-style` property. */
  lazy val textDecorationStyle: StyleProp[String] =
    styleProp("text-decoration-style")

  /** The `text-decoration-color` property. */
  lazy val textDecorationColor: StyleProp[String] =
    styleProp("text-decoration-color")

  /** The `text-decoration-thickness` property. */
  lazy val textDecorationThickness: StyleProp[String] =
    styleProp("text-decoration-thickness")

  /** The `text-underline-offset` property. */
  lazy val textUnderlineOffset: StyleProp[String] =
    styleProp("text-underline-offset")

  /** The `outline-offset` property. */
  lazy val outlineOffset: StyleProp[String] = styleProp("outline-offset")

  /** The `scrollbar-width` property. */
  lazy val scrollbarWidth: StyleProp[String] = styleProp("scrollbar-width")

  /** The `scrollbar-color` property. */
  lazy val scrollbarColor: StyleProp[String] = styleProp("scrollbar-color")

  /** The `scrollbar-gutter` property. */
  lazy val scrollbarGutter: StyleProp[String] = styleProp("scrollbar-gutter")

  /** The `scroll-behavior` property. */
  lazy val scrollBehavior: StyleProp[String] = styleProp("scroll-behavior")

  /** The `scroll-snap-type` property. */
  lazy val scrollSnapType: StyleProp[String] = styleProp("scroll-snap-type")

  /** The `scroll-snap-align` property. */
  lazy val scrollSnapAlign: StyleProp[String] = styleProp("scroll-snap-align")

  /** The `stroke` property. */
  lazy val stroke: StyleProp[String] = styleProp("stroke")

  /** The `stroke-width` property. */
  lazy val strokeWidth: StyleProp[String] = styleProp("stroke-width")

  /** The `stroke-linecap` property. */
  lazy val strokeLinecap: StyleProp[String] = styleProp("stroke-linecap")

  /** The `stroke-linejoin` property. */
  lazy val strokeLinejoin: StyleProp[String] = styleProp("stroke-linejoin")

  /** The `stroke-dasharray` property. */
  lazy val strokeDasharray: StyleProp[String] = styleProp("stroke-dasharray")

  /** The `stroke-dashoffset` property. */
  lazy val strokeDashoffset: StyleProp[String] = styleProp("stroke-dashoffset")

  /** The `stroke-opacity` property. */
  lazy val strokeOpacity: StyleProp[String] = styleProp("stroke-opacity")

  /** The `shape-rendering` property. */
  lazy val shapeRendering: StyleProp[String] = styleProp("shape-rendering")

  /** The `filter` property. */
  lazy val filter: StyleProp[String] = styleProp("filter")

  /** The `backdrop-filter` property. */
  lazy val backdropFilter: StyleProp[String] = styleProp("backdrop-filter")

  /** The `clip-path` property. */
  lazy val clipPath: StyleProp[String] = styleProp("clip-path")

  /** The `mask-image` property. */
  lazy val maskImage: StyleProp[String] = styleProp("mask-image")

  /** The `will-change` property. */
  lazy val willChange: StyleProp[String] = styleProp("will-change")

  /** The `contain` property. */
  lazy val contain: StyleProp[String] = styleProp("contain")

  /** The `font-display` descriptor. */
  lazy val fontDisplay: StyleProp[String] = styleProp("font-display")

  /**
    * The `src` descriptor of a font face (named apart from Laminar's `src`
    * attribute).
    */
  lazy val fontSource: StyleProp[String] = styleProp("src")

  /** The `unicode-range` descriptor. */
  lazy val unicodeRange: StyleProp[String] = styleProp("unicode-range")

  /** The `font-variation-settings` property. */
  lazy val fontVariationSettings: StyleProp[String] =
    styleProp("font-variation-settings")

  /** The `-webkit-line-clamp` property. */
  lazy val webkitLineClamp: StyleProp[String] = styleProp("-webkit-line-clamp")

  /** The `-webkit-box-orient` property. */
  lazy val webkitBoxOrient: StyleProp[String] = styleProp("-webkit-box-orient")

  /** The `-webkit-user-select` property. */
  lazy val webkitUserSelect: StyleProp[String] =
    styleProp("-webkit-user-select")

  /** The `-webkit-touch-callout` property. */
  lazy val webkitTouchCallout: StyleProp[String] =
    styleProp("-webkit-touch-callout")

  /** The `-webkit-font-smoothing` property. */
  lazy val webkitFontSmoothing: StyleProp[String] =
    styleProp("-webkit-font-smoothing")

  /** The `-webkit-tap-highlight-color` property. */
  lazy val webkitTapHighlightColor: StyleProp[String] =
    styleProp("-webkit-tap-highlight-color")

  /** The `-webkit-text-size-adjust` property. */
  lazy val webkitTextSizeAdjust: StyleProp[String] =
    styleProp("-webkit-text-size-adjust")

  /** The `-webkit-appearance` property. */
  lazy val webkitAppearance: StyleProp[String] = styleProp("-webkit-appearance")
