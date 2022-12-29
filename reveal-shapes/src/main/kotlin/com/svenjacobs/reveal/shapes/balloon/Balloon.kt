package com.svenjacobs.reveal.shapes.balloon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A balloon (speech / chat bubble) with an arrow on one side.
 *
 * @see Arrow
 */
@Composable
public fun Balloon(
	arrow: Arrow,
	color: Color,
	modifier: Modifier = Modifier,
	cornerRadius: Dp = 8.dp,
	elevation: Dp = 0.dp,
	contentAlignment: Alignment = Alignment.TopStart,
	content: @Composable BoxScope.() -> Unit,
) {
	Box(
		modifier = modifier
			.graphicsLayer {
				shadowElevation = elevation.toPx()
				shape = BalloonShape(
					arrow = arrow,
					cornerRadius = cornerRadius,
					density = this@graphicsLayer,
				)
				clip = true
			}
			.background(color = color)
			.padding(arrow.padding),
		contentAlignment = contentAlignment,
		content = content,
	)
}
