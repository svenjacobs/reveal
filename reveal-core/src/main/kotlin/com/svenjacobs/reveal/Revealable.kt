package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.layout.LayoutCoordinates

@Immutable
internal class Revealable(
	val key: Key,
	val layoutCoordinates: LayoutCoordinates,
	val padding: PaddingValues,
	val shape: RevealShape,
) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Revealable) return false

		if (key != other.key) return false

		return true
	}

	override fun hashCode(): Int = key.hashCode()
}
