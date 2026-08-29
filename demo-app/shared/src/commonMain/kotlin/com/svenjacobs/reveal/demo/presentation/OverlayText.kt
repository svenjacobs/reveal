package com.svenjacobs.reveal.demo.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon

/**
 * Explanatory balloon which is placed next to a reveal area, shared by the demo screens.
 */
@Composable
fun OverlayText(text: String, arrow: Arrow, modifier: Modifier = Modifier) {
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
