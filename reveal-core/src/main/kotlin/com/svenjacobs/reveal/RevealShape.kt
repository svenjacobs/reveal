package com.svenjacobs.reveal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Shape of the reveal area.
 */
public sealed interface RevealShape {

	/**
	 * Returns a [Path] which is used to clip the area around the revealable item with given [size].
	 */
	public fun clip(size: Size, density: Density, layoutDirection: LayoutDirection): Path

	public object Rect : RevealShape {

		override fun clip(size: Size, density: Density, layoutDirection: LayoutDirection): Path =
			Path().apply {
				addRect(size.asRect())
			}
	}

	public object Circle : RevealShape {

		override fun clip(size: Size, density: Density, layoutDirection: LayoutDirection): Path =
			Path().apply {
				addOval(size.asRect())
			}
	}

	public class RoundRect(
		private val cornerSize: Dp,
	) : RevealShape {

		override fun clip(size: Size, density: Density, layoutDirection: LayoutDirection): Path =
			Path().apply {
				val cornerSizePx = with(density) { cornerSize.toPx() }
				addRoundRect(
					RoundRect(
						size.asRect(),
						CornerRadius(cornerSizePx, cornerSizePx),
					),
				)
			}
	}

	/**
	 * A custom shape.
	 *
	 * [onClip] should return a Path which is used to clip the area around the revealable item with
	 * given `size`.
	 */
	public class Custom(
		private val onClip: (size: Size, density: Density, layoutDirection: LayoutDirection) -> Path,
	) : RevealShape {

		override fun clip(size: Size, density: Density, layoutDirection: LayoutDirection): Path =
			onClip(size, density, layoutDirection)
	}

	public fun Size.asRect(): ComposeRect = ComposeRect(
		offset = Offset.Zero,
		size = this,
	)
}
