package com.svenjacobs.reveal.internal.revealable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.RevealShape

@Immutable
internal class Revealable(
	val key: Key,
	val layoutCoordinates: LayoutCoordinates,
	val padding: PaddingValues,
	val shape: RevealShape,
) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Revealable) return false

		if (key != other.key) return false

		return true
	}

	override fun hashCode(): Int = key.hashCode()
}

/**
 * Returns [Rect] in pixels of the reveal area including padding for this [Revealable].
 */
internal fun Revealable.getRevealArea(
	containerPositionInRoot: Offset,
	density: Density,
	layoutDirection: LayoutDirection,
): Rect {
	val pos = layoutCoordinates.positionInRoot() - containerPositionInRoot
	return with(density) {
		val rect = Rect(
			left = pos.x - padding.calculateLeftPadding(layoutDirection).toPx(),
			top = pos.y - padding.calculateTopPadding().toPx(),
			right = pos.x + padding.calculateRightPadding(layoutDirection)
				.toPx() + layoutCoordinates.size.width.toFloat(),
			bottom = pos.y + padding.calculateBottomPadding()
				.toPx() + layoutCoordinates.size.height.toFloat(),
		)

		if (shape == RevealShape.Circle) {
			Rect(rect.center, rect.maxDimension / 2.0f)
		} else {
			rect
		}
	}
}
