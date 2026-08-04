package com.svenjacobs.reveal.android.tests

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import com.svenjacobs.reveal.rememberRevealState
import junit.framework.TestCase.assertTrue
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

/**
 * Regression test for issue #360: the reveal area must keep tracking the revealable when the
 * element moves after the effect is already shown.
 *
 * This mirrors the scenario from the report where content is inserted above the coach target after
 * the reveal, and also covers the originally reported symptom, where a settling layout pass on some
 * devices moved the target right after `reveal()` was called. Before the fix, `reveal()` copied the
 * geometry once and neither the cutout nor the overlay content ever followed, so the effect stayed
 * permanently offset.
 */
class RevealMovingRevealableTest {

    private enum class Keys { Target }

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun overlayFollowsRevealableThatMovesAfterReveal() {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope
        var spacerHeight by mutableStateOf(0.dp)

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            revealState = rememberRevealState()

            Reveal(
                modifier = Modifier.fillMaxSize(),
                revealState = revealState,
                // Instant animations so the overlay is fully laid out once it appears.
                overlayEffect = DimRevealOverlayEffect(
                    alphaAnimationSpec = snap(),
                    contentAlphaAnimationSpec = snap(),
                ),
                overlayContent = {
                    Box(
                        modifier = Modifier
                            .align(horizontalArrangement = RevealOverlayArrangement.Start)
                            .size(100.dp)
                            .testTag(OVERLAY_TAG),
                    )
                },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(spacerHeight))
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(48.dp)
                            .testTag(TARGET_TAG)
                            .revealable(key = Keys.Target, padding = PaddingValues(0.dp)),
                    )
                }
            }
        }

        scope.launch { revealState.reveal(Keys.Target) }
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        // boundsInWindow (not boundsInRoot) is used throughout because the target lives in the
        // activity's composition root while the overlay content lives in the popup's own root, and
        // the popup deliberately draws behind the system bars. In root coordinates the two are
        // offset by the status bar height, which is zero only on an edge-to-edge device.
        val overlayBefore = composeTestRule
            .onNodeWithTag(OVERLAY_TAG)
            .fetchSemanticsNode()
            .boundsInWindow

        // Insert content above the target, moving it down while the effect is visible.
        spacerHeight = 200.dp
        composeTestRule.waitForIdle()

        val target = composeTestRule.onNodeWithTag(TARGET_TAG).fetchSemanticsNode().boundsInWindow
        val overlay = composeTestRule.onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow

        assertTrue(
            "Overlay content should have moved down with the target, but stayed at $overlayBefore",
            overlay.top > overlayBefore.top,
        )

        // The overlay is aligned to the start of the reveal area, so it must abut the target's
        // left edge and stay centered on it vertically at the target's new position.
        assertTrue(
            "Overlay content should abut the moved reveal area's left edge, but " +
                "overlay=$overlay target=$target",
            abs(overlay.right - target.left) <= TOLERANCE,
        )
        assertTrue(
            "Overlay content should be vertically centered on the moved reveal area, but " +
                "overlay=$overlay target=$target",
            abs(overlay.center.y - target.center.y) <= TOLERANCE,
        )
    }

    private companion object {
        const val TARGET_TAG = "target"
        const val OVERLAY_TAG = "overlayContent"

        // Pixels, since boundsInWindow reports pixels rather than dp.
        const val TOLERANCE = 1.0f
    }
}
