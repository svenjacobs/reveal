package com.svenjacobs.reveal.shapes.balloon

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A balloon (speech / chat bubble) with an arrow on one side.
 *
 * This variant specifies a [backgroundColor]. See other variant for [Brush] support.
 *
 * @see Arrow
 */
@Composable
public fun Balloon(
	arrow: Arrow,
	backgroundColor: Color,
	modifier: Modifier = Modifier,
	cornerRadius: Dp = 8.dp,
	elevation: Dp = 0.dp,
	contentAlignment: Alignment = Alignment.TopStart,
	content: @Composable BoxScope.() -> Unit,
) {
	Balloon(
		arrow = arrow,
		modifier = modifier,
		backgroundModifier = Modifier.background(color = backgroundColor),
		cornerRadius = cornerRadius,
		elevation = elevation,
		contentAlignment = contentAlignment,
		content = content,
	)
}

/**
 * A balloon (speech / chat bubble) with an arrow on one side.
 *
 * This variant specifies a [backgroundBrush] and optional [backgroundAlpha]. See other variant for
 * simple [Color] support.
 *
 * @see Arrow
 */
@Composable
public fun Balloon(
	arrow: Arrow,
	backgroundBrush: Brush,
	modifier: Modifier = Modifier,
	@FloatRange(from = 0.0, to = 1.0) backgroundAlpha: Float = 1.0f,
	cornerRadius: Dp = 8.dp,
	elevation: Dp = 0.dp,
	contentAlignment: Alignment = Alignment.TopStart,
	content: @Composable BoxScope.() -> Unit,
) {
	Balloon(
		arrow = arrow,
		modifier = modifier,
		backgroundModifier = Modifier.background(
			brush = backgroundBrush,
			alpha = backgroundAlpha,
		),
		cornerRadius = cornerRadius,
		elevation = elevation,
		contentAlignment = contentAlignment,
		content = content,
	)
}

@Composable
private fun Balloon(
	arrow: Arrow,
	cornerRadius: Dp,
	elevation: Dp,
	contentAlignment: Alignment,
	modifier: Modifier = Modifier,
	backgroundModifier: Modifier = Modifier,
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
			.then(backgroundModifier)
			.padding(arrow.padding),
		contentAlignment = contentAlignment,
		content = content,
	)
}
