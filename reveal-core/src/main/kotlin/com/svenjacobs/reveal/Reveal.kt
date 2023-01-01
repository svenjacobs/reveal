package com.svenjacobs.reveal

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.svenjacobs.reveal.internal.rect.toIntRect

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
 * @param onRevealableClick Called when the revealable area was clicked, where the parameter `key`
 *                          is the key of the current revealable item.
 * @param onOverlayClick    Called when the overlay is clicked somewhere outside of the current
 *                          revealable, where the parameter `key` is the key of the current
 *                          revealable.
 * @param modifier          Modifier applied to this composable.
 * @param revealState       State which controls the visibility of the reveal effect.
 * @param overlayEffect     The effect which is used for the background and reveal of items.
 *                          Currently only [DimRevealOverlayEffect] is supported.
 * @param overlayContent    Optional content which is placed above the overlay and where its
 *                          elements can be aligned relative to the reveal area via modifiers
 *                          available in the scope of this composable. The `key` parameter is the
 *                          key of the current visible revealable item.
 * @param content           Actual content which is visible when the Reveal composable is not
 *                          active. Elements are registered as revealables via modifiers provided
 *                          in the scope of this composable.
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
	overlayContent: @Composable RevealOverlayScope.(key: Key) -> Unit = {},
	content: @Composable RevealScope.() -> Unit,
) {
	var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
	val positionInRoot by remember {
		derivedStateOf { layoutCoordinates?.positionInRoot() ?: Offset.Zero }
	}
	val layoutDirection = LocalLayoutDirection.current
	val density = LocalDensity.current

	Box(
		modifier = modifier.onGloballyPositioned { layoutCoordinates = it },
	) {
		content(RevealScopeInstance(revealState))

		val revealable = remember {
			derivedStateOf {
				revealState.currentRevealable?.let {
					CurrentRevealable(
						key = it.key,
						shape = it.shape,
						padding = it.padding,
						revealArea = it.getRevealArea(
							containerPositionInRoot = positionInRoot,
							density = density,
							layoutDirection = layoutDirection,
						),
					)
				}
			}
		}

		val clickModifier = revealable.value.let { rev ->
			when {
				revealState.isVisible && rev != null -> Modifier.pointerInput(Unit) {
					detectTapGestures(
						onPress = { offset ->
							rev.key.let(
								if (rev.revealArea.contains(offset)) {
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
		}

		overlayEffect.Overlay(
			revealState = revealState,
			revealable = revealable,
			modifier = clickModifier.matchParentSize(),
		) {
			revealable.value?.let {
				RevealOverlayScopeInstance(
					revealableRect = it.revealArea.toIntRect(),
				).overlayContent(it.key)
			}
		}
	}
}
