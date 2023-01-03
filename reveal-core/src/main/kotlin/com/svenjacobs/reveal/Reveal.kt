package com.svenjacobs.reveal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.svenjacobs.reveal.effect.RevealOverlayEffect
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect

/**
 * Container composable for the reveal effect.
 *
 * When active, greys out its contents and only reveals current revealable element.
 * Since the grey out effect should probably cover the whole screen, this composable should be one
 * of the top most composables in the hierarchy, filling out all available space (`Modifier.fillMaxSize()`).
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
	overlayContent: @Composable RevealOverlayScope.(key: Key) -> Unit = {},
	content: @Composable RevealScope.() -> Unit,
) {
	var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
	val positionInRoot by remember {
		derivedStateOf { layoutCoordinates?.positionInRoot() ?: Offset.Zero }
	}
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
		modifier = modifier.onGloballyPositioned { layoutCoordinates = it },
	) {
		content(RevealScopeInstance(revealState))

		val currentRevealable = remember {
			derivedStateOf {
				revealState.currentRevealable?.toActual(
					containerPositionInRoot = positionInRoot,
					density = density,
					layoutDirection = layoutDirection,
				)
			}
		}

		val previousRevealable = remember {
			derivedStateOf {
				revealState.previousRevealable?.toActual(
					containerPositionInRoot = positionInRoot,
					density = density,
					layoutDirection = layoutDirection,
				)
			}
		}

		val rev by rememberUpdatedState(currentRevealable.value)

		val clickModifier = when {
			revealState.isVisible && rev != null -> Modifier.pointerInput(Unit) {
				detectTapGestures(
					onPress = { offset ->
						rev?.key?.let(
							if (rev?.revealArea?.contains(offset) == true) {
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

		overlayEffect.Overlay(
			revealState = revealState,
			currentRevealable = currentRevealable,
			previousRevealable = previousRevealable,
			modifier = clickModifier
				.matchParentSize()
				.alpha(animatedOverlayAlpha),
			content = overlayContent,
		)
	}
}

private fun InternalRevealable.toActual(
	containerPositionInRoot: Offset,
	density: Density,
	layoutDirection: LayoutDirection,
): ActualRevealable = ActualRevealable(
	key = key,
	shape = shape,
	padding = padding,
	revealArea = getRevealArea(
		containerPositionInRoot = containerPositionInRoot,
		density = density,
		layoutDirection = layoutDirection,
	),
)
