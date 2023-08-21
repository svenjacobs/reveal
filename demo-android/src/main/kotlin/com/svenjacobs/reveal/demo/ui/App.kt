package com.svenjacobs.reveal.demo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.RevealCanvas
import com.svenjacobs.reveal.demo.ui.theme.DemoTheme
import com.svenjacobs.reveal.rememberRevealCanvasState

@Composable
fun App(modifier: Modifier = Modifier) {
	DemoTheme {
		val revealCanvasState = rememberRevealCanvasState()

		RevealCanvas(
			revealCanvasState = revealCanvasState,
			modifier = modifier,
		) {
			MainScreen(revealCanvasState = revealCanvasState)
		}
	}
}
