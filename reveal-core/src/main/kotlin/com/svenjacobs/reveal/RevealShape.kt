package com.svenjacobs.reveal

import androidx.compose.ui.unit.Dp

/**
 * Shape of the reveal area.
 *
 * TODO: Shapes should draw themselves via a draw() method
 */
sealed interface RevealShape {

	object Rect : RevealShape

	object Circle : RevealShape

	data class RoundRect(
		val cornerSize: Dp,
	) : RevealShape
}
