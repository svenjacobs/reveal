package com.svenjacobs.reveal.demo.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.OnClick
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class BottomSheetKeys { Confirm }

/**
 * Demonstrates that the reveal effect is drawn above a `ModalBottomSheet` (issue #100): [Reveal]
 * and its revealables are placed inside the sheet's own content, rather than around it, so the
 * effect always attaches to the same window as the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScreen(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val revealState = rememberRevealState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        LaunchedEffect(Unit) {
            delay(1.seconds)
            revealState.reveal(BottomSheetKeys.Confirm)
        }

        Reveal(
            modifier = Modifier.fillMaxWidth(),
            revealState = revealState,
            onOverlayClick = { scope.launch { revealState.hide() } },
            overlayContent = { key ->
                when (key) {
                    BottomSheetKeys.Confirm -> Balloon(
                        modifier = Modifier
                            .align(verticalArrangement = RevealOverlayArrangement.Top)
                            .padding(8.dp),
                        arrow = Arrow.bottom(anchorToReveal = true),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        elevation = 2.dp,
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "This reveal effect is drawn above the bottom sheet.",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = "This sheet contains its own Reveal, so the effect always draws " +
                        "above it, even though the sheet lives in its own window.",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Button(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .revealable(
                            key = BottomSheetKeys.Confirm,
                            shape = RevealShape.RoundRect(16.dp),
                            onClick = OnClick.Listener {
                                scope.launch { revealState.hide() }
                            },
                        ),
                    onClick = { scope.launch { revealState.hide() } },
                ) {
                    Text("Got it")
                }
            }
        }
    }
}
