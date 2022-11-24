package com.svenjacobs.reveal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection

enum class RevealOverlayAlignment {
	Start, Top, End, Bottom,
}

/**
 * Scope for overlay content which provides a Modifier to align an element relative to the
 * reveal area.
 *
 * @see align
 */
@Immutable
interface RevealOverlayScope {

	/**
	 * Aligns the element either to the start, top, end or bottom of the reveal area.
	 *
	 * Should be one of the first modifiers applied to the element so that other modifiers are
	 * applied after the element was positioned.
	 *
	 * @see RevealOverlayAlignment
	 */
	@Stable
	fun Modifier.align(alignment: RevealOverlayAlignment): Modifier
}

internal class RevealOverlayScopeInstance(
	private val revealRect: IntRect,
) : RevealOverlayScope {

	// TODO: fix RTL layout
	override fun Modifier.align(alignment: RevealOverlayAlignment): Modifier = this.then(
		Modifier.layout { measurable, constraints ->
			val placeable = measurable.measure(constraints)
			layout(constraints.maxWidth, constraints.maxHeight) {
				val horizontalCenterX =
					revealRect.left + (revealRect.width - placeable.width) / 2
				val verticalCenterY =
					revealRect.top + (revealRect.height - placeable.height) / 2

				val actualAlignment = when {
					layoutDirection == LayoutDirection.Rtl &&
						alignment == RevealOverlayAlignment.Start -> RevealOverlayAlignment.End
					layoutDirection == LayoutDirection.Rtl &&
						alignment == RevealOverlayAlignment.End -> RevealOverlayAlignment.Start
					else -> alignment
				}

				when (actualAlignment) {
					RevealOverlayAlignment.Start ->
						placeable.place(
							x = revealRect.left - placeable.width,
							y = verticalCenterY,
						)
					RevealOverlayAlignment.Top ->
						placeable.place(
							x = horizontalCenterX,
							y = revealRect.top - placeable.height,
						)
					RevealOverlayAlignment.End ->
						placeable.place(
							x = revealRect.right,
							y = verticalCenterY,
						)
					RevealOverlayAlignment.Bottom ->
						placeable.place(
							x = horizontalCenterX,
							y = revealRect.bottom,
						)
				}
			}
		},
	)
}
