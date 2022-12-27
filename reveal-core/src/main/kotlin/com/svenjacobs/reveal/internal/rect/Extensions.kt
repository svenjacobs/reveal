package com.svenjacobs.reveal.internal.rect

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntRect

internal fun Rect.toIntRect(): IntRect = IntRect(
	left = left.toInt(),
	top = top.toInt(),
	right = right.toInt(),
	bottom = bottom.toInt(),
)
