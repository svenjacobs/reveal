package com.svenjacobs.reveal

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import com.svenjacobs.reveal.internal.rect.toIntRect
import com.svenjacobs.reveal.internal.revealable.getRevealArea

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
 * @param onRevealableClick           Called when the revealable area was clicked, where the
 *                                    parameter `key` is the key of the current revealable item.
 * @param onOverlayClick              Called when the overlay (greyed out area) is clicked somewhere
 *                                    outside of the current revealable, where the parameter `key`
 *                                    is the key of the current revealable.
 * @param modifier                    Modifier applied to this composable.
 * @param revealState                 State which controls the visibility of the reveal effect.
 * @param overlayColor                Animated background color of the overlay.
 *                                    See [overlayColorAnimationSpec].
 * @param overlayColorAnimationSpec   Animation spec for the overlay background color.
 * @param overlayContentAnimationSpec Animation spec for the animated alpha value of the overlay
 *                                    content.
 * @param overlayContent              Optional content which is placed above the greyed out backdrop
 *                                    and where its elements can be aligned relative to the reveal
 *                                    area via modifiers available in the scope of this composable.
 *                                    The `key` parameter is the key of the current visible
 *                                    revealable item.
 * @param content                     Actual content which is visible when the Reveal composable is
 *                                    not active. Elements are registered as revealables via
 *                                    modifiers provided in the scope of this composable.
 *
 * @see RevealState
 * @see RevealScope
 * @see RevealOverlayScope
 */
@Composable
public fun Reveal(
	onRevealableClick: (key: Key) -> Unit,
	onOverlayClick: (key: Key) -> Unit,
	modifier: Modifier = Modifier,
	revealState: RevealState = rememberRevealState(),
	overlayColor: Color = Color.Black.copy(alpha = 0.8f),
	overlayColorAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 500),
	overlayContentAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 500),
	overlayContent: @Composable RevealOverlayScope.(key: Key) -> Unit = {},
	content: @Composable RevealScope.() -> Unit,
) {
	val animatedOverlayColor by animateColorAsState(
		targetValue = if (revealState.isVisible) overlayColor else Color.Transparent,
		animationSpec = overlayColorAnimationSpec,
	)
	val animatedOverlayContentAlpha by animateFloatAsState(
		targetValue = if (revealState.isVisible) 1.0f else 0.0f,
		animationSpec = overlayContentAnimationSpec,
		finishedListener = { alpha ->
			if (alpha == 0.0f) {
				revealState.onHideAnimationFinished()
			}
		},
	)
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

		revealState.currentRevealable?.let { revealable ->
			val revealableRect by remember(revealable) {
				derivedStateOf {
					revealable.getRevealArea(
						containerPositionInRoot = positionInRoot,
						density = density,
						layoutDirection = layoutDirection,
					)
				}
			}

			val clickModifier = when (revealState.isVisible) {
				true -> Modifier.pointerInput(Unit) {
					detectTapGestures(
						onPress = { offset ->
							revealable.key.let(
								if (revealableRect.contains(offset)) {
									onRevealableClick
								} else {
									onOverlayClick
								},
							)
						},
					)
				}
				false -> Modifier
			}

			Box(
				modifier = clickModifier
					.matchParentSize()
					.drawBehind {
						val path = revealable.shape.clip(
							revealableRect = revealableRect,
							density = density,
						)

						clipPath(path, clipOp = ClipOp.Difference) {
							drawRect(animatedOverlayColor)
						}
					},
			) {
				// Optimization: don't place element into composition if it isn't visible at all
				if (animatedOverlayContentAlpha > 0f) {
					Box(
						modifier = Modifier
							.matchParentSize()
							.alpha(animatedOverlayContentAlpha),
						content = {
							RevealOverlayScopeInstance(
								revealableRect = revealableRect.toIntRect(),
							).overlayContent(
								revealable.key,
							)
						},
					)
				}
			}
		}
	}
}
