package com.svenjacobs.reveal.demo.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.OnClick
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Keys { Fab, Explanation }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(onShowMultipleRevealables: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val revealState = rememberRevealState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (revealState.isVisible) return@LaunchedEffect
        delay(2.seconds)
        revealState.reveal(Keys.Fab)
    }

    if (showBottomSheet) {
        BottomSheetScreen(onDismissRequest = { showBottomSheet = false })
    }

    Reveal(
        onOverlayClick = { scope.launch { revealState.hide() } },
        modifier = modifier,
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
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .revealable(
                            key = Keys.Explanation,
                            borderStroke = BorderStroke(2.dp, Color.DarkGray),
                            onClick = OnClick.Listener {
                                scope.launch { revealState.hide() }
                            },
                        ),
                    text = "Reveal is a lightweight, simple reveal effect (also known as " +
                        "coach mark or onboarding) library for Compose Multiplatform.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                )

                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = { showBottomSheet = true },
                ) {
                    Text("Show bottom sheet demo")
                }

                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = onShowMultipleRevealables,
                ) {
                    Text("Show multiple revealables demo")
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
            arrow = Arrow.end(anchorToReveal = true),
        )

        Keys.Explanation -> OverlayText(
            modifier = Modifier.align(
                verticalArrangement = RevealOverlayArrangement.Bottom,
            ),
            text = "Actually we already started. This was an example of the reveal effect.",
            arrow = Arrow.top(anchorToReveal = true),
        )
    }
}

