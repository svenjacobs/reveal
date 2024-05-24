package com.svenjacobs.reveal.android.tests.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.RevealCanvas
import com.svenjacobs.reveal.android.tests.presentation.theme.AppTheme
import com.svenjacobs.reveal.rememberRevealCanvasState

@Composable
fun App(modifier: Modifier = Modifier) {
	AppTheme {
		val revealCanvasState = rememberRevealCanvasState()

		RevealCanvas(
			revealCanvasState = revealCanvasState,
			modifier = modifier,
		) {
			MainScreen(revealCanvasState = revealCanvasState)
		}
	}
}
