package com.svenjacobs.reveal

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

public object RevealOverlayArrangement {

	public object Start : Horizontal {

		override fun arrange(
			revealable: IntRect,
			space: IntSize,
			confineHeight: Boolean,
			layoutDirection: LayoutDirection,
		): IntRect = IntRect(
			left = if (layoutDirection == LayoutDirection.Ltr) 0 else revealable.right,
			top = if (confineHeight) revealable.top else 0,
			right = if (layoutDirection == LayoutDirection.Ltr) revealable.left else space.width,
			bottom = if (confineHeight) revealable.bottom else space.height,
		)

		override fun align(size: Int, layout: Int, space: Int): Int = layout - size
	}

	public object End : Horizontal {

		override fun arrange(
			revealable: IntRect,
			space: IntSize,
			confineHeight: Boolean,
			layoutDirection: LayoutDirection,
		): IntRect = IntRect(
			left = if (layoutDirection == LayoutDirection.Ltr) revealable.right else 0,
			top = if (confineHeight) revealable.top else 0,
			right = if (layoutDirection == LayoutDirection.Ltr) space.width else revealable.left,
			bottom = if (confineHeight) revealable.bottom else space.height,
		)

		override fun align(size: Int, layout: Int, space: Int): Int = space - layout
	}

	public object Top : Vertical {

		override fun arrange(revealable: IntRect, space: IntSize, confineWidth: Boolean): IntRect =
			IntRect(
				left = if (confineWidth) revealable.left else 0,
				top = 0,
				right = if (confineWidth) revealable.right else space.width,
				bottom = revealable.top,
			)

		override fun align(size: Int, layout: Int, space: Int): Int = layout - size
	}

	public object Bottom : Vertical {

		override fun arrange(revealable: IntRect, space: IntSize, confineWidth: Boolean): IntRect =
			IntRect(
				left = if (confineWidth) revealable.left else 0,
				top = revealable.bottom,
				right = if (confineWidth) revealable.right else space.width,
				bottom = space.height,
			)

		override fun align(size: Int, layout: Int, space: Int): Int = space - layout
	}

	public sealed interface Horizontal {

		/**
		 * Returns an [IntRect] which represents the position and size of the overlay layout area
		 * for a [revealable] within available [space].
		 */
		public fun arrange(
			revealable: IntRect,
			space: IntSize,
			confineHeight: Boolean,
			layoutDirection: LayoutDirection,
		): IntRect

		/**
		 * Returns the X offset to place the overlay content with width [size] in available [layout]
		 * width and total [space] width.
		 */
		public fun align(size: Int, layout: Int, space: Int): Int
	}

	public sealed interface Vertical {

		/**
		 * Returns an [IntRect] which represents the position and size of the overlay layout area
		 * for a [revealable] within available [space].
		 */
		public fun arrange(revealable: IntRect, space: IntSize, confineWidth: Boolean): IntRect

		/**
		 * Returns an Y offset to place the overlay content with height [size] in available [layout]
		 * height and total [space] height.
		 */
		public fun align(size: Int, layout: Int, space: Int): Int
	}
}
