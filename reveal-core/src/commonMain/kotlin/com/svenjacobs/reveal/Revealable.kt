package com.svenjacobs.reveal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
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

@Immutable
public data class ActualRevealable(
	val key: Key,
	val shape: RevealShape,
	val padding: PaddingValues,
	val borderStroke: BorderStroke?,
	val area: Rect,
	val onClick: OnClick?,
)

/**
 * Returns [Rect] in pixels of the reveal area including padding for this [Revealable].
 */
internal fun Revealable.computeArea(
	density: Density,
	layoutDirection: LayoutDirection,
	additionalOffset: DpOffset,
): Rect = with(density) {
	val rect = Rect(
		left = layout.offset.x +
			additionalOffset.x.toPx() -
			padding.calculateLeftPadding(layoutDirection).toPx(),
		top = layout.offset.y +
			additionalOffset.y.toPx() -
			padding.calculateTopPadding().toPx(),
		right = layout.offset.x +
			additionalOffset.x.toPx() +
			padding.calculateRightPadding(layoutDirection).toPx() +
			layout.size.width,
		bottom = layout.offset.y +
			additionalOffset.y.toPx() +
			padding.calculateBottomPadding().toPx() +
			layout.size.height,
	)

	if (shape == RevealShape.Circle) {
		Rect(rect.center, rect.maxDimension / 2.0f)
	} else {
		rect
	}
}
