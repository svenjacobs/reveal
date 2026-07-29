package com.svenjacobs.reveal.internal.popup

import android.view.WindowManager
import kotlin.test.Test
import kotlin.test.assertEquals

class RevealOverlayPopupFlagsTest {

    @Test
    fun nonPassthroughDoesNotSetNotTouchable() {
        val flags = computeRevealOverlayPopupFlags(passthrough = false)

        assertEquals(
            0,
            flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            "Overlay popup must intercept touches when the current revealable is not passthrough",
        )
    }

    @Test
    fun passthroughSetsNotTouchable() {
        val flags = computeRevealOverlayPopupFlags(passthrough = true)

        assertEquals(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            "Overlay popup must let touches fall through when the current revealable uses " +
                "OnClick.Passthrough",
        )
    }

    @Test
    fun alwaysNotFocusableAndUnclipped() {
        val flags = computeRevealOverlayPopupFlags(passthrough = false)

        assertEquals(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        )
        assertEquals(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )
    }
}
