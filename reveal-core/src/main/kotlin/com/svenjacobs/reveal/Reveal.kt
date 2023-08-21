package com.svenjacobs.reveal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.svenjacobs.reveal.common.inserter.RevealOverlayInserter
import com.svenjacobs.reveal.effect.RevealOverlayEffect
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect

/**
 * Container composable for the reveal effect.
 *
 * When active, applies the [overlayEffect] and only reveals current revealable element.
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
 * This composable requires a higher level [RevealCanvas] and its associated [RevealCanvasState].
 * While there should be only one [RevealCanvas] per application at a top position in the Compose
 * hierarchy, there can be many [Reveal] instances. The recommendation is one instance per "screen".
 * However only one [Reveal] should be active/visible at a time.
 *
 * @param revealCanvasState  State of higher level [RevealCanvas]
 * @param modifier           Modifier applied to this composable.
 * @param onRevealableClick  Called when the revealable area was clicked, where the parameter `key`
 *                           is the key of the current revealable item. Is not called for an item
 *                           when the clicked revealable item declares `onClick` via its modifier.
 * @param onOverlayClick     Called when the overlay is clicked somewhere outside of the current
 *                           revealable, where the parameter `key` is the key of the current
 *                           revealable.
 * @param revealState        State which controls the visibility of the reveal effect.
 * @param overlayEffect      The effect which is used for the background and reveal of items.
 *                           Currently only [DimRevealOverlayEffect] is supported.
 * @param overlayContent     Optional content which is placed above the overlay and where its
 *                           elements can be aligned relative to the reveal area via modifiers
 *                           available in the scope of this composable. The `key` parameter is the
 *                           key of the current visible revealable item.
 * @param content            Actual content which is visible when the Reveal composable is not
 *                           active. Elements are registered as revealables via modifiers provided
 *                           in the scope of this composable.
 *
 * @see RevealCanvas
 * @see RevealState
 * @see RevealScope
 * @see RevealOverlayScope
 * @see DimRevealOverlayEffect
 * @see RevealOverlayInserter
 */
@Composable
public fun Reveal(
	revealCanvasState: RevealCanvasState,
	modifier: Modifier = Modifier,
	onRevealableClick: OnClickListener = {},
	onOverlayClick: OnClickListener = {},
	revealState: RevealState = rememberRevealState(),
	overlayEffect: RevealOverlayEffect = DimRevealOverlayEffect(),
	overlayContent: @Composable (RevealOverlayScope.(key: Key) -> Unit) = {},
	content: @Composable (RevealScope.() -> Unit),
) {
	val animatedOverlayAlpha by animateFloatAsState(
		targetValue = if (revealState.isVisible) 1.0f else 0.0f,
		animationSpec = overlayEffect.alphaAnimationSpec,
		finishedListener = { alpha ->
			if (alpha == 0.0f) {
				revealState.onHideAnimationFinished()
			}
		},
		label = "animatedOverlayAlpha",
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
					additionalOffset = revealCanvasState.revealableOffset,
				)
			}
		}

		val previousRevealable = remember {
			derivedStateOf {
				revealState.previousRevealable?.toActual(
					density = density,
					layoutDirection = layoutDirection,
					additionalOffset = revealCanvasState.revealableOffset,
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
								rev?.onClick ?: onRevealableClick
							} else {
								onOverlayClick
							},
						)
					},
				)
			}

			else -> Modifier
		}

		LaunchedEffect(animatedOverlayAlpha) {
			@Suppress("ktlint:standard:wrapping")
			revealCanvasState.overlayContent = when {
				animatedOverlayAlpha > 0.0f -> ({
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
				})

				else -> null
			}
		}
	}
}

public typealias OnClickListener = (key: Key) -> Unit

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
	onClick = onClick,
)
