package com.svenjacobs.reveal

import androidx.compose.ui.unit.Dp

/**
 * Shape of the reveal area.
 *
 * TODO: Shapes should draw themselves via a draw() method
 */
public sealed interface RevealShape {

	public object Rect : RevealShape

	public object Circle : RevealShape

	public data class RoundRect(
		val cornerSize: Dp,
	) : RevealShape
}
