package com.svenjacobs.reveal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection
import java.lang.Integer.max
import java.lang.Integer.min

enum class RevealOverlayAlignment {
	Start, Top, End, Bottom
}

enum class VerticalRevealOverlayAlignment {
	Top, Bottom
}

enum class VerticalRevealOverlayAnchor {
	ParentStart, ParentEnd, ParentCenter, RevealableStart, RevealableEnd, RevealableCenter
}

enum class HorizontalRevealOverlayAlignment {
	Start, End
}

enum class HorizontalRevealOverlayAnchor {
	ParentTop, ParentCenter, ParentBottom, RevealableTop, RevealableCenter, RevealableBottom
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

	@Stable
	fun Modifier.align(verticalAlignment: VerticalRevealOverlayAlignment, anchor: VerticalRevealOverlayAnchor = VerticalRevealOverlayAnchor.RevealableCenter): Modifier

	@Stable
	fun Modifier.align(horizontalAlignment: HorizontalRevealOverlayAlignment, anchor: HorizontalRevealOverlayAnchor = HorizontalRevealOverlayAnchor.RevealableCenter): Modifier
}

internal class RevealOverlayScopeInstance(
	private val revealRect: IntRect,
) : RevealOverlayScope {

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

	override fun Modifier.align(
		verticalAlignment: VerticalRevealOverlayAlignment,
		anchor: VerticalRevealOverlayAnchor
	): Modifier = this.then(
		Modifier.layout { measurable, constraints ->
			val placeable = measurable.measure(constraints)
			layout(constraints.maxWidth, constraints.maxHeight) {
				val actualAnchor = when {
					layoutDirection == LayoutDirection.Rtl
						&& anchor == VerticalRevealOverlayAnchor.ParentStart -> VerticalRevealOverlayAnchor.ParentEnd
					layoutDirection == LayoutDirection.Rtl
						&& anchor == VerticalRevealOverlayAnchor.ParentEnd -> VerticalRevealOverlayAnchor.ParentStart
					layoutDirection == LayoutDirection.Rtl
						&& anchor == VerticalRevealOverlayAnchor.RevealableStart -> VerticalRevealOverlayAnchor.RevealableEnd
					layoutDirection == LayoutDirection.Rtl
						&& anchor == VerticalRevealOverlayAnchor.RevealableEnd -> VerticalRevealOverlayAnchor.RevealableStart
					else -> anchor
				}

				when {
					verticalAlignment == VerticalRevealOverlayAlignment.Top
						&& actualAnchor == VerticalRevealOverlayAnchor.ParentStart -> {
							placeable.place(
								x = 0,
								y = max(revealRect.top - placeable.height, 0)
							)
						}
					verticalAlignment == VerticalRevealOverlayAlignment.Top
						&& actualAnchor == VerticalRevealOverlayAnchor.ParentCenter -> {
							placeable.place(
								x = (constraints.maxWidth / 2) - (placeable.width / 2),
								y = max(revealRect.top - placeable.height, 0)
							)
						}
					verticalAlignment == VerticalRevealOverlayAlignment.Top
						&& actualAnchor == VerticalRevealOverlayAnchor.ParentEnd -> {
						placeable.place(
							x = constraints.maxWidth - placeable.width,
							y = max(revealRect.top - placeable.height, 0)
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Top
						&& actualAnchor == VerticalRevealOverlayAnchor.RevealableStart -> {
						placeable.place(
							x = max(revealRect.left, 0),
							y = max(revealRect.top - placeable.height, 0)
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Top
						&& actualAnchor == VerticalRevealOverlayAnchor.RevealableCenter -> {
						placeable.place(
							x = revealRect.left + (revealRect.width - placeable.width) / 2,
							y = max(revealRect.top - placeable.height, 0)
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Top
						&& actualAnchor == VerticalRevealOverlayAnchor.RevealableEnd -> {
						placeable.place(
							x = min(revealRect.right, constraints.maxWidth),
							y = revealRect.top - placeable.height
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Bottom
						&& actualAnchor == VerticalRevealOverlayAnchor.ParentStart -> {
						placeable.place(
							x = 0,
							y = min(revealRect.bottom, constraints.maxHeight)
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Bottom
						&& actualAnchor == VerticalRevealOverlayAnchor.ParentCenter -> {
						placeable.place(
							x = (constraints.maxWidth / 2) - (placeable.width / 2),
							y = min(revealRect.bottom, constraints.maxHeight)
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Bottom
						&& actualAnchor == VerticalRevealOverlayAnchor.ParentEnd -> {
						placeable.place(
							x = constraints.maxWidth - placeable.width,
							y = min(revealRect.bottom, constraints.maxHeight)
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Bottom
						&& actualAnchor == VerticalRevealOverlayAnchor.RevealableStart -> {
						placeable.place(
							x = revealRect.left,
							y = revealRect.bottom
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Bottom
						&& actualAnchor == VerticalRevealOverlayAnchor.RevealableCenter -> {
						placeable.place(
							x = revealRect.left + (revealRect.width - placeable.width) / 2,
							y = revealRect.bottom
						)
					}
					verticalAlignment == VerticalRevealOverlayAlignment.Bottom
						&& actualAnchor == VerticalRevealOverlayAnchor.RevealableEnd -> {
						placeable.place(
							x = revealRect.right,
							y = revealRect.bottom
						)
					}
				}
			}
		}
	)

	override fun Modifier.align(
		horizontalAlignment: HorizontalRevealOverlayAlignment,
		anchor: HorizontalRevealOverlayAnchor
	): Modifier = this.then(
		Modifier.layout { measurable, constraints ->
			val placeable = measurable.measure(constraints)
			layout(constraints.maxWidth, constraints.maxHeight) {
				val actualAlignment = when {
					layoutDirection == LayoutDirection.Rtl
						&& horizontalAlignment == HorizontalRevealOverlayAlignment.Start -> HorizontalRevealOverlayAlignment.End
					layoutDirection == LayoutDirection.Rtl
						&& horizontalAlignment == HorizontalRevealOverlayAlignment.End -> HorizontalRevealOverlayAlignment.Start
					else -> horizontalAlignment
				}

				when {
					actualAlignment == HorizontalRevealOverlayAlignment.Start
						&& anchor == HorizontalRevealOverlayAnchor.ParentTop -> {
							placeable.place(
								x = max(revealRect.left - placeable.width, 0),
								y = 0
							)
						}
					actualAlignment == HorizontalRevealOverlayAlignment.Start
						&& anchor == HorizontalRevealOverlayAnchor.ParentCenter -> {
							placeable.place(
								x = max(revealRect.left - placeable.width, 0),
								y = (constraints.maxHeight / 2) - (placeable.height / 2)
							)
						}
					actualAlignment == HorizontalRevealOverlayAlignment.Start
						&& anchor == HorizontalRevealOverlayAnchor.ParentBottom -> {
							placeable.place(
								x = max(revealRect.left - placeable.width, 0),
								y = constraints.maxHeight - (placeable.height)
							)
						}
					actualAlignment == HorizontalRevealOverlayAlignment.Start
						&& anchor == HorizontalRevealOverlayAnchor.RevealableTop -> {
							placeable.place(
								x = max(revealRect.left - placeable.width, 0),
								y = max(revealRect.top - placeable.height, 0)
							)
						}
					actualAlignment == HorizontalRevealOverlayAlignment.Start
						&& anchor == HorizontalRevealOverlayAnchor.RevealableCenter -> {
							placeable.place(
								x = max(revealRect.left - placeable.width, 0),
								y = revealRect.top + (revealRect.height - placeable.height) / 2
							)
						}
					actualAlignment == HorizontalRevealOverlayAlignment.Start
						&& anchor == HorizontalRevealOverlayAnchor.RevealableBottom -> {
							placeable.place(
								x = max(revealRect.left - placeable.width, 0),
								y = revealRect.bottom
							)
						}

					actualAlignment == HorizontalRevealOverlayAlignment.End
						&& anchor == HorizontalRevealOverlayAnchor.ParentTop -> {
						placeable.place(
							x = revealRect.right,
							y = 0
						)
					}
					actualAlignment == HorizontalRevealOverlayAlignment.End
						&& anchor == HorizontalRevealOverlayAnchor.ParentCenter -> {
						placeable.place(
							x = revealRect.right,
							y = (constraints.maxHeight / 2) - (placeable.height / 2)
						)
					}
					actualAlignment == HorizontalRevealOverlayAlignment.End
						&& anchor == HorizontalRevealOverlayAnchor.ParentBottom -> {
						placeable.place(
							x = revealRect.right,
							y = constraints.maxHeight - (placeable.height)
						)
					}
					actualAlignment == HorizontalRevealOverlayAlignment.End
						&& anchor == HorizontalRevealOverlayAnchor.RevealableTop -> {
						placeable.place(
							x = revealRect.right,
							y = max(revealRect.top - placeable.height, 0)
						)
					}
					actualAlignment == HorizontalRevealOverlayAlignment.End
						&& anchor == HorizontalRevealOverlayAnchor.RevealableCenter -> {
						placeable.place(
							x = revealRect.right,
							y = revealRect.top + (revealRect.height - placeable.height) / 2
						)
					}
					actualAlignment == HorizontalRevealOverlayAlignment.End
						&& anchor == HorizontalRevealOverlayAnchor.RevealableBottom -> {
						placeable.place(
							x = revealRect.right,
							y = revealRect.bottom
						)
					}
				}
			}
		}
	)
}
