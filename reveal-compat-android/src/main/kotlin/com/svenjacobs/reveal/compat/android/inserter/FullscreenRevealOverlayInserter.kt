package com.svenjacobs.reveal.compat.android.inserter

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.DpOffset
import com.svenjacobs.reveal.common.inserter.InPlaceRevealOverlayInserter
import com.svenjacobs.reveal.common.inserter.RevealOverlayInserter

/**
 * Inserts the overlay into a new [ComposeView] which is added to Android's root content view.
 * Thereby the effect is rendered "full screen" regardless where the `Reveal` composable is added
 * in the composition.
 *
 * In order to ensure compatibility with Compose Multiplatform in the future, it is recommended
 * to **not** use this inserter but use [InPlaceRevealOverlayInserter] in apps written fully in
 * Compose.
 *
 * @param revealableOffset Additional offset which is applied to all revealables when using this
 *                         inserter. Should be used to correct misplaced reveal effects where the
 *                         root composables and root content view do not match, e.g. in applications
 *                         that use `ComposeView` in legacy Android views. Use negative values to
 *                         offset towards [0,0] of the coordinate system.
 *
 * @see Fullscreen
 * @see InPlaceRevealOverlayInserter
 */
public class FullscreenRevealOverlayInserter(
	override val revealableOffset: DpOffset = DpOffset.Zero,
) : RevealOverlayInserter {

	@Composable
	override fun Container(content: @Composable () -> Unit) {
		Fullscreen(content = content)
	}
}
