package com.svenjacobs.reveal.android.tests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.OnClickListener
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

/**
 * Tests for revealing multiple elements at once (issue #105): both reveal areas are active at the
 * same time, and a click is attributed to the item which was actually hit rather than to whichever
 * item was revealed first.
 *
 * Clicks are injected at a position within the overlay's own full screen catcher node, because that
 * is the node which receives touches while the effect is visible. The two revealables sit in
 * opposite corners and are inflated by a generous `padding`, so that a click near a corner is
 * inside exactly one reveal area and a click at the center is inside neither, regardless of screen
 * size, density and system bar insets.
 */
class RevealMultipleRevealablesTest {

    private enum class Keys { First, Second }

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun clickOnFirstRevealableReportsItsKey() {
        var revealableClickKey: Key? = null

        reveal(onRevealableClick = { revealableClickKey = it })
        clickOnOverlay(x = 0.05f, y = 0.05f)

        assertEquals(Keys.First, revealableClickKey)
    }

    @Test
    fun clickOnSecondRevealableReportsItsKey() {
        var revealableClickKey: Key? = null

        reveal(onRevealableClick = { revealableClickKey = it })
        clickOnOverlay(x = 0.95f, y = 0.95f)

        assertEquals(Keys.Second, revealableClickKey)
    }

    @Test
    fun clickOutsideOfAllRevealablesCallsOnOverlayClick() {
        var overlayClickKey: Key? = null
        var revealableClickKey: Key? = null

        reveal(
            onRevealableClick = { revealableClickKey = it },
            onOverlayClick = { overlayClickKey = it },
        )
        clickOnOverlay(x = 0.5f, y = 0.5f)

        // Clicks outside of all reveal areas are reported with the key of the first revealed item.
        assertEquals(Keys.First, overlayClickKey)
        assertNull(revealableClickKey)
    }

    private fun clickOnOverlay(x: Float, y: Float) {
        composeTestRule.onNodeWithTag(OVERLAY_TAG).performTouchInput {
            click(Offset(width * x, height * y))
        }
        composeTestRule.waitForIdle()
    }

    private fun reveal(
        onRevealableClick: OnClickListener = {},
        onOverlayClick: OnClickListener = {},
    ) {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            revealState = rememberRevealState()

            Reveal(
                modifier = Modifier.fillMaxSize(),
                onRevealableClick = onRevealableClick,
                onOverlayClick = onOverlayClick,
                revealState = revealState,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    for ((key, alignment) in ALIGNMENTS) {
                        Box(
                            modifier = Modifier
                                .align(alignment)
                                .size(40.dp)
                                .revealable(key = key, padding = PaddingValues(60.dp)),
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        scope.launch { revealState.reveal(Keys.First, Keys.Second) }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val OVERLAY_TAG = "overlay"

        val ALIGNMENTS = mapOf<Keys, Alignment>(
            Keys.First to Alignment.TopStart,
            Keys.Second to Alignment.BottomEnd,
        )
    }
}
