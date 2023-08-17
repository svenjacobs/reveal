package com.svenjacobs.reveal.common.inserter

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset

/**
 * Inserts the overlay at the current position in the composition without adding any wrapper or
 * other elements.
 *
 * This will become the default behaviour in the next major version of Reveal with support for
 * Compose Multiplatform.
 */
public class InPlaceRevealOverlayInserter : RevealOverlayInserter {

	@Composable
	override fun Container(content: @Composable () -> Unit) {
		content()
	}

	override val revealableOffset: DpOffset = DpOffset.Zero
}
