package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
@Suppress("MemberVisibilityCanBePrivate")
public class RevealState internal constructor(
	visible: Boolean = false,
	private val restoreRevealableKey: Key? = null,
) {

	private val mutex = Mutex()
	private var didRestoreCurrentRevealable = false
	private var visible by mutableStateOf(visible)
	private val revealables = mutableStateMapOf<Key, InternalRevealable>()
	internal var currentRevealable by mutableStateOf<InternalRevealable?>(null)
		private set
	internal var previousRevealable by mutableStateOf<InternalRevealable?>(null)
		private set

	/**
	 * Returns `true` if reveal effect is visible, else `false`
	 */
	public val isVisible: Boolean
		get() = visible

	/**
	 * Observable key of current revealable or `null` if no revealable is currently visible
	 *
	 * @see previousRevealableKey
	 */
	public val currentRevealableKey: Key?
		get() = currentRevealable?.key

	/**
	 * Observable key of previous revealable which was displayed before [currentRevealableKey]
	 *
	 * @see currentRevealableKey
	 */
	public val previousRevealableKey: Key?
		get() = previousRevealable?.key

	/**
	 * Observable set of keys known to this state instance
	 *
	 * Can be used to query when a revealable was registered via [RevealScope.revealable].
	 */
	public val revealableKeys: Set<Key>
		get() = revealables.keys.toSet()

	/**
	 * Reveals revealable with given [key]
	 *
	 * @see containsRevealable
	 * @throws IllegalArgumentException if revealable with given key was not found
	 */
	public suspend fun reveal(key: Key) {
		require(revealables.containsKey(key)) { "Revealable with key \"$key\" not found" }
		mutex.withLock {
			previousRevealable = currentRevealable
			currentRevealable = revealables[key]
			visible = true
		}
	}

	/**
	 * Hides reveal effect
	 */
	public suspend fun hide() {
		mutex.withLock {
			visible = false
		}
	}

	/**
	 * Returns `true` if this state instance contains revealable with given [key]
	 */
	public fun containsRevealable(key: Key): Boolean = revealableKeys.contains(key)

	internal fun onHideAnimationFinished() {
		currentRevealable = null
		previousRevealable = null
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

	/**
	 * Is called from [RevealScope.revealable] when the composable is disposed.
	 */
	internal fun removeRevealable(key: Key) {
		revealables.remove(key)

		// Hide effect if the current revealable left the composition.
		// currentRevealable and previousRevealable are reset via onHideAnimationFinished().
		if (currentRevealableKey == key) {
			visible = false
		}

		if (previousRevealableKey == key) {
			previousRevealable = null
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
