package com.svenjacobs.reveal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

public sealed interface OnClick {
	/**
	 * Clicks on Revealable are handled by the specified handler.
	 */
	@Immutable
	public data class Listener(val listener: OnClickListener) : OnClick

	/**
	 * Clicks on Revealable are not handled by Reveal and passed through to underlying
	 * composables.
	 */
	public data object Passthrough : OnClick
}

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
 * @param key          Unique key to identify the revealable content. Also see documentation of [Key].
 * @param state        Item is associated with this [RevealState].
 * @param shape        Shape of the reveal effect around the element. Defaults to a rounded rect
 *                     with a corner size of 4 dp.
 * @param padding      Additional padding around the reveal area. Positive values increase area
 *                     while negative values decrease it. Defaults to 8 dp on all sides.
 * @param borderStroke Optional border around the revealable item.
 * @param onClick      If `null` clicks will be handled by `onRevealableClick` of `Reveal`.
 *                     If set to `OnClick.Handler` clicks will be handled by this listener.
 *                     If set to `OnClick.Passthrough` Reveal will not intercept clicks and clicks
 *                     will be passed through to underlying composables.
 *
 * @see Key
 */
public fun Modifier.revealable(
	key: Key,
	state: RevealState,
	shape: RevealShape = RevealShape.RoundRect(4.dp),
	padding: PaddingValues = PaddingValues(8.dp),
	borderStroke: BorderStroke? = null,
	onClick: OnClick? = null,
): Modifier = this.then(
	Modifier.revealable(
		state = state,
		keys = listOf(key),
		shape = shape,
		padding = padding,
		borderStroke = borderStroke,
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
 * @param keys         Unique keys to identify the revealable content. Also see documentation of [Key].
 * @param state        Item is associated with this [RevealState].
 * @param shape        Shape of the reveal effect around the element. Defaults to a rounded rect
 *                     with a corner size of 4 dp.
 * @param padding      Additional padding around the reveal area. Positive values increase area
 *                     while negative values decrease it. Defaults to 8 dp on all sides.
 * @param borderStroke Optional border around the revealable item.
 * @param onClick      If `null` clicks will be handled by `onRevealableClick` of `Reveal`.
 *                     If set to `OnClick.Handler` clicks will be handled by this listener.
 *                     If set to `OnClick.Passthrough` Reveal will not intercept clicks and clicks
 *                     will be passed through to underlying composables.
 *
 * @see Key
 */
public fun Modifier.revealable(
	vararg keys: Key,
	state: RevealState,
	shape: RevealShape = RevealShape.RoundRect(4.dp),
	padding: PaddingValues = PaddingValues(8.dp),
	borderStroke: BorderStroke? = null,
	onClick: OnClick? = null,
): Modifier = this.then(
	Modifier.revealable(
		state = state,
		keys = keys.toList(),
		shape = shape,
		padding = padding,
		borderStroke = borderStroke,
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
 * @param keys         Unique keys to identify the revealable content. Also see documentation of [Key].
 * @param state        Item is associated with this [RevealState].
 * @param shape        Shape of the reveal effect around the element. Defaults to a rounded rect
 *                     with a corner size of 4 dp.
 * @param padding      Additional padding around the reveal area. Positive values increase area
 *                     while negative values decrease it. Defaults to 8 dp on all sides.
 * @param borderStroke Optional border around the revealable item.
 * @param onClick      If `null` clicks will be handled by `onRevealableClick` of `Reveal`.
 *                     If set to `OnClick.Handler` clicks will be handled by this listener.
 *                     If set to `OnClick.Passthrough` Reveal will not intercept clicks and clicks
 *                     will be passed through to underlying composables.
 *
 * @see Key
 */
public fun Modifier.revealable(
	keys: Iterable<Key>,
	state: RevealState,
	shape: RevealShape = RevealShape.RoundRect(4.dp),
	padding: PaddingValues = PaddingValues(8.dp),
	borderStroke: BorderStroke? = null,
	onClick: OnClick? = null,
): Modifier = this.then(
	Modifier
		.onGloballyPositioned { layoutCoordinates ->
			keys.forEach { key ->
				state.addRevealable(
					Revealable(
						key = key,
						shape = shape,
						padding = padding,
						borderStroke = borderStroke,
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
					keys.forEach(state::removeRevealable)
				}
			}
			this
		},
)
