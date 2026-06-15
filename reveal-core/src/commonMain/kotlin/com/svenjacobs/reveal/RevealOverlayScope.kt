package com.svenjacobs.reveal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

/**
 * Scope for overlay content which provides Modifiers to align an element relative to the
 * reveal area.
 *
 * @see align
 */
@Immutable
public interface RevealOverlayScope {

	/**
	 * Aligns the element horizontally either to the start or end of the reveal area.
	 * Additionally the element is vertically aligned in relation to the reveal area via
	 * [verticalAlignment]. Set [confineHeight] to `false` to not confine the height to the height
	 * of the reveal area. For instance use it with a vertical alignment of [Alignment.Top] to
	 * implement a custom alignment.
	 *
	 * Should be one of the first modifiers applied to the element so that other modifiers are
	 * applied after the element was positioned.
	 *
	 * @param horizontalArrangement Horizontal arrangement (start, end)
	 * @param verticalAlignment Vertical alignment of element in relation to reveal area
	 * @param confineHeight Confine height of element to height of reveal area
	 *
	 * @see RevealOverlayArrangement.Horizontal
	 */
	public fun Modifier.align(
		horizontalArrangement: RevealOverlayArrangement.Horizontal,
		verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
		confineHeight: Boolean = true,
	): Modifier

	/**
	 * Aligns the element vertically either to the top or bottom of the reveal area.
	 * Additionally the element is horizontally aligned in relation to the reveal area via
	 * [horizontalAlignment]. Set [confineWidth] to `false` to not confine the width to the width
	 * of the reveal area. For instance use it with a horizontal alignment of [Alignment.Start] to
	 * implement a custom alignment.
	 *
	 * Should be one of the first modifiers applied to the element so that other modifiers are
	 * applied after the element was positioned.
	 *
	 * @param verticalArrangement Vertical arrangement (top, bottom)
	 * @param horizontalAlignment Horizontal alignment in relation to reveal area
	 * @param confineWidth Confine width of element to width of reveal area
	 *
	 * @see RevealOverlayArrangement.Vertical
	 */
	public fun Modifier.align(
		verticalArrangement: RevealOverlayArrangement.Vertical,
		horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
		confineWidth: Boolean = true,
	): Modifier
}

internal class RevealOverlayScopeInstance(
	private val revealableRect: IntRect,
	private val arrowAnchor: RevealOverlayArrowAnchor,
) : RevealOverlayScope {

	override fun Modifier.align(
		horizontalArrangement: RevealOverlayArrangement.Horizontal,
		verticalAlignment: Alignment.Vertical,
		confineHeight: Boolean,
	): Modifier = this.then(
		Modifier.layout { measurable, constraints ->
			val space = IntSize(
				width = constraints.maxWidth,
				height = constraints.maxHeight,
			)
			val layoutSize = horizontalArrangement.arrange(
				revealable = revealableRect,
				space = space,
				confineHeight = confineHeight,
				layoutDirection = layoutDirection,
			)
			// Constrain the content to the available space so it never exceeds the
			// boundaries of the screen. The width is confined to the arranged layout area
			// (the space beside the reveal area), the height to the whole available space.
			val placeable = measurable.measure(
				constraints.copy(
					maxWidth = layoutSize.width,
					maxHeight = space.height,
				),
			)

			layout(space.width, space.height) {
				val x = horizontalArrangement.align(
					size = placeable.width,
					layout = layoutSize.width,
					space = space.width,
				)
				val y = layoutSize.top +
					verticalAlignment.align(
						size = placeable.height,
						space = layoutSize.height,
					)
				val placedX = x.coerceWithin(size = placeable.width, space = space.width)
				val placedY = y.coerceWithin(size = placeable.height, space = space.height)
				// The content is placed to the side of the reveal area, so the arrow points
				// horizontally and slides along the vertical axis towards the reveal center.
				// The offset is stored relative to the composable's outer center so that the
				// BalloonShape can recover the correct shape-local coordinate via size/2 + offset
				// without needing to know the caller's outer padding value.
				arrowAnchor.offsetX = null
				arrowAnchor.offsetY =
					(revealableRect.center.y - placedY - placeable.height / 2).toFloat()
				placeable.placeRelative(x = placedX, y = placedY)
			}
		},
	)

	override fun Modifier.align(
		verticalArrangement: RevealOverlayArrangement.Vertical,
		horizontalAlignment: Alignment.Horizontal,
		confineWidth: Boolean,
	): Modifier = this.then(
		Modifier.layout { measurable, constraints ->
			val space = IntSize(
				width = constraints.maxWidth,
				height = constraints.maxHeight,
			)
			val layoutSize = verticalArrangement.arrange(
				revealable = revealableRect,
				space = space,
				confineWidth = confineWidth,
			)
			// Constrain the content to the available space so it never exceeds the
			// boundaries of the screen. The height is confined to the arranged layout area
			// (the space above/below the reveal area), the width to the whole available space.
			val placeable = measurable.measure(
				constraints.copy(
					maxWidth = space.width,
					maxHeight = layoutSize.height,
				),
			)

			layout(space.width, space.height) {
				// Using place() instead of placeRelative() because layoutSize and the value
				// returned by horizontalAlignment.align() are RTL-aware
				val x = layoutSize.left +
					horizontalAlignment.align(
						size = placeable.width,
						space = layoutSize.width,
						layoutDirection = layoutDirection,
					)
				val y = verticalArrangement.align(
					size = placeable.height,
					layout = layoutSize.height,
					space = space.height,
				)
				val placedX = x.coerceWithin(size = placeable.width, space = space.width)
				val placedY = y.coerceWithin(size = placeable.height, space = space.height)
				// The content is placed above/below the reveal area, so the arrow points
				// vertically and slides along the horizontal axis towards the reveal center.
				// The offset is stored relative to the composable's outer center so that the
				// BalloonShape can recover the correct shape-local coordinate via size/2 + offset
				// without needing to know the caller's outer padding value.
				arrowAnchor.offsetX =
					(revealableRect.center.x - placedX - placeable.width / 2).toFloat()
				arrowAnchor.offsetY = null
				placeable.place(
					x = placedX,
					y = placedY,
				)
			}
		},
	)
}

/**
 * Coerces this offset so that an element of [size] is fully contained within [space], shifting it
 * back into bounds when it would otherwise overflow the start or end of the available space.
 */
private fun Int.coerceWithin(size: Int, space: Int): Int =
	coerceIn(0, (space - size).coerceAtLeast(0))
