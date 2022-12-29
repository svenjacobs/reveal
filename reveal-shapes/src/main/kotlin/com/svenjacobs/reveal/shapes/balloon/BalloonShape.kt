@file:Suppress("FunctionName")

package com.svenjacobs.reveal.shapes.balloon

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

internal fun BalloonShape(arrow: Arrow, cornerRadius: Dp, density: Density): Shape =
	GenericShape { size, layoutDirection ->
		val cornerRadiusPx = with(density) { cornerRadius.toPx() }
		val arrowPath = arrow.path(density)
		val arrowOffset = arrow.offset(density, size, cornerRadiusPx, layoutDirection)

		addRoundRect(
			with(density) {
				RoundRect(
					left = arrow.padding.calculateLeftPadding(layoutDirection).toPx(),
					top = arrow.padding.calculateTopPadding().toPx(),
					right = size.width - arrow.padding.calculateRightPadding(layoutDirection).toPx(),
					bottom = size.height - arrow.padding.calculateBottomPadding().toPx(),
					cornerRadius = CornerRadius(cornerRadiusPx),
				)
			},
		)

		addPath(
			arrowPath,
			offset = arrowOffset,
		)
	}
