package com.svenjacobs.reveal.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.demo.ui.theme.DemoTheme
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Keys { Fab, Explanation }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(modifier: Modifier = Modifier) {
	val revealState = rememberRevealState()

	LaunchedEffect(Unit) {
		if (revealState.isVisible) return@LaunchedEffect
		delay(2.seconds)
		revealState.reveal(Keys.Fab)
	}

	DemoTheme {
		val scope = rememberCoroutineScope()

		Reveal(
			modifier = modifier,
			revealState = revealState,
			onRevealableClick = { key ->
				scope.launch {
					if (key == Keys.Fab) {
						revealState.reveal(Keys.Explanation)
					} else {
						revealState.hide()
					}
				}
			},
			onOverlayClick = { scope.launch { revealState.hide() } },
			overlayContent = { key -> RevealOverlayContent(key) },
		) {
			Scaffold(
				modifier = modifier.fillMaxSize(),
				topBar = {
					CenterAlignedTopAppBar(
						title = { Text("Reveal Demo") },
					)
				},
				floatingActionButton = {
					FloatingActionButton(
						modifier = Modifier.revealable(
							key = Keys.Fab,
							shape = RevealShape.RoundRect(16.dp),
						),
						onClick = {
							scope.launch { revealState.reveal(Keys.Explanation) }
						},
					) {
						Icon(
							Icons.Filled.Add,
							contentDescription = null,
						)
					}
				},
			) { contentPadding ->
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(contentPadding)
						.padding(horizontal = 16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					Text(
						modifier = Modifier
							.padding(top = 16.dp)
							.revealable(
								key = Keys.Explanation,
							),
						text = "Reveal is a lightweight, simple reveal effect (also known as " +
							"coach mark or onboarding) library for Jetpack Compose.",
						style = MaterialTheme.typography.bodyLarge,
						textAlign = TextAlign.Justify,
					)
				}
			}
		}
	}
}

@Composable
private fun RevealOverlayScope.RevealOverlayContent(key: Key) {
	when (key) {
		Keys.Fab -> OverlayText(
			modifier = Modifier.align(
				horizontalArrangement = RevealOverlayArrangement.Horizontal.Start,
			),
			text = "Click button to get started",
			arrow = Arrow.end(),
		)
		Keys.Explanation -> OverlayText(
			modifier = Modifier.align(
				verticalArrangement = RevealOverlayArrangement.Vertical.Bottom,
			),
			text = "Actually we already started. This was an example of the reveal effect.",
			arrow = Arrow.top(),
		)
	}
}

@Composable
private fun OverlayText(text: String, arrow: Arrow, modifier: Modifier = Modifier) {
	Balloon(
		modifier = modifier.padding(8.dp),
		arrow = arrow,
		backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
		elevation = 2.dp,
	) {
		Text(
			modifier = Modifier.padding(8.dp),
			text = text,
			style = MaterialTheme.typography.labelLarge,
			textAlign = TextAlign.Center,
		)
	}
}

@Composable
@Preview(showBackground = true)
private fun MainScreenPreview() {
	DemoTheme {
		MainScreen()
	}
}
