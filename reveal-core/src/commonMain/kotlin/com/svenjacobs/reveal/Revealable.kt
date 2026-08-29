package com.svenjacobs.reveal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Immutable
public data class Revealable(
    val key: Key,
    val shape: RevealShape,
    val padding: PaddingValues,
    val borderStroke: BorderStroke?,
    val layout: Layout,
    val onClick: OnClick?,
) {

    /**
     * @param offset Offset in pixels of revealable to root composable
     * @param size Size in pixels of revealable
     */
    @Immutable
    public data class Layout(val offset: Offset, val size: Size)
}

/**
 * A [Revealable] whose [Revealable.Layout] has been resolved into the final reveal [area]: density,
 * layout direction, the revealable's padding and the shape's own expansion are already applied, and
 * the rect is expressed in window coordinates, matching the overlay popup.
 */
@Immutable
public data class PositionedRevealable(
    val key: Key,
    val shape: RevealShape,
    val padding: PaddingValues,
    val borderStroke: BorderStroke?,
    val area: Rect,
    val onClick: OnClick?,
)

/**
 * The revealables which are currently revealed ([current]) and those which were revealed before and
 * are fading out ([previous]).
 *
 * A key never appears in both lists: revealing a key which is already revealed keeps it in
 * [current] instead of also fading it out.
 */
@Immutable
public data class PositionedRevealables(
    val current: List<PositionedRevealable> = emptyList(),
    val previous: List<PositionedRevealable> = emptyList(),
)

/**
 * Returns [Rect] in pixels of the reveal area including padding for this [Revealable].
 *
 * @param additionalOffset Offset in pixels between the composition root of [Reveal] and the
 *                          window, so that the returned area is expressed in window coordinates,
 *                          matching the overlay popup.
 */
internal fun Revealable.computeArea(
    density: Density,
    layoutDirection: LayoutDirection,
    additionalOffset: Offset,
): Rect = with(density) {
    val x = layout.offset.x + additionalOffset.x
    val y = layout.offset.y + additionalOffset.y
    val rect = Rect(
        left = x - padding.calculateLeftPadding(layoutDirection).toPx(),
        top = y - padding.calculateTopPadding().toPx(),
        right = x + padding.calculateRightPadding(layoutDirection).toPx() + layout.size.width,
        bottom = y + padding.calculateBottomPadding().toPx() + layout.size.height,
    )

    if (shape == RevealShape.Circle) {
        Rect(rect.center, rect.maxDimension / 2.0f)
    } else {
        rect
    }
}
