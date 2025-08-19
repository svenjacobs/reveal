package com.svenjacobs.reveal.shapes.balloon

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.shapes.balloon.Arrow.Companion.bottom
import com.svenjacobs.reveal.shapes.balloon.Arrow.Companion.end
import com.svenjacobs.reveal.shapes.balloon.Arrow.Companion.start
import com.svenjacobs.reveal.shapes.balloon.Arrow.Companion.top

/**
 * An arrow pointing to start, top, end or bottom to be used with [Balloon].
 *
 * @see [start]
 * @see [top]
 * @see [end]
 * @see [bottom]
 * @see Balloon
 */
public sealed interface Arrow {

	public companion object {

		private val DefaultHorizontalWidth = 8.dp
		private val DefaultHorizontalHeight = 12.dp

		@Composable
		@ReadOnlyComposable
		public fun start(
			width: Dp = DefaultHorizontalWidth,
			height: Dp = DefaultHorizontalHeight,
			verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
		): Arrow = when (LocalLayoutDirection.current) {
			LayoutDirection.Ltr -> ::StartInternal
			LayoutDirection.Rtl -> ::EndInternal
		}(width, height, verticalAlignment)

		@Composable
		@ReadOnlyComposable
		public fun top(
			width: Dp = DefaultHorizontalHeight,
			height: Dp = DefaultHorizontalWidth,
			horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
		): Arrow = TopInternal(
			width = width,
			height = height,
			horizontalAlignment = horizontalAlignment,
		)

		@Composable
		@ReadOnlyComposable
		public fun end(
			width: Dp = DefaultHorizontalWidth,
			height: Dp = DefaultHorizontalHeight,
			verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
		): Arrow = when (LocalLayoutDirection.current) {
			LayoutDirection.Ltr -> ::EndInternal
			LayoutDirection.Rtl -> ::StartInternal
		}(width, height, verticalAlignment)

		@Composable
		@ReadOnlyComposable
		public fun bottom(
			width: Dp = DefaultHorizontalHeight,
			height: Dp = DefaultHorizontalWidth,
			horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
		): Arrow = BottomInternal(
			width = width,
			height = height,
			horizontalAlignment = horizontalAlignment,
		)

		private fun Alignment.Horizontal.cornerRadiusOffset(
			cornerRadius: Float,
			layoutDirection: LayoutDirection,
		): Float = when (this) {
			Alignment.Start -> cornerRadius
			Alignment.End -> -cornerRadius
			else -> 0f
		}.let { if (layoutDirection == LayoutDirection.Rtl) -it else it }

		private fun Alignment.Vertical.cornerRadiusOffset(cornerRadius: Float): Float = when (this) {
			Alignment.Top -> cornerRadius
			Alignment.Bottom -> -cornerRadius
			else -> 0f
		}
	}

	public val width: Dp
	public val height: Dp

	/**
	 * [PaddingValues] that must be applied to Balloon content to account for arrow.
	 */
	public val padding: PaddingValues

	/**
	 * Returns [Path] that renders arrow.
	 */
	public fun path(density: Density): Path

	/**
	 * Returns [Offset] which must be applied to [Path] returned by [path] when shape is rendered.
	 */
	public fun offset(
		density: Density,
		size: Size,
		cornerRadius: Float,
		layoutDirection: LayoutDirection,
	): Offset

	private class StartInternal(
		override val width: Dp,
		override val height: Dp,
		private val verticalAlignment: Alignment.Vertical,
	) : Arrow {

		override val padding: PaddingValues = PaddingValues.Absolute(left = width)

		override fun path(density: Density): Path = with(density) {
			Path().apply {
				moveTo(0f, height.toPx() / 2f)
				lineTo(width.toPx(), 0f)
				lineTo(width.toPx(), height.toPx())
				close()
			}
		}

		override fun offset(
			density: Density,
			size: Size,
			cornerRadius: Float,
			layoutDirection: LayoutDirection,
		): Offset = with(density) {
			Offset(
				x = 0f,
				y = verticalAlignment.align(
					size = height.roundToPx(),
					space = size.height.toInt(),
				).toFloat() +
					verticalAlignment.cornerRadiusOffset(cornerRadius),
			)
		}
	}

	private class TopInternal(
		override val width: Dp,
		override val height: Dp,
		private val horizontalAlignment: Alignment.Horizontal,
	) : Arrow {

		override val padding: PaddingValues = PaddingValues.Absolute(top = height)

		override fun path(density: Density): Path = with(density) {
			Path().apply {
				moveTo(0f, height.toPx())
				lineTo(width.toPx() / 2f, 0f)
				lineTo(width.toPx(), height.toPx())
				close()
			}
		}

		override fun offset(
			density: Density,
			size: Size,
			cornerRadius: Float,
			layoutDirection: LayoutDirection,
		): Offset = with(density) {
			Offset(
				x = horizontalAlignment.align(
					size = width.roundToPx(),
					space = size.width.toInt(),
					layoutDirection = layoutDirection,
				).toFloat() +
					horizontalAlignment.cornerRadiusOffset(
						cornerRadius,
						layoutDirection,
					),
				y = 0f,
			)
		}
	}

	private class EndInternal(
		override val width: Dp,
		override val height: Dp,
		private val verticalAlignment: Alignment.Vertical,
	) : Arrow {

		override val padding: PaddingValues = PaddingValues.Absolute(right = width)

		override fun path(density: Density): Path = with(density) {
			Path().apply {
				lineTo(width.toPx(), height.toPx() / 2f)
				lineTo(0f, height.toPx())
				close()
			}
		}

		override fun offset(
			density: Density,
			size: Size,
			cornerRadius: Float,
			layoutDirection: LayoutDirection,
		): Offset = with(density) {
			Offset(
				x = size.width - width.toPx(),
				y = verticalAlignment.align(
					size = height.roundToPx(),
					space = size.height.toInt(),
				).toFloat() +
					verticalAlignment.cornerRadiusOffset(cornerRadius),
			)
		}
	}

	private class BottomInternal(
		override val width: Dp,
		override val height: Dp,
		private val horizontalAlignment: Alignment.Horizontal,
	) : Arrow {

		override val padding: PaddingValues = PaddingValues.Absolute(bottom = height)

		override fun path(density: Density): Path = with(density) {
			Path().apply {
				lineTo(width.toPx() / 2f, height.toPx())
				lineTo(width.toPx(), 0f)
				close()
			}
		}

		override fun offset(
			density: Density,
			size: Size,
			cornerRadius: Float,
			layoutDirection: LayoutDirection,
		): Offset = with(density) {
			Offset(
				x = horizontalAlignment.align(
					width.roundToPx(),
					size.width.toInt(),
					layoutDirection,
				).toFloat() +
					horizontalAlignment.cornerRadiusOffset(
						cornerRadius,
						layoutDirection,
					),
				y = size.height - height.toPx(),
			)
		}
	}
}
