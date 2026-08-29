package com.svenjacobs.reveal.internal.popup

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * Always positions the overlay popup at the origin of the window, so that it covers the full
 * screen regardless of where [com.svenjacobs.reveal.Reveal] is composed.
 */
internal object RevealOverlayPopupPositionProvider : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}

/**
 * Platform specific properties for the full screen overlay popup.
 *
 * @param passthrough Whether all current revealables use [com.svenjacobs.reveal.OnClick.Passthrough].
 *                     If supported by the platform, the popup should not intercept touches in this
 *                     case. Since this is a property of the whole popup window, it cannot be
 *                     honoured for individual items when multiple items are revealed at once.
 */
internal expect fun revealOverlayPopupProperties(passthrough: Boolean): PopupProperties
