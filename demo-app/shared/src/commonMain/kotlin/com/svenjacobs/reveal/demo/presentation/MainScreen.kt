package com.svenjacobs.reveal.demo.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.OnClick
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealCanvasState
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Keys { Fab, Explanation, MultipleSelection }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(revealCanvasState: RevealCanvasState, modifier: Modifier = Modifier) {
	val scope = rememberCoroutineScope()
	val revealState = rememberRevealState()

	LaunchedEffect(Unit) {
		if (revealState.isVisible) return@LaunchedEffect
		delay(2.seconds)
		revealState.reveal(Keys.Fab)
	}

	Reveal(
		onOverlayClick = { scope.launch { revealState.hide() } },
		modifier = modifier,
		revealCanvasState = revealCanvasState,
		revealState = revealState,
		overlayContent = { key -> RevealOverlayContent(key) },
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
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
						borderStroke = BorderStroke(2.dp, Color.DarkGray),
						onClick = OnClick.Listener {
							scope.launch { revealState.reveal(Keys.Explanation) }
						},
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
				verticalArrangement = Arrangement.spacedBy(16.dp),
			) {
				Text(
					modifier = Modifier
						.padding(top = 16.dp)
						.revealable(
							key = Keys.Explanation,
							borderStroke = BorderStroke(2.dp, Color.DarkGray),
							onClick = OnClick.Listener {
								scope.launch { revealState.reveal(Keys.MultipleSelection) }
							},
						),
					text = "Reveal is a lightweight, simple reveal effect (also known as " +
						"coach mark or onboarding) library for Compose Multiplatform.",
					style = MaterialTheme.typography.bodyLarge,
					textAlign = TextAlign.Justify,
				)

				Row(
					modifier = Modifier
						.align(Alignment.CenterHorizontally)
						.revealable(
							key = Keys.MultipleSelection,
							onClick = OnClick.Passthrough,
						)
					,
					horizontalArrangement = Arrangement.spacedBy(16.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Button(
						onClick = {
							println("Button 1 was clicked")
						}
					) {
						Text("Option 1")
					}

					Button(
						onClick = {
							println("Button 2 was clicked")
						}
					) {
						Text("Option 2")
					}
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
				horizontalArrangement = RevealOverlayArrangement.Start,
			),
			text = "Click button to get started",
			arrow = Arrow.end(),
		)

		Keys.Explanation -> OverlayText(
			modifier = Modifier.align(
				verticalArrangement = RevealOverlayArrangement.Bottom,
			),
			text = "Actually we already started. This was an example of the reveal effect.",
			arrow = Arrow.top(),
		)

		Keys.MultipleSelection -> OverlayText(
			modifier = Modifier.align(
				verticalArrangement = RevealOverlayArrangement.Bottom,
			),
			text = "Pick one of the options to proceed though the app flow.",
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
