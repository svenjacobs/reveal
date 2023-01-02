package com.svenjacobs.reveal.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.ActualRevealable
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealState

/**
 * Overlay effect which renders the background and reveal effect.
 */
public interface RevealOverlayEffect {

	@Composable
	public fun Overlay(
		revealState: RevealState,
		currentRevealable: State<ActualRevealable?>,
		previousRevealable: State<ActualRevealable?>,
		modifier: Modifier,
		content: @Composable RevealOverlayScope.(key: Key) -> Unit,
	)
}
