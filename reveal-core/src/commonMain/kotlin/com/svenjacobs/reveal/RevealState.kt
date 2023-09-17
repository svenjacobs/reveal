package com.svenjacobs.reveal

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @see rememberRevealState
 */
@Stable
@Suppress("MemberVisibilityCanBePrivate")
public class RevealState internal constructor(
	visible: Boolean = false,
	private val restoreCurrentRevealableKey: Key? = null,
) {

	private val mutex = Mutex()
	private var didRestoreCurrentRevealable = false
	private var visible by mutableStateOf(visible)
	private val revealables = mutableStateMapOf<Key, Revealable>()
	internal var currentRevealable by mutableStateOf<Revealable?>(null)
		private set
	internal var previousRevealable by mutableStateOf<Revealable?>(null)
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
		get() = revealables.keys

	/**
	 * Reveals revealable with given [key]
	 *
	 * Might throw [IllegalArgumentException] if the revealable item is not known to Reveal. This
	 * might happen if for example the item is in a lazy container and is currently not part of the
	 * visible area. It is the duty of the developer to ensure that a revealable item is currently
	 * visible (known to Reveal) before calling this function. Additionally [containsRevealable] or
	 * [revealableKeys] can be used to ensure this.
	 *
	 * @see tryReveal
	 * @see containsRevealable
	 * @see revealableKeys
	 * @throws IllegalArgumentException if revealable with given key was not found
	 */
	public suspend fun reveal(key: Key) {
		require(containsRevealable(key)) { "Revealable with key \"$key\" not found" }
		internalReveal(key)
	}

	/**
	 * Like [reveal] but doesn't throw exception if revealable was not found.
	 * Instead returns `false`.
	 *
	 * @see reveal
	 */
	public suspend fun tryReveal(key: Key): Boolean {
		if (!containsRevealable(key)) return false
		internalReveal(key)
		return true
	}

	private suspend fun internalReveal(key: Key) {
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

	/**
	 * Adds a [Revealable] to this state.
	 *
	 * Usually this should not be called manually but revealables registered via the
	 * [RevealScope.revealable] modifier. Only use this function when for instance you want to
	 * reveal legacy Android views.
	 *
	 * @see RevealScope.revealable
	 */
	public fun putRevealable(revealable: Revealable) {
		revealables[revealable.key] = revealable

		if (!didRestoreCurrentRevealable && restoreCurrentRevealableKey == revealable.key) {
			currentRevealable = revealable
			didRestoreCurrentRevealable = true
		}
	}

	/**
	 * Removes a [Revealable] from this state.
	 *
	 * Usually this should not be called manually. The [RevealScope.revealable] modifier takes care
	 * of removing revealables when the composable is disposed.
	 */
	public fun removeRevealable(key: Key) {
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
					restoreCurrentRevealableKey = it[1]?.let { keySaveable ->
						keySaver.restore(keySaveable)
					},
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
