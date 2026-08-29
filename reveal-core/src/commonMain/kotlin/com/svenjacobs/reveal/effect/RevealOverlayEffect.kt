package com.svenjacobs.reveal.effect

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.PositionedRevealable
import com.svenjacobs.reveal.PositionedRevealables
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealState

/**
 * Overlay effect which renders the background and reveal effect.
 */
public interface RevealOverlayEffect {

    /**
     * Renders the overlay for all currently revealed items, as well as those which are fading out.
     *
     * [content] is called once per revealed item, with the key of the respective item.
     *
     * The default implementation delegates to the deprecated single revealable overload, so that
     * effects which were written before support for multiple revealables keep working. Override
     * this function instead.
     */
    @Composable
    public fun Overlay(
        revealState: RevealState,
        revealables: State<PositionedRevealables>,
        modifier: Modifier,
        content: @Composable RevealOverlayScope.(key: Key) -> Unit,
    ) {
        val current = remember(revealables) {
            derivedStateOf { revealables.value.current.firstOrNull() }
        }
        val previous = remember(revealables) {
            derivedStateOf { revealables.value.previous.firstOrNull() }
        }

        @Suppress("DEPRECATION")
        Overlay(
            revealState = revealState,
            currentRevealable = current,
            previousRevealable = previous,
            modifier = modifier,
            content = content,
        )
    }

    @Deprecated(
        message = "Override the Overlay() overload which takes PositionedRevealables. This one " +
            "only ever receives the first of possibly multiple revealed items.",
    )
    @Composable
    public fun Overlay(
        revealState: RevealState,
        currentRevealable: State<PositionedRevealable?>,
        previousRevealable: State<PositionedRevealable?>,
        modifier: Modifier,
        content: @Composable RevealOverlayScope.(key: Key) -> Unit,
    ) {
    }

    /**
     * Animation spec for the animated alpha of the overlay when this effect is shown or hidden.
     */
    public val alphaAnimationSpec: AnimationSpec<Float>
}
