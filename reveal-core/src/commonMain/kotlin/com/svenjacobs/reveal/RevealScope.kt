package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

/**
 * Scope inside [Reveal]'s contents which provides [revealable] modifier.
 */
@Immutable
public interface RevealScope {

	/**
	 * Registers the element as a revealable item.
	 *
	 * [key] must be unique in the current scope and should be used for [RevealState.reveal].
	 * Internally [Modifier.onGloballyPositioned] is used. Hence elements are only registered after
	 * they have been laid out.
	 *
	 * If the element that this modifier is applied to leaves the composition while the reveal
	 * effect is shown for the element, the effect is finished.
	 *
	 * @param key     Unique key to identify the revealable content. Also see documentation of [Key].
	 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
	 *                with a corner size of 4 dp.
	 * @param padding Additional padding around the reveal area. Positive values increase area while
	 *                negative values decrease it. Defaults to 8 dp on all sides.
	 * @param onClick Called when item is clicked while revealed. `key` is the key of this, the
	 *                clicked element. If click listener is defined here, clicks for this element
	 *                will not be handled by `onRevealableClick` of `Reveal`
	 *
	 * @see Key
	 */
	public fun Modifier.revealable(
		key: Key,
		shape: RevealShape = RevealShape.RoundRect(4.dp),
		padding: PaddingValues = PaddingValues(8.dp),
		onClick: OnClickListener? = null,
	): Modifier

	/**
	 * Registers the element as a revealable item.
	 *
	 * Each key in [keys] must be unique in the current scope and should be used for
	 * [RevealState.reveal]. Internally [Modifier.onGloballyPositioned] is used. Hence elements are
	 * only registered after they have been laid out.
	 *
	 * If the element that this modifier is applied to leaves the composition while the reveal
	 * effect is shown for the element, the effect is finished.
	 *
	 * @param keys    Unique keys to identify the revealable content. Also see documentation of [Key].
	 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
	 *                with a corner size of 4 dp.
	 * @param padding Additional padding around the reveal area. Positive values increase area while
	 *                negative values decrease it. Defaults to 8 dp on all sides.
	 * @param onClick Called when item is clicked while revealed. `key` is the key of this, the
	 *                clicked element. If click listener is defined here, clicks for this element
	 *                will not be handled by `onRevealableClick` of `Reveal`
	 *
	 * @see Key
	 */
	public fun Modifier.revealable(
		vararg keys: Key,
		shape: RevealShape = RevealShape.RoundRect(4.dp),
		padding: PaddingValues = PaddingValues(8.dp),
		onClick: OnClickListener? = null,
	): Modifier

	/**
	 * Registers the element as a revealable item.
	 *
	 * Each key specified in [keys] must be unique in the current scope and should be used for
	 * [RevealState.reveal]. Internally [Modifier.onGloballyPositioned] is used. Hence elements are
	 * only registered after they have been laid out.
	 *
	 * If the element that this modifier is applied to leaves the composition while the reveal
	 * effect is shown for the element, the effect is finished.
	 *
	 * @param keys    Unique keys to identify the revealable content. Also see documentation of [Key].
	 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
	 *                with a corner size of 4 dp.
	 * @param padding Additional padding around the reveal area. Positive values increase area while
	 *                negative values decrease it. Defaults to 8 dp on all sides.
	 * @param onClick Called when item is clicked while revealed. `key` is the key of this, the
	 *                clicked element. If click listener is defined here, clicks for this element
	 *                will not be handled by `onRevealableClick` of `Reveal`
	 *
	 * @see Key
	 */
	public fun Modifier.revealable(
		keys: Iterable<Key>,
		shape: RevealShape = RevealShape.RoundRect(4.dp),
		padding: PaddingValues = PaddingValues(8.dp),
		onClick: OnClickListener? = null,
	): Modifier
}

internal class RevealScopeInstance(private val revealState: RevealState) : RevealScope {

	override fun Modifier.revealable(
		key: Key,
		shape: RevealShape,
		padding: PaddingValues,
		onClick: OnClickListener?,
	): Modifier = this.then(
		Modifier.revealable(
			key = key,
			state = revealState,
			shape = shape,
			padding = padding,
			onClick = onClick,
		),
	)

	override fun Modifier.revealable(
		vararg keys: Key,
		shape: RevealShape,
		padding: PaddingValues,
		onClick: OnClickListener?,
	): Modifier = this.then(
		Modifier.revealable(
			keys = keys,
			state = revealState,
			shape = shape,
			padding = padding,
			onClick = onClick,
		),
	)

	override fun Modifier.revealable(
		keys: Iterable<Key>,
		shape: RevealShape,
		padding: PaddingValues,
		onClick: OnClickListener?,
	): Modifier = this.then(
		Modifier.revealable(
			keys = keys,
			state = revealState,
			shape = shape,
			padding = padding,
			onClick = onClick,
		),
	)
}
