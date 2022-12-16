package com.svenjacobs.reveal.demo.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svenjacobs.reveal.HorizontalRevealOverlayAlignment
import com.svenjacobs.reveal.HorizontalRevealOverlayAnchor
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayAlignment
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.VerticalRevealOverlayAlignment
import com.svenjacobs.reveal.VerticalRevealOverlayAnchor
import com.svenjacobs.reveal.demo.ui.theme.DemoTheme
import com.svenjacobs.reveal.rememberRevealState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Keys { Fab, Explanation, BarItem }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(modifier: Modifier = Modifier) {
	val revealState = rememberRevealState()

	LaunchedEffect(Unit) {
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
					when (key) {
						Keys.Fab -> {
							revealState.reveal(Keys.Explanation)
						}
						Keys.Explanation -> {
							revealState.reveal(Keys.BarItem)
						}
						else -> {
							revealState.hide()
						}
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
				bottomBar = {
					BottomNavBar()
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
fun RevealScope.BottomNavBar() {
	var selectedIndex by remember { mutableStateOf(0) }
	BottomAppBar {
		for (i in 0 until 5) {
			val m = if (i == 0) Modifier.revealable(Keys.BarItem) else Modifier
			NavigationBarItem(
				modifier = m,
				selected = selectedIndex == i,
				onClick = { selectedIndex = i },
				interactionSource = MutableInteractionSource(),
				icon = {
					Icon(
						imageVector = Icons.Filled.Add,
						contentDescription = null
					)
				},
				label = {
					Text(text = "Item")
				}
			)
		}
	}
}

@Composable
fun RevealOverlayScope.RevealOverlayContent(key: Key) {
	when (key) {
		Keys.Fab -> OverlayText(
			modifier = Modifier.align(RevealOverlayAlignment.Start),
			text = "Click button to get started",
		)
		Keys.Explanation -> OverlayText(
			modifier = Modifier.align(RevealOverlayAlignment.Bottom),
			text = "Actually we already started. This was an example of the reveal effect.",
		)
		Keys.BarItem -> OverlayText(
			modifier = Modifier.align(VerticalRevealOverlayAlignment.Top, VerticalRevealOverlayAnchor.ParentCenter),
			text = "This is an example of aligning an overlay in the parent"
		)
	}
}

@Composable
private fun OverlayText(text: String, modifier: Modifier = Modifier) {
	Surface(
		modifier = modifier.padding(8.dp),
		shape = RoundedCornerShape(4.dp),
		color = MaterialTheme.colorScheme.secondaryContainer,
	) {
		Text(
			modifier = Modifier.padding(4.dp),
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
