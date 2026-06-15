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

		/**
		 * Default minimum distance the arrow keeps from a rounded corner when [anchorToReveal] is
		 * enabled.
		 */
		public val DefaultCornerMargin: Dp = 4.dp

		@Composable
		@ReadOnlyComposable
		public fun start(
			width: Dp = DefaultHorizontalWidth,
			height: Dp = DefaultHorizontalHeight,
			verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
			anchorToReveal: Boolean = false,
			cornerMargin: Dp = DefaultCornerMargin,
		): Arrow = when (LocalLayoutDirection.current) {
			LayoutDirection.Ltr -> ::StartInternal
			LayoutDirection.Rtl -> ::EndInternal
		}(width, height, verticalAlignment, anchorToReveal, cornerMargin)

		@Composable
		@ReadOnlyComposable
		public fun top(
			width: Dp = DefaultHorizontalHeight,
			height: Dp = DefaultHorizontalWidth,
			horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
			anchorToReveal: Boolean = false,
			cornerMargin: Dp = DefaultCornerMargin,
		): Arrow = TopInternal(
			width = width,
			height = height,
			horizontalAlignment = horizontalAlignment,
			anchorToReveal = anchorToReveal,
			cornerMargin = cornerMargin,
		)

		@Composable
		@ReadOnlyComposable
		public fun end(
			width: Dp = DefaultHorizontalWidth,
			height: Dp = DefaultHorizontalHeight,
			verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
			anchorToReveal: Boolean = false,
			cornerMargin: Dp = DefaultCornerMargin,
		): Arrow = when (LocalLayoutDirection.current) {
			LayoutDirection.Ltr -> ::EndInternal
			LayoutDirection.Rtl -> ::StartInternal
		}(width, height, verticalAlignment, anchorToReveal, cornerMargin)

		@Composable
		@ReadOnlyComposable
		public fun bottom(
			width: Dp = DefaultHorizontalHeight,
			height: Dp = DefaultHorizontalWidth,
			horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
			anchorToReveal: Boolean = false,
			cornerMargin: Dp = DefaultCornerMargin,
		): Arrow = BottomInternal(
			width = width,
			height = height,
			horizontalAlignment = horizontalAlignment,
			anchorToReveal = anchorToReveal,
			cornerMargin = cornerMargin,
		)

		/**
		 * Clamps the desired [center] of the arrow (along its sliding axis, in pixels) so that the
		 * arrow of length [arrowLength] stays within [available] space and keeps at least
		 * [cornerRadius] + [margin] distance from both rounded corners. Returns the offset of the
		 * arrow's leading edge.
		 */
		internal fun clampArrowOffset(
			center: Float,
			arrowLength: Float,
			available: Float,
			cornerRadius: Float,
			margin: Float,
		): Float {
			val min = cornerRadius + margin
			val max = available - arrowLength - cornerRadius - margin
			val leadingEdge = center - arrowLength / 2f
			return leadingEdge.coerceIn(min, maxOf(min, max))
		}

		/**
		 * Converts a center-relative anchor (reveal center offset from the composable's outer
		 * center) to a shape-local coordinate along the arrow's sliding axis.
		 *
		 * For horizontal arrows (Top/Bottom) the sliding axis is the width; for vertical arrows
		 * (Start/End) it is the height.
		 */
		internal fun shapeLocalAnchor(arrow: Arrow, offset: Float, size: Size): Float = when (arrow) {
			is TopInternal, is BottomInternal -> size.width / 2f + offset
			is StartInternal, is EndInternal -> size.height / 2f + offset
		}

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
	 * Whether this arrow dynamically points towards the center of the reveal area. See the
	 * `anchorToReveal` parameter of the [start], [top], [end] and [bottom] factory functions.
	 */
	public val anchorToReveal: Boolean

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
	 *
	 * When this arrow was created with `anchorToReveal = true` and a non-null [anchor] is provided,
	 * the arrow is positioned so that it points towards [anchor] (the coordinate of the reveal area
	 * center along the arrow's sliding axis, in pixels, relative to the balloon), clamped so it does
	 * not overlap the rounded corners. Otherwise the arrow is positioned via its alignment.
	 */
	public fun offset(
		density: Density,
		size: Size,
		cornerRadius: Float,
		layoutDirection: LayoutDirection,
		anchor: Float? = null,
	): Offset

	private class StartInternal(
		override val width: Dp,
		override val height: Dp,
		private val verticalAlignment: Alignment.Vertical,
		override val anchorToReveal: Boolean = false,
		private val cornerMargin: Dp = DefaultCornerMargin,
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
			anchor: Float?,
		): Offset = with(density) {
			Offset(
				x = 0f,
				y = if (anchorToReveal && anchor != null) {
					clampArrowOffset(
						center = anchor,
						arrowLength = height.toPx(),
						available = size.height,
						cornerRadius = cornerRadius,
						margin = cornerMargin.toPx(),
					)
				} else {
					verticalAlignment.align(
						size = height.roundToPx(),
						space = size.height.toInt(),
					).toFloat() +
						verticalAlignment.cornerRadiusOffset(cornerRadius)
				},
			)
		}
	}

	private class TopInternal(
		override val width: Dp,
		override val height: Dp,
		private val horizontalAlignment: Alignment.Horizontal,
		override val anchorToReveal: Boolean = false,
		private val cornerMargin: Dp = DefaultCornerMargin,
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
			anchor: Float?,
		): Offset = with(density) {
			Offset(
				x = if (anchorToReveal && anchor != null) {
					clampArrowOffset(
						center = anchor,
						arrowLength = width.toPx(),
						available = size.width,
						cornerRadius = cornerRadius,
						margin = cornerMargin.toPx(),
					)
				} else {
					horizontalAlignment.align(
						size = width.roundToPx(),
						space = size.width.toInt(),
						layoutDirection = layoutDirection,
					).toFloat() +
						horizontalAlignment.cornerRadiusOffset(
							cornerRadius,
							layoutDirection,
						)
				},
				y = 0f,
			)
		}
	}

	private class EndInternal(
		override val width: Dp,
		override val height: Dp,
		private val verticalAlignment: Alignment.Vertical,
		override val anchorToReveal: Boolean = false,
		private val cornerMargin: Dp = DefaultCornerMargin,
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
			anchor: Float?,
		): Offset = with(density) {
			Offset(
				x = size.width - width.toPx(),
				y = if (anchorToReveal && anchor != null) {
					clampArrowOffset(
						center = anchor,
						arrowLength = height.toPx(),
						available = size.height,
						cornerRadius = cornerRadius,
						margin = cornerMargin.toPx(),
					)
				} else {
					verticalAlignment.align(
						size = height.roundToPx(),
						space = size.height.toInt(),
					).toFloat() +
						verticalAlignment.cornerRadiusOffset(cornerRadius)
				},
			)
		}
	}

	private class BottomInternal(
		override val width: Dp,
		override val height: Dp,
		private val horizontalAlignment: Alignment.Horizontal,
		override val anchorToReveal: Boolean = false,
		private val cornerMargin: Dp = DefaultCornerMargin,
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
			anchor: Float?,
		): Offset = with(density) {
			Offset(
				x = if (anchorToReveal && anchor != null) {
					clampArrowOffset(
						center = anchor,
						arrowLength = width.toPx(),
						available = size.width,
						cornerRadius = cornerRadius,
						margin = cornerMargin.toPx(),
					)
				} else {
					horizontalAlignment.align(
						width.roundToPx(),
						size.width.toInt(),
						layoutDirection,
					).toFloat() +
						horizontalAlignment.cornerRadiusOffset(
							cornerRadius,
							layoutDirection,
						)
				},
				y = size.height - height.toPx(),
			)
		}
	}
}
