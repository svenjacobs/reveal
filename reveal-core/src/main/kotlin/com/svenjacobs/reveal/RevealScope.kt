package com.svenjacobs.reveal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
	 * @param padding Additional padding around the reveal area. Positive values increase area while
	 *                negative values decrease it. Defaults to 8 dp on all sides.
	 * @param shape   Shape of the reveal effect around the element. Defaults to a rounded rect
	 *                with a corner size of 4 dp.
	 *
	 * @see Key
	 */
	public fun Modifier.revealable(
		key: Key,
		padding: PaddingValues = PaddingValues(8.dp),
		shape: RevealShape = RevealShape.RoundRect(4.dp),
	): Modifier
}

internal class RevealScopeInstance(
	private val revealState: RevealState,
) : RevealScope {

	override fun Modifier.revealable(key: Key, padding: PaddingValues, shape: RevealShape): Modifier =
		this.then(
			Modifier
				.onGloballyPositioned { layoutCoordinates ->
					revealState.putRevealable(
						key = key,
						shape = shape,
						padding = padding,
						layoutCoordinates = layoutCoordinates,
					)
				}
				.composed {
					DisposableEffect(Unit) {
						onDispose {
							revealState.removeRevealable(key)
						}
					}
					this
				},
		)
}
