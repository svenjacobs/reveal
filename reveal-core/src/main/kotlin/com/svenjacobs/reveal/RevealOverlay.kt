package com.svenjacobs.reveal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

public object RevealOverlayArrangement {

	public sealed interface Horizontal {

		/**
		 * Returns an [IntRect] which represents the position and size of the overlay layout area
		 * for a [revealable] within available [space].
		 */
		public fun arrange(
			revealable: IntRect,
			space: IntSize,
			confineHeight: Boolean,
		): IntRect

		/**
		 * Returns an [IntOffset] to place the overlay content with [size] in available [layout].
		 */
		public fun align(
			size: IntSize,
			layout: IntRect,
		): IntOffset

		public object Start : Horizontal {

			override fun arrange(
				revealable: IntRect,
				space: IntSize,
				confineHeight: Boolean,
			): IntRect = IntRect(
				left = 0,
				top = if (confineHeight) revealable.top else 0,
				right = revealable.left,
				bottom = if (confineHeight) revealable.bottom else space.height,
			)

			override fun align(size: IntSize, layout: IntRect): IntOffset =
				IntOffset(x = layout.right - size.width, y = 0)
		}

		public object End : Horizontal {

			override fun arrange(
				revealable: IntRect,
				space: IntSize,
				confineHeight: Boolean,
			): IntRect = IntRect(
				left = revealable.right,
				top = if (confineHeight) revealable.top else 0,
				right = space.width,
				bottom = if (confineHeight) revealable.bottom else space.height,
			)

			override fun align(size: IntSize, layout: IntRect): IntOffset =
				IntOffset(x = layout.left, y = 0)
		}
	}

	public sealed interface Vertical {

		/**
		 * Returns an [IntRect] which represents the position and size of the overlay layout area
		 * for a [revealable] within available [space].
		 */
		public fun arrange(
			revealable: IntRect,
			space: IntSize,
			confineWidth: Boolean,
		): IntRect

		/**
		 * Returns an [IntOffset] to place the overlay content with [size] in available [layout].
		 */
		public fun align(
			size: IntSize,
			layout: IntRect,
		): IntOffset

		public object Top : Vertical {

			override fun arrange(
				revealable: IntRect,
				space: IntSize,
				confineWidth: Boolean,
			): IntRect =
				IntRect(
					left = if (confineWidth) revealable.left else 0,
					top = 0,
					right = if (confineWidth) revealable.right else space.width,
					bottom = revealable.top,
				)

			override fun align(
				size: IntSize,
				layout: IntRect,
			): IntOffset = IntOffset(
				x = 0,
				y = layout.bottom - size.height,
			)
		}

		public object Bottom : Vertical {

			override fun arrange(
				revealable: IntRect,
				space: IntSize,
				confineWidth: Boolean,
			): IntRect =
				IntRect(
					left = if (confineWidth) revealable.left else 0,
					top = revealable.bottom,
					right = if (confineWidth) revealable.right else space.width,
					bottom = space.height,
				)

			override fun align(
				size: IntSize,
				layout: IntRect,
			): IntOffset = IntOffset(
				x = 0,
				y = layout.top,
			)
		}
	}
}

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
	private val revealRect: IntRect,
) : RevealOverlayScope {

	override fun Modifier.align(
		horizontalArrangement: RevealOverlayArrangement.Horizontal,
		verticalAlignment: Alignment.Vertical,
		confineHeight: Boolean,
	): Modifier = this.then(
		Modifier.layout { measurable, constraints ->
			val layoutSize = horizontalArrangement.arrange(
				revealable = revealRect,
				space = IntSize(
					width = constraints.maxWidth,
					height = constraints.maxHeight,
				),
				confineHeight = confineHeight,
			)
			val placeable = measurable.measure(
				constraints.copy(maxWidth = layoutSize.width),
			)

			layout(placeable.width, placeable.height) {
				placeable.placeRelative(
					x = horizontalArrangement.align(
						size = IntSize(placeable.width, placeable.height),
						layout = layoutSize,
					).x,
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
			val layoutSize = verticalArrangement.arrange(
				revealable = revealRect,
				space = IntSize(
					width = constraints.maxWidth,
					height = constraints.maxHeight,
				),
				confineWidth = confineWidth,
			)
			val placeable = measurable.measure(
				constraints.copy(maxHeight = layoutSize.height),
			)

			layout(placeable.width, placeable.height) {
				placeable.placeRelative(
					x = layoutSize.left + horizontalAlignment.align(
						size = placeable.width,
						space = layoutSize.width,
						layoutDirection = LayoutDirection.Ltr, // Ltr because we use placeRelative()
					),
					y = verticalArrangement.align(
						size = IntSize(placeable.width, placeable.height),
						layout = layoutSize,
					).y,
				)
			}
		},
	)
}
