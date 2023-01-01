package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

public interface Revealable {
	public val key: Key
	public val shape: RevealShape
	public val padding: PaddingValues
}

@Immutable
public class CurrentRevealable(
	override val key: Key,
	override val shape: RevealShape,
	override val padding: PaddingValues,
	public val revealArea: Rect,
) : Revealable

internal interface InternalRevealable : Revealable {
	val layoutCoordinates: LayoutCoordinates
}

@Immutable
internal class InternalRevealableInstance(
	override val key: Key,
	override val shape: RevealShape,
	override val padding: PaddingValues,
	override val layoutCoordinates: LayoutCoordinates,
) : InternalRevealable {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is InternalRevealableInstance) return false

		if (key != other.key) return false

		return true
	}

	override fun hashCode(): Int = key.hashCode()
}

/**
 * Returns [Rect] in pixels of the reveal area including padding for this [Revealable].
 */
internal fun InternalRevealable.getRevealArea(
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
