package com.svenjacobs.reveal.effect.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.svenjacobs.reveal.ActualRevealable

internal fun ActualRevealable.createShapePath(
	density: Density,
	layoutDirection: LayoutDirection,
): Path = shape
	.clip(
		size = Size(
			width = revealArea.width,
			height = revealArea.height,
		),
		density = density,
		layoutDirection = layoutDirection,
	)
	.apply {
		translate(
			Offset(
				x = revealArea.left,
				y = revealArea.top,
			),
		)
	}
