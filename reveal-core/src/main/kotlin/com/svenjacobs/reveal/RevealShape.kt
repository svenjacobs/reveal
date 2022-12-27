package com.svenjacobs.reveal

import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

/**
 * Shape of the reveal area.
 */
public sealed interface RevealShape {

	/**
	 * Returns a [Path] which is used to clip the area around the revealable item specified
	 * via [revealRect].
	 */
	public fun clip(revealRect: ComposeRect, density: Density): Path

	public object Rect : RevealShape {

		override fun clip(revealRect: ComposeRect, density: Density): Path = Path().apply {
			addRect(revealRect)
		}
	}

	public object Circle : RevealShape {

		override fun clip(revealRect: ComposeRect, density: Density): Path =
			Path().apply { addOval(revealRect) }
	}

	public data class RoundRect(
		val cornerSize: Dp,
	) : RevealShape {

		override fun clip(revealRect: ComposeRect, density: Density): Path = Path().apply {
			val size = with(density) { cornerSize.toPx() }
			addRoundRect(RoundRect(revealRect, CornerRadius(size, size)))
		}
	}
}
