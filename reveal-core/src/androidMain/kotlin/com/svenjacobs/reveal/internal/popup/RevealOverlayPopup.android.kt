package com.svenjacobs.reveal.internal.popup

import android.view.WindowManager
import androidx.compose.ui.window.PopupProperties

/**
 * Extracted from [revealOverlayPopupProperties] so the flag computation can be unit tested without
 * needing to construct a real [PopupProperties] (its `flags` are internal to the `ui` module and
 * therefore not readable from here).
 */
internal fun computeRevealOverlayPopupFlags(passthrough: Boolean): Int =
    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
        (if (passthrough) WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE else 0)

internal actual fun revealOverlayPopupProperties(passthrough: Boolean): PopupProperties =
    PopupProperties(
        flags = computeRevealOverlayPopupFlags(passthrough),
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
    )
