package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

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
 * @param state   Item is associated with this [RevealState]
 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
 *                with a corner size of 4 dp.
 * @param padding Additional padding around the reveal area. Positive values increase area while
 *                negative values decrease it. Defaults to 8 dp on all sides.
 * @param onClick Called when item is clicked while revealed. `key` is the key of this, the clicked
 *                element. If click listener is defined here, clicks for this element will not be
 *                handled by `onRevealableClick` of `Reveal`
 *
 * @see Key
 */
public fun Modifier.revealable(
	key: Key,
	state: RevealState,
	shape: RevealShape = RevealShape.RoundRect(4.dp),
	padding: PaddingValues = PaddingValues(8.dp),
	onClick: OnClickListener? = null,
): Modifier = this.then(
	Modifier.revealable(
		state = state,
		keys = listOf(key),
		shape = shape,
		padding = padding,
		onClick = onClick,
	),
)

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
 * @param state   Item is associated with this [RevealState]
 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
 *                with a corner size of 4 dp.
 * @param padding Additional padding around the reveal area. Positive values increase area while
 *                negative values decrease it. Defaults to 8 dp on all sides.
 * @param onClick Called when item is clicked while revealed. `key` is the key of this, the clicked
 *                element. If click listener is defined here, clicks for this element will not be
 *                handled by `onRevealableClick` of `Reveal`
 *
 * @see Key
 */
public fun Modifier.revealable(
	vararg keys: Key,
	state: RevealState,
	shape: RevealShape = RevealShape.RoundRect(4.dp),
	padding: PaddingValues = PaddingValues(8.dp),
	onClick: OnClickListener? = null,
): Modifier = this.then(
	Modifier.revealable(
		state = state,
		keys = keys.toList(),
		shape = shape,
		padding = padding,
		onClick = onClick,
	),
)

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
 * @param state   Item is associated with this [RevealState]
 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
 *                with a corner size of 4 dp.
 * @param padding Additional padding around the reveal area. Positive values increase area while
 *                negative values decrease it. Defaults to 8 dp on all sides.
 * @param onClick Called when item is clicked while revealed. `key` is the key of this, the clicked
 *                element. If click listener is defined here, clicks for this element will not be
 *                handled by `onRevealableClick` of `Reveal`
 *
 * @see Key
 */
public fun Modifier.revealable(
	keys: Iterable<Key>,
	state: RevealState,
	shape: RevealShape = RevealShape.RoundRect(4.dp),
	padding: PaddingValues = PaddingValues(8.dp),
	onClick: OnClickListener? = null,
): Modifier = this.then(
	Modifier
		.onGloballyPositioned { layoutCoordinates ->
			for (key in keys) {
				state.putRevealable(
					Revealable(
						key = key,
						shape = shape,
						padding = padding,
						layout = Revealable.Layout(
							offset = layoutCoordinates.positionInRoot(),
							size = layoutCoordinates.size.toSize(),
						),
						onClick = onClick,
					),
				)
			}
		}
		.composed {
			DisposableEffect(Unit) {
				onDispose {
					for (key in keys) {
						state.removeRevealable(key)
					}
				}
			}
			this
		},
)
