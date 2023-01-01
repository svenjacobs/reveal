package com.svenjacobs.reveal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity

/**
 * Overlay effect which renders the background and reveal effect.
 */
public interface RevealOverlayEffect {

	@Composable
	public fun Overlay(
		revealState: RevealState,
		revealable: State<CurrentRevealable?>,
		modifier: Modifier,
		content: @Composable () -> Unit,
	)
}

/**
 * An overlay effect which dims the background as specified via [color] and [colorAnimationSpec].
 *
 * @param color                Animated background color of the overlay.
 * @param colorAnimationSpec   Animation spec for the overlay background color.
 * @param contentAnimationSpec Animation spec for the animated alpha value of the overlay content.
 */
@Immutable
public class DimRevealOverlayEffect(
	private val color: Color = Color.Black.copy(alpha = 0.8f),
	private val colorAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 500),
	private val contentAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 500),
) : RevealOverlayEffect {

	@Composable
	override fun Overlay(
		revealState: RevealState,
		revealable: State<CurrentRevealable?>,
		modifier: Modifier,
		content: @Composable () -> Unit,
	) {
		val animatedColor by animateColorAsState(
			targetValue = if (revealState.isVisible) color else Color.Transparent,
			animationSpec = colorAnimationSpec,
		)
		val animatedContentAlpha by animateFloatAsState(
			targetValue = if (revealState.isVisible) 1.0f else 0.0f,
			animationSpec = contentAnimationSpec,
			finishedListener = { alpha ->
				if (alpha == 0.0f) {
					revealState.onHideAnimationFinished()
				}
			},
		)

		revealable.value?.let { rev ->
			val density = LocalDensity.current

			Box(
				modifier = modifier.drawBehind {
					val path = rev.shape
						.clip(
							size = Size(
								width = rev.revealArea.width,
								height = rev.revealArea.height,
							),
							density = density,
							layoutDirection = layoutDirection,
						)
						.apply {
							translate(
								Offset(
									x = rev.revealArea.left,
									y = rev.revealArea.top,
								),
							)
						}

					clipPath(path, clipOp = ClipOp.Difference) {
						drawRect(animatedColor)
					}
				},
			) {
				// Optimization: don't place element into composition if it isn't visible at all
				if (animatedContentAlpha > 0f) {
					Box(
						modifier = Modifier
							.matchParentSize()
							.alpha(animatedContentAlpha),
						content = { content() },
					)
				}
			}
		}
	}
}
