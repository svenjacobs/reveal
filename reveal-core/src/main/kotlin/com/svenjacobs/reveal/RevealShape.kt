package com.svenjacobs.reveal

import androidx.compose.ui.unit.Dp

/**
 * Shape of the reveal area.
 */
sealed interface RevealShape {

	object Rect : RevealShape

	object Circle : RevealShape

	data class RoundRect(
		val cornerSize: Dp,
	) : RevealShape
}
