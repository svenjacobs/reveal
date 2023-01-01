package com.svenjacobs.reveal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

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
			val placeable = measurable.measure(
				constraints.copy(maxWidth = layoutSize.width),
			)

			layout(layoutSize.width, layoutSize.height) {
				placeable.placeRelative(
					x = horizontalArrangement.align(
						size = placeable.width,
						layout = layoutSize.width,
						space = space.width,
					),
					y = layoutSize.top + verticalAlignment.align(
						size = placeable.height,
						space = layoutSize.height,
					),
				)
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
			val placeable = measurable.measure(
				constraints.copy(maxHeight = layoutSize.height),
			)

			layout(layoutSize.width, layoutSize.height) {
				placeable.placeRelative(
					x = layoutSize.left + horizontalAlignment.align(
						size = placeable.width,
						space = layoutSize.width,
						layoutDirection = LayoutDirection.Ltr, // Ltr because we use placeRelative()
					),
					y = verticalArrangement.align(
						size = placeable.height,
						layout = layoutSize.height,
						space = space.height,
					),
				)
			}
		},
	)
}
