package com.svenjacobs.reveal.demo.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

private enum class MultipleKeys { First, Second, Third }

/**
 * Demonstrates revealing multiple elements at once (issue #105): `reveal()` accepts several keys,
 * the overlay content is rendered once per revealed element, and a click is reported with the key
 * of the element which was actually clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleRevealablesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val revealState = rememberRevealState()
    var lastClickedKey by remember { mutableStateOf<Key?>(null) }

    LaunchedEffect(Unit) {
        if (revealState.isVisible) return@LaunchedEffect
        delay(1.seconds)
        revealState.tryReveal(MultipleKeys.First, MultipleKeys.Second)
    }

    Reveal(
        modifier = modifier,
        revealState = revealState,
        onRevealableClick = { key -> lastClickedKey = key },
        onOverlayClick = { scope.launch { revealState.hide() } },
        overlayContent = { key -> RevealOverlayContent(key) },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Multiple revealables") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
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
                    modifier = Modifier.padding(top = 16.dp),
                    text = "The first two cards are revealed at once, each with its own " +
                        "explanatory balloon, while the third one stays dimmed.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                )

                for (key in MultipleKeys.entries) {
                    ItemCard(
                        modifier = Modifier
                            // Generous spacing so that each explanatory balloon fits into the gap
                            // below its own card.
                            .padding(top = 72.dp)
                            .revealable(
                                key = key,
                                shape = RevealShape.RoundRect(12.dp),
                                borderStroke = BorderStroke(2.dp, Color.DarkGray),
                                onClick = OnClick.Listener { clickedKey ->
                                    lastClickedKey = clickedKey
                                    scope.launch { revealState.hide() }
                                },
                            ),
                        text = "${key.name} card",
                    )
                }

                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = {
                        scope.launch {
                            revealState.reveal(MultipleKeys.First, MultipleKeys.Second)
                        }
                    },
                ) {
                    Text("Reveal first and second")
                }

                Button(
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = { scope.launch { revealState.reveal(MultipleKeys.entries) } },
                ) {
                    Text("Reveal all three")
                }

                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Revealed: ${revealState.currentRevealableKeys.joinToString()}\n" +
                        "Last clicked: ${lastClickedKey ?: "-"}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ItemCard(text: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun RevealOverlayScope.RevealOverlayContent(key: Key) {
    // Called once per revealed element, so each element gets its own explanatory balloon.
    when (key) {
        MultipleKeys.First -> OverlayText(
            modifier = Modifier.align(verticalArrangement = RevealOverlayArrangement.Bottom),
            text = "These two cards belong together …",
            arrow = Arrow.top(anchorToReveal = true),
        )

        MultipleKeys.Second -> OverlayText(
            modifier = Modifier.align(verticalArrangement = RevealOverlayArrangement.Bottom),
            text = "… so they are revealed at the same time.",
            arrow = Arrow.top(anchorToReveal = true),
        )

        MultipleKeys.Third -> OverlayText(
            modifier = Modifier.align(verticalArrangement = RevealOverlayArrangement.Bottom),
            text = "And this one joins when all three are revealed.",
            arrow = Arrow.top(anchorToReveal = true),
        )
    }
}
