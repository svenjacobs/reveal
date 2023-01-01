package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.LayoutCoordinates
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
public class RevealState internal constructor(
	visible: Boolean = false,
	private val restoreRevealableKey: Key? = null,
) {

	private val mutex = Mutex()
	private var didRestoreCurrentRevealable = false
	private var visible by mutableStateOf(visible)
	private val revealables: MutableMap<Key, InternalRevealable> = mutableMapOf()
	internal var currentRevealable by mutableStateOf<InternalRevealable?>(null)
		private set

	public val isVisible: Boolean
		get() = visible

	public val currentRevealableKey: Key?
		get() = currentRevealable?.key

	public suspend fun reveal(key: Key) {
		require(revealables.containsKey(key)) { "Revealable with key \"$key\" not found" }
		mutex.withLock {
			currentRevealable = revealables[key]
			visible = true
		}
	}

	public suspend fun hide() {
		mutex.withLock {
			visible = false
		}
	}

	internal fun onHideAnimationFinished() {
		currentRevealable = null
	}

	internal fun putRevealable(
		key: Key,
		shape: RevealShape,
		padding: PaddingValues,
		layoutCoordinates: LayoutCoordinates,
	) {
		val revealable = InternalRevealableInstance(
			key = key,
			shape = shape,
			padding = padding,
			layoutCoordinates = layoutCoordinates,
		)

		revealables[key] = revealable

		if (!didRestoreCurrentRevealable && restoreRevealableKey == key) {
			currentRevealable = revealable
			didRestoreCurrentRevealable = true
		}
	}

	internal companion object {

		internal fun newSaver(keySaver: Saver<Key, Any>): Saver<RevealState, *> = listSaver(
			save = {
				listOf(
					it.isVisible,
					it.currentRevealableKey?.let { key -> with(keySaver) { save(key) } },
				)
			},
			restore = {
				RevealState(
					visible = it[0] as Boolean,
					restoreRevealableKey = it[1]?.let { keySaveable -> keySaver.restore(keySaveable) },
				)
			},
		)
	}
}

/**
 * Creates a [RevealState] that is remembered across compositions.
 *
 * If a custom type is used for revealable keys which cannot be saved automatically by Compose,
 * a custom saver must be specified via [keySaver].
 *
 * @param keySaver Custom saver for revealable keys
 */
@Composable
public fun rememberRevealState(keySaver: Saver<Key, Any> = autoSaver()): RevealState =
	rememberSaveable(saver = RevealState.newSaver(keySaver)) { RevealState() }
