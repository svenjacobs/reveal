package com.svenjacobs.reveal.common.inserter

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset

public typealias DefaultRevealOverlayInserter = InPlaceRevealOverlayInserter

/**
 * Inserts the overlay at the current position in the composition without adding any wrapper or
 * other elements.
 */
public class InPlaceRevealOverlayInserter : RevealOverlayInserter {

	@Composable
	override fun Container(content: @Composable () -> Unit) {
		content()
	}

	override val revealableOffset: DpOffset = DpOffset.Zero
}
