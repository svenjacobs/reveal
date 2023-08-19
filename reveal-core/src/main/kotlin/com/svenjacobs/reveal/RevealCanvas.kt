package com.svenjacobs.reveal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.common.inserter.DefaultRevealOverlayInserter
import com.svenjacobs.reveal.common.inserter.RevealOverlayInserter
import com.svenjacobs.reveal.compat.android.inserter.FullscreenRevealOverlayInserter

/**
 * Composable that handles the rendering of the overlay effect.
 *
 * There should be only one instance per application at a top or the topmost position of the Compose
 * hierarchy. The default behaviour is to insert the overlay into the composition at the same
 * position as this composables. If for some reason [RevealCanvas] cannot be the topmost composable,
 * the effect will likely not cover the whole screen and/or reveal effects might be misplaced.
 * In this case pass [FullscreenRevealOverlayInserter] from the `reveal-compat-android` artifact to
 * the [overlayInserter] argument, which however only works for Android targets.
 *
 * @param revealCanvasState State associated with this composable which must also be passed to all
 *                          [Reveal] instances.
 * @param overlayInserter   Strategy of how to insert the overlay into the composition.
 *                          Default behaviour is to insert the overlay at the same position as this
 *                          composable.
 *
 * @see rememberRevealCanvasState
 * @see Reveal
 */
@Composable
public fun RevealCanvas(
	revealCanvasState: RevealCanvasState,
	modifier: Modifier = Modifier,
	overlayInserter: RevealOverlayInserter = DefaultRevealOverlayInserter(),
	content: @Composable () -> Unit,
) {
	SideEffect {
		revealCanvasState.revealableOffset = overlayInserter.revealableOffset
	}

	Box(modifier = modifier) {
		content()

		revealCanvasState.overlayContent?.let { overlayContent ->
			overlayInserter.Container {
				overlayContent()
			}
		}
	}
}
