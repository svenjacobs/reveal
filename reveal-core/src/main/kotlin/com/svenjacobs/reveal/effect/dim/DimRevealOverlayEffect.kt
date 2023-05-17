package com.svenjacobs.reveal.effect.dim

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.svenjacobs.reveal.ActualRevealable
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealOverlayScopeInstance
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.effect.RevealOverlayEffect
import com.svenjacobs.reveal.effect.dim.DimItemState.Gone
import com.svenjacobs.reveal.effect.dim.DimItemState.Visible
import com.svenjacobs.reveal.effect.internal.createShapePath
import com.svenjacobs.reveal.internal.rect.toIntRect

/**
 * An overlay effect which dims the background as specified via [color].
 *
 * @param color                Background color of the overlay.
 * @param contentAnimationSpec Animation spec for the animated alpha value of the overlay content.
 */
@Immutable
public class DimRevealOverlayEffect(
	private val color: Color = Color.Black.copy(alpha = 0.8f),
	private val contentAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 500),
) : RevealOverlayEffect {

	@Composable
	override fun Overlay(
		revealState: RevealState,
		currentRevealable: State<ActualRevealable?>,
		previousRevealable: State<ActualRevealable?>,
		modifier: Modifier,
		content: @Composable RevealOverlayScope.(key: Key) -> Unit,
	) {
		val currentItemHolder = currentRevealable.value?.let {
			rememberDimItemHolder(
				revealable = it,
				fromState = Gone,
				toState = Visible,
				contentAnimationSpec = contentAnimationSpec,
			)
		}

		val prevItemHolder = previousRevealable.value?.let {
			rememberDimItemHolder(
				revealable = it,
				fromState = Visible,
				toState = Gone,
				contentAnimationSpec = contentAnimationSpec,
			)
		}

		val density = LocalDensity.current

		Box(
			modifier = modifier
				.graphicsLayer(alpha = 0.99f)
				.drawBehind {
					drawRect(color)
					prevItemHolder?.let { with(it) { drawCutout(density) } }
					currentItemHolder?.let { with(it) { drawCutout(density) } }
				},
		) {
			prevItemHolder?.let { with(it) { Container(content = content) } }
			currentItemHolder?.let { with(it) { Container(content = content) } }
		}
	}
}

@Stable
private class DimItemHolder(
	val revealable: ActualRevealable,
	val contentAlpha: State<Float>,
) {

	@Composable
	fun BoxScope.Container(
		modifier: Modifier = Modifier,
		content: @Composable RevealOverlayScope.(key: Key) -> Unit,
	) {
		// Optimization: don't place element into composition if it isn't visible at all
		if (contentAlpha.value == 0.0f) return

		Box(
			modifier = modifier
				.matchParentSize()
				.alpha(contentAlpha.value),
			content = {
				RevealOverlayScopeInstance(
					revealableRect = revealable.area.toIntRect(),
				).content(revealable.key)
			},
		)
	}

	fun DrawScope.drawCutout(density: Density) {
		val path = revealable.createShapePath(
			density = density,
			layoutDirection = layoutDirection,
		)

		drawPath(
			path,
			Color.Black,
			alpha = contentAlpha.value,
			blendMode = BlendMode.DstOut,
		)
	}
}

private enum class DimItemState { Visible, Gone }

@Composable
private fun rememberDimItemHolder(
	revealable: ActualRevealable,
	fromState: DimItemState,
	toState: DimItemState,
	contentAnimationSpec: AnimationSpec<Float>,
): DimItemHolder = key(revealable.key) {
	val targetState = remember { mutableStateOf(fromState) }
	val contentAlpha = animateFloatAsState(
		targetValue = if (targetState.value == Visible) 1.0f else 0.0f,
		animationSpec = contentAnimationSpec,
	)

	LaunchedEffect(Unit) {
		targetState.value = toState
	}

	remember {
		DimItemHolder(
			revealable = revealable,
			contentAlpha = contentAlpha,
		)
	}
}
