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
    visible: Boolean,
    private val restoreCurrentRevealableKeys: List<Key>,
) {

    public constructor() : this(
        visible = false,
        restoreCurrentRevealableKeys = emptyList(),
    )

    private val mutex = Mutex()
    private var didRestoreCurrentRevealables = restoreCurrentRevealableKeys.isEmpty()
    private var visible by mutableStateOf(visible)
    private val revealables = mutableStateMapOf<Key, Revealable>()
    internal var currentRevealables by mutableStateOf<List<Revealable>>(emptyList())
        private set
    internal var previousRevealables by mutableStateOf<List<Revealable>>(emptyList())
        private set

    /**
     * Returns `true` if reveal effect is visible, else `false`
     */
    public val isVisible: Boolean
        get() = visible

    /**
     * Observable keys of the currently revealed items, in the order they were passed to [reveal],
     * or an empty list if no revealable is currently visible
     *
     * @see previousRevealableKeys
     */
    public val currentRevealableKeys: List<Key>
        get() = currentRevealables.map(Revealable::key)

    /**
     * Observable keys of the previous revealables which were displayed before
     * [currentRevealableKeys]
     *
     * Keys which are still revealed are not contained in this list.
     *
     * @see currentRevealableKeys
     */
    public val previousRevealableKeys: List<Key>
        get() = previousRevealables.map(Revealable::key)

    /**
     * Observable key of current revealable or `null` if no revealable is currently visible
     *
     * If multiple revealables are revealed at once, this is the first of [currentRevealableKeys].
     *
     * @see currentRevealableKeys
     * @see previousRevealableKey
     */
    public val currentRevealableKey: Key?
        get() = currentRevealables.firstOrNull()?.key

    /**
     * Observable key of previous revealable which was displayed before [currentRevealableKey]
     *
     * If multiple revealables were revealed at once, this is the first of [previousRevealableKeys].
     *
     * @see previousRevealableKeys
     * @see currentRevealableKey
     */
    public val previousRevealableKey: Key?
        get() = previousRevealables.firstOrNull()?.key

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
     * The reveal area tracks the element for as long as it is revealed: if the element moves or is
     * resized afterwards — because of a later layout pass, a recomposition, insets settling or
     * animated content — the effect follows it. There is no need to wait for the layout to become
     * stable before calling this function.
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
        internalReveal(listOf(key))
    }

    /**
     * Reveals all revealables with given [keys] at once
     *
     * @see reveal
     * @throws IllegalArgumentException if [keys] is empty or any of the revealables was not found
     */
    public suspend fun reveal(vararg keys: Key): Unit = reveal(keys.asIterable())

    /**
     * Reveals all revealables with given [keys] at once
     *
     * All keys must be known to Reveal: if any of them is missing, nothing is revealed and an
     * exception is thrown.
     *
     * The reveal areas track their elements for as long as they are revealed: if an element moves
     * or is resized afterwards — because of a later layout pass, a recomposition, insets settling
     * or animated content — the effect follows it. There is no need to wait for the layout to
     * become stable before calling this function.
     *
     * @see reveal
     * @see tryReveal
     * @see containsRevealable
     * @see revealableKeys
     * @throws IllegalArgumentException if [keys] is empty or any of the revealables was not found
     */
    public suspend fun reveal(keys: Iterable<Key>) {
        val list = keys.toList()
        require(list.isNotEmpty()) { "No keys specified" }
        val missing = list.filterNot(::containsRevealable)
        require(missing.isEmpty()) {
            "Revealables with keys ${missing.joinToString { "\"$it\"" }} not found"
        }
        internalReveal(list)
    }

    /**
     * Like [reveal] but doesn't throw exception if revealable was not found.
     * Instead returns `false`.
     *
     * @see reveal
     */
    public suspend fun tryReveal(key: Key): Boolean {
        if (!containsRevealable(key)) return false
        internalReveal(listOf(key))
        return true
    }

    /**
     * Like [reveal] but doesn't throw exception if a revealable was not found.
     * Instead returns `false`.
     *
     * @see reveal
     */
    public suspend fun tryReveal(vararg keys: Key): Boolean = tryReveal(keys.asIterable())

    /**
     * Like [reveal] but doesn't throw exception if a revealable was not found.
     * Instead returns `false`.
     *
     * Either all revealables are revealed or, if any of [keys] is unknown, none of them.
     *
     * @see reveal
     */
    public suspend fun tryReveal(keys: Iterable<Key>): Boolean {
        val list = keys.toList()
        if (list.isEmpty() || !list.all(::containsRevealable)) return false
        internalReveal(list)
        return true
    }

    private suspend fun internalReveal(keys: List<Key>) {
        // Duplicates would result in the same key being used twice for the composition groups of
        // the overlay effect, so they are dropped here rather than in each effect.
        val distinctKeys = keys.distinct()

        mutex.withLock {
            val next = distinctKeys.mapNotNull(revealables::get)
            // A key which stays revealed must not fade out at the same time, which would draw its
            // reveal area twice with two different alpha values.
            previousRevealables = currentRevealables.filterNot { it.key in distinctKeys }
            currentRevealables = next
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
        currentRevealables = emptyList()
        previousRevealables = emptyList()
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
    public fun addRevealable(revealable: Revealable) {
        revealables[revealable.key] = revealable

        // Keep the revealed geometry in sync with later layout passes. onGloballyPositioned fires
        // again whenever the element moves or is resized, so without this the effect would stay
        // frozen at the position the element had when reveal() was called.
        //
        // Only Layout is carried over, and only when it really changed. The remaining properties
        // are deliberately kept as captured at reveal time. They come from the modifier call site
        // and are reallocated on every recomposition, and while most of them compare by value,
        // OnClick.Listener and RevealShape.Custom wrap caller-supplied lambdas and so cannot.
        // Assigning the incoming Revealable wholesale would therefore write a never-equal value
        // into snapshot state from the layout phase, invalidating the composition that reads it,
        // which reallocates those lambdas and re-triggers this callback — composition would never
        // go idle. Offset and Size do compare by value, so this converges once the layout settles.
        currentRevealables = currentRevealables.withLayoutOf(revealable)
        previousRevealables = previousRevealables.withLayoutOf(revealable)

        if (!didRestoreCurrentRevealables &&
            revealable.key in restoreCurrentRevealableKeys &&
            currentRevealables.none { it.key == revealable.key }
        ) {
            currentRevealables = (currentRevealables + revealable)
                .sortedBy { restoreCurrentRevealableKeys.indexOf(it.key) }
            didRestoreCurrentRevealables =
                currentRevealables.size == restoreCurrentRevealableKeys.size
        }
    }

    /**
     * @see addRevealable
     */
    @Deprecated(
        message = "Use addRevealable()",
        replaceWith = ReplaceWith("addRevealable(revealable)"),
    )
    public fun putRevealable(revealable: Revealable): Unit = addRevealable(revealable)

    /**
     * Removes a [Revealable] from this state.
     *
     * Usually this should not be called manually. The [RevealScope.revealable] modifier takes care
     * of removing revealables when the composable is disposed.
     */
    public fun removeRevealable(key: Key) {
        revealables.remove(key)

        if (currentRevealables.any { it.key == key }) {
            if (currentRevealables.size == 1) {
                // Hide effect if the last current revealable left the composition.
                // currentRevealables and previousRevealables are reset via
                // onHideAnimationFinished(), so the item can still fade out.
                visible = false
            } else {
                // One of several revealed items disappears without a fade-out. Moving it into
                // previousRevealables would animate it, at the cost of a second code path for a
                // rather exotic case (an element leaving the composition while others stay
                // revealed).
                currentRevealables = currentRevealables.filterNot { it.key == key }
            }
        }

        previousRevealables = previousRevealables.filterNot { it.key == key }
    }

    /**
     * Returns this list with the [Revealable.Layout] of [incoming] applied to the entry with the
     * same key, or this very instance when nothing changed, so that no write to snapshot state
     * happens while the layout is stable.
     */
    private fun List<Revealable>.withLayoutOf(incoming: Revealable): List<Revealable> =
        if (none { it.key == incoming.key && it.layout != incoming.layout }) {
            this
        } else {
            map { if (it.key == incoming.key) it.copy(layout = incoming.layout) else it }
        }

    internal companion object {

        internal fun newSaver(keySaver: Saver<Key, Any>): Saver<RevealState, *> = listSaver(
            save = {
                listOf(
                    it.isVisible,
                    it.currentRevealableKeys.map { key -> with(keySaver) { save(key) } },
                )
            },
            restore = {
                RevealState(
                    visible = it[0] as Boolean,
                    restoreCurrentRevealableKeys = (it[1] as List<*>).mapNotNull { keySaveable ->
                        keySaveable?.let(keySaver::restore)
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
