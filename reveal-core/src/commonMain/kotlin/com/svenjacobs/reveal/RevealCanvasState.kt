package com.svenjacobs.reveal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpOffset

/**
 * @see rememberRevealCanvasState
 */
@Stable
public class RevealCanvasState internal constructor() {

	internal var overlayContent: (@Composable () -> Unit)? by mutableStateOf(null)
	internal var revealableOffset: DpOffset by mutableStateOf(DpOffset.Zero)
}

@Composable
public fun rememberRevealCanvasState(): RevealCanvasState = remember { RevealCanvasState() }
