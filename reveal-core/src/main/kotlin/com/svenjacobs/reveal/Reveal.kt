package com.svenjacobs.reveal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import com.svenjacobs.reveal.effect.RevealOverlayEffect
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import com.svenjacobs.reveal.internal.fullscreen.Fullscreen

/**
 * Container composable for the reveal effect.
 *
 * When active, applies the [overlayEffect] and only reveals current revealable element.
 *
 * Adds a new `ComposeView` to the root content view (`android.R.id.content`) for the fullscreen
 * effect. Therefore it does not matter where in the component hierarchy this composable is added.
 * The effect is always rendered fullscreen.
 *
 * Elements inside the contents of this composable are registered as "revealables" via the
 * [RevealScope.revealable] modifier in the scope of the [content] composable.
 *
 * The effect is controlled via [RevealState.reveal] and [RevealState.hide].
 *
 * Optionally an [overlayContent] can be specified to place explanatory elements (like texts or
 * images) next to the reveal area. This content is placed above the greyed out backdrop. Elements
 * in this scope can be aligned relative to the reveal area via [RevealOverlayScope.align].
 *
 * @param onRevealableClick          Called when the revealable area was clicked, where the
 *                                   parameter `key` is the key of the current revealable item.
 * @param onOverlayClick             Called when the overlay is clicked somewhere outside of the
 *                                   current revealable, where the parameter `key` is the key of the
 *                                   current revealable.
 * @param modifier                   Modifier applied to this composable.
 * @param revealState                State which controls the visibility of the reveal effect.
 * @param overlayEffect              The effect which is used for the background and reveal of
 *                                   items. Currently only [DimRevealOverlayEffect] is supported.
 * @param overlayEffectAnimationSpec Animation spec for the animated alpha value of the overlay
 *                                   effect when showing or hiding.
 * @param revealableOffset           Additional offset which is applied to all revealables of this
 *                                   Reveal instance. Should be used to correct misplaced reveal
 *                                   effects where the root composables and root content view do
 *                                   not match, e.g. in applications that use `ComposeView` in
 *                                   legacy Android views. Use negative values to offset towards
 *                                   [0,0] of the coordinate system.
 * @param overlayContent             Optional content which is placed above the overlay and where
 *                                   its elements can be aligned relative to the reveal area via
 *                                   modifiers available in the scope of this composable. The `key`
 *                                   parameter is the key of the current visible revealable item.
 * @param content                    Actual content which is visible when the Reveal composable is
 *                                   not active. Elements are registered as revealables via
 *                                   modifiers provided in the scope of this composable.
 *
 * @see RevealState
 * @see RevealScope
 * @see RevealOverlayScope
 * @see DimRevealOverlayEffect
 */
@Composable
public fun Reveal(
	onRevealableClick: (key: Key) -> Unit,
	onOverlayClick: (key: Key) -> Unit,
	modifier: Modifier = Modifier,
	revealState: RevealState = rememberRevealState(),
	overlayEffect: RevealOverlayEffect = DimRevealOverlayEffect(),
	overlayEffectAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 500),
	revealableOffset: DpOffset = DpOffset.Zero,
	overlayContent: @Composable RevealOverlayScope.(key: Key) -> Unit = {},
	content: @Composable RevealScope.() -> Unit,
) {
	val animatedOverlayAlpha by animateFloatAsState(
		targetValue = if (revealState.isVisible) 1.0f else 0.0f,
		animationSpec = overlayEffectAnimationSpec,
		finishedListener = { alpha ->
			if (alpha == 0.0f) {
				revealState.onHideAnimationFinished()
			}
		},
	)
	val layoutDirection = LocalLayoutDirection.current
	val density = LocalDensity.current

	Box(
		modifier = modifier,
	) {
		content(RevealScopeInstance(revealState))

		val currentRevealable = remember {
			derivedStateOf {
				revealState.currentRevealable?.toActual(
					density = density,
					layoutDirection = layoutDirection,
					additionalOffset = revealableOffset,
				)
			}
		}

		val previousRevealable = remember {
			derivedStateOf {
				revealState.previousRevealable?.toActual(
					density = density,
					layoutDirection = layoutDirection,
					additionalOffset = revealableOffset,
				)
			}
		}

		val rev by rememberUpdatedState(currentRevealable.value)

		val clickModifier = when {
			revealState.isVisible && rev != null -> Modifier.pointerInput(Unit) {
				detectTapGestures(
					onPress = { offset ->
						rev?.key?.let(
							if (rev?.area?.contains(offset) == true) {
								onRevealableClick
							} else {
								onOverlayClick
							},
						)
					},
				)
			}

			else -> Modifier
		}

		if (animatedOverlayAlpha > 0.0f) {
			Fullscreen {
				overlayEffect.Overlay(
					revealState = revealState,
					currentRevealable = currentRevealable,
					previousRevealable = previousRevealable,
					modifier = clickModifier
						.semantics { testTag = "overlay" }
						.fillMaxSize()
						.alpha(animatedOverlayAlpha),
					content = overlayContent,
				)
			}
		}
	}
}

private fun Revealable.toActual(
	density: Density,
	layoutDirection: LayoutDirection,
	additionalOffset: DpOffset,
): ActualRevealable = ActualRevealable(
	key = key,
	shape = shape,
	padding = padding,
	area = computeArea(
		density = density,
		layoutDirection = layoutDirection,
		additionalOffset = additionalOffset,
	),
)
