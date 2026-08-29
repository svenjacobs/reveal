package com.svenjacobs.reveal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import com.svenjacobs.reveal.effect.RevealOverlayEffect
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import com.svenjacobs.reveal.internal.popup.RevealOverlayPopupPositionProvider
import com.svenjacobs.reveal.internal.popup.revealOverlayPopupProperties

/**
 * Container composable for the reveal effect.
 *
 * When active, applies the [overlayEffect] and only reveals the current revealable elements.
 *
 * Multiple elements can be revealed at once via [RevealState.reveal]. In that case [overlayContent]
 * is called once per revealed element, with the key of the respective element.
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
 * The overlay effect is rendered in a full screen [Popup], so [Reveal] can be placed anywhere in
 * the Compose hierarchy, including inside a `ModalBottomSheet` or `Dialog`, and the effect is
 * always drawn above the rest of that window's content. There can be many [Reveal] instances,
 * however only one should be active/visible at a time.
 *
 * @param modifier           Modifier applied to this composable.
 * @param onRevealableClick  Called when a revealable area was clicked, where the parameter `key`
 *                           is the key of the clicked revealable item. Is not called for an item
 *                           when the clicked revealable item declares `onClick` via its modifier.
 * @param onOverlayClick     Called when the overlay is clicked somewhere outside of the current
 *                           revealables, where the parameter `key` is the key of the current
 *                           revealable, or the first one if multiple items are revealed at once.
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
 * @see RevealState
 * @see RevealScope
 * @see RevealOverlayScope
 * @see DimRevealOverlayEffect
 */
@Composable
public fun Reveal(
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

    // Reveal areas are recorded relative to this composable's composition root
    // (Modifier.revealable uses positionInRoot()), while the overlay is rendered in the popup's
    // own composition root. RevealOverlayPopupPositionProvider places that popup at the window
    // origin, so the popup's local coordinates are window coordinates, and areas are mapped over
    // by adding the root's own offset within the window.
    //
    // Note this is deliberately measured within a single composition root, rather than by
    // comparing a positionOnScreen() reading taken in each root: screen conversion is not a
    // portable common reference frame. Compose's skiko backends return Offset.Unspecified (NaN)
    // from positionOnScreen() while the underlying component isn't showing, which silently
    // poisons the arithmetic and places the reveal area nowhere.
    var rootOffsetInWindow by remember { mutableStateOf(Offset.Zero) }

    val revealables = remember(density, layoutDirection) {
        derivedStateOf {
            PositionedRevealables(
                current = revealState.currentRevealables.toPositioned(
                    density = density,
                    layoutDirection = layoutDirection,
                    additionalOffset = rootOffsetInWindow,
                ),
                previous = revealState.previousRevealables.toPositioned(
                    density = density,
                    layoutDirection = layoutDirection,
                    additionalOffset = rootOffsetInWindow,
                ),
            )
        }
    }

    val revs by rememberUpdatedState(revealables.value.current)

    // Passthrough is a window level property on Android (FLAG_NOT_TOUCHABLE), so it can only be
    // honoured when every revealed item opts into it.
    val passthrough = revs.isNotEmpty() && revs.all { it.onClick is OnClick.Passthrough }

    val clickModifier = when {
        revealState.isVisible -> Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                if (!passthrough) {
                    down.consume()
                }

                val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    ?: return@awaitEachGesture

                when (val hit = revs.firstOrNull { it.area.contains(up.position) }) {
                    // Clicks outside of all reveal areas are reported with the key of the first
                    // revealed item.
                    null -> revs.firstOrNull()?.key?.let(onOverlayClick)

                    else -> when (val onClick = hit.onClick) {
                        // pass through touches in the area on top of revealable
                        is OnClick.Passthrough -> return@awaitEachGesture

                        is OnClick.Listener -> onClick.listener(hit.key)

                        null -> onRevealableClick(hit.key)
                    }
                }

                up.consume()
            }
        }

        else -> Modifier
    }

    Box(
        modifier = modifier.onGloballyPositioned {
            rootOffsetInWindow = it.positionInWindow() - it.positionInRoot()
        },
    ) {
        content(RevealScopeInstance(revealState))
    }

    if (animatedOverlayAlpha > 0.0f) {
        Popup(
            popupPositionProvider = RevealOverlayPopupPositionProvider,
            properties = revealOverlayPopupProperties(passthrough = passthrough),
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .then(clickModifier)
                        .semantics { testTag = "overlay" },
                )

                overlayEffect.Overlay(
                    revealState = revealState,
                    revealables = revealables,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(animatedOverlayAlpha),
                    content = overlayContent,
                )
            }
        }
    }
}

public typealias OnClickListener = (key: Key) -> Unit

private fun List<Revealable>.toPositioned(
    density: Density,
    layoutDirection: LayoutDirection,
    additionalOffset: Offset,
): List<PositionedRevealable> = map {
    PositionedRevealable(
        key = it.key,
        shape = it.shape,
        padding = it.padding,
        borderStroke = it.borderStroke,
        area = it.computeArea(
            density = density,
            layoutDirection = layoutDirection,
            additionalOffset = additionalOffset,
        ),
        onClick = it.onClick,
    )
}
