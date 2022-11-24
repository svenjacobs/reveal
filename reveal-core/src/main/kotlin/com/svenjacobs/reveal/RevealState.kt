package com.svenjacobs.reveal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
class RevealState {

	private val mutex = Mutex()

	internal var visible by mutableStateOf(false)
		private set
	internal var currentRevealable by mutableStateOf<Revealable?>(null)
		private set
	private val revealables: MutableMap<Key, Revealable> = mutableMapOf()

	suspend fun reveal(key: Key) {
		mutex.withLock {
			// TODO: hide when key was not found?
			currentRevealable = revealables[key]
			visible = true
		}
	}

	suspend fun hide() {
		mutex.withLock {
			visible = false
		}
	}

	internal fun putRevealable(revealable: Revealable) {
		revealables[revealable.key] = revealable
	}
}

@Composable
fun rememberRevealState() = remember { RevealState() }
