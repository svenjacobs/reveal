package com.svenjacobs.reveal.internal.popup

import androidx.compose.ui.window.PopupProperties

/**
 * [passthrough] is intentionally ignored: skiko based popups always occupy their own input layer
 * and cannot forward touches/clicks to layers below.
 */
internal actual fun revealOverlayPopupProperties(passthrough: Boolean): PopupProperties =
    PopupProperties(
        focusable = false,
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        clippingEnabled = false,
        usePlatformInsets = false,
    )
