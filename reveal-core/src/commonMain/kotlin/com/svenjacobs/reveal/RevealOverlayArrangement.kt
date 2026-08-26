package com.svenjacobs.reveal

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

public object RevealOverlayArrangement {

    public data object Start : Horizontal {

        override fun arrange(
            revealable: IntRect,
            space: IntSize,
            confineHeight: Boolean,
            layoutDirection: LayoutDirection,
        ): IntRect = arrangedRect(
            left = if (layoutDirection == LayoutDirection.Ltr) 0 else revealable.right,
            top = if (confineHeight) revealable.top else 0,
            right = if (layoutDirection == LayoutDirection.Ltr) revealable.left else space.width,
            bottom = if (confineHeight) revealable.bottom else space.height,
            space = space,
        )

        override fun align(size: Int, layout: Int, space: Int): Int = layout - size
    }

    public data object End : Horizontal {

        override fun arrange(
            revealable: IntRect,
            space: IntSize,
            confineHeight: Boolean,
            layoutDirection: LayoutDirection,
        ): IntRect = arrangedRect(
            left = if (layoutDirection == LayoutDirection.Ltr) revealable.right else 0,
            top = if (confineHeight) revealable.top else 0,
            right = if (layoutDirection == LayoutDirection.Ltr) space.width else revealable.left,
            bottom = if (confineHeight) revealable.bottom else space.height,
            space = space,
        )

        override fun align(size: Int, layout: Int, space: Int): Int = space - layout
    }

    public data object Top : Vertical {

        override fun arrange(revealable: IntRect, space: IntSize, confineWidth: Boolean): IntRect =
            arrangedRect(
                left = if (confineWidth) revealable.left else 0,
                top = 0,
                right = if (confineWidth) revealable.right else space.width,
                bottom = revealable.top,
                space = space,
            )

        override fun align(size: Int, layout: Int, space: Int): Int = layout - size
    }

    public data object Bottom : Vertical {

        override fun arrange(revealable: IntRect, space: IntSize, confineWidth: Boolean): IntRect =
            arrangedRect(
                left = if (confineWidth) revealable.left else 0,
                top = revealable.bottom,
                right = if (confineWidth) revealable.right else space.width,
                bottom = space.height,
                space = space,
            )

        override fun align(size: Int, layout: Int, space: Int): Int = space - layout
    }

    public sealed interface Horizontal {

        /**
         * Returns an [IntRect] which represents the position and size of the overlay layout area
         * for a [revealable] within available [space].
         *
         * The returned rect is always contained within [space] and never has a negative width or
         * height, so that its dimensions can be passed to `Constraints` unchecked. When there is
         * no room on the arranged side, the area collapses to zero rather than inverting.
         *
         * [space] itself is expected to be non-negative, which holds for the maximum dimensions of
         * any `Constraints`.
         */
        public fun arrange(
            revealable: IntRect,
            space: IntSize,
            confineHeight: Boolean,
            layoutDirection: LayoutDirection,
        ): IntRect

        /**
         * Returns the X offset to place the overlay content with width [size] in available [layout]
         * width and total [space] width.
         */
        public fun align(size: Int, layout: Int, space: Int): Int
    }

    public sealed interface Vertical {

        /**
         * Returns an [IntRect] which represents the position and size of the overlay layout area
         * for a [revealable] within available [space].
         *
         * The returned rect is always contained within [space] and never has a negative width or
         * height, so that its dimensions can be passed to `Constraints` unchecked. When there is
         * no room on the arranged side, the area collapses to zero rather than inverting.
         *
         * [space] itself is expected to be non-negative, which holds for the maximum dimensions of
         * any `Constraints`.
         */
        public fun arrange(revealable: IntRect, space: IntSize, confineWidth: Boolean): IntRect

        /**
         * Returns an Y offset to place the overlay content with height [size] in available [layout]
         * height and total [space] height.
         */
        public fun align(size: Int, layout: Int, space: Int): Int
    }
}

/**
 * Builds an arranged layout area that is always contained within [space] and never has a negative
 * width or height, so that it can be passed to `Constraints` unchecked. Edges are clamped rather
 * than the rect being rejected: a reveal area inflated past a window edge (by the revealable's
 * padding, or by the circle-shape expansion in [computeArea]) collapses the area on that side to
 * zero instead of inverting it.
 */
private fun arrangedRect(left: Int, top: Int, right: Int, bottom: Int, space: IntSize): IntRect {
    val clampedLeft = left.coerceIn(0, space.width)
    val clampedTop = top.coerceIn(0, space.height)
    return IntRect(
        left = clampedLeft,
        top = clampedTop,
        right = right.coerceIn(clampedLeft, space.width),
        bottom = bottom.coerceIn(clampedTop, space.height),
    )
}
