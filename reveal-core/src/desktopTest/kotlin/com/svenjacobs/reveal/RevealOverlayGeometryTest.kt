package com.svenjacobs.reveal

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tests for the coordinate mapping between [Reveal]'s own composition root, where reveal areas are
 * recorded via `positionInRoot()`, and the overlay popup's composition root, where the effect is
 * drawn. These live in the desktop source set because it is the only target where the skiko popup
 * implementation can be exercised without a device.
 *
 * Caveat on their reach: `runComposeUiTest` renders without a real window, so both composition
 * roots resolve to the same origin here. That makes these tests a guard against gross mapping
 * errors, but it also means they do **not** reproduce the class of bug where the two roots
 * disagree only once a real window is involved — for example deriving the mapping from a
 * `positionOnScreen()` reading taken separately in each root, which returns `Offset.Unspecified`
 * (NaN) on skiko while the underlying AWT component isn't showing. That variant was verified to
 * still pass these tests while being visibly broken in the demo app, so changes to the mapping
 * still warrant a manual look at a real desktop window.
 */
@OptIn(ExperimentalTestApi::class)
class RevealOverlayGeometryTest {

    @Test
    fun overlayContentIsPlacedNextToTheRevealArea() = runComposeUiTest {
        revealAndWaitForOverlay()

        val target = onNodeWithTag(TARGET_TAG).fetchSemanticsNode().boundsInWindow
        val overlay = onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow

        // Aligned to the start of the reveal area, so it should sit immediately left of the
        // target and be centered on it vertically. If the reveal area were mapped to the wrong
        // coordinate space, it would fall outside the popup and the overlay content would instead
        // be clamped against a window edge, far away from the target.
        assertTrue(
            abs(overlay.right - target.left) <= TOLERANCE,
            "Overlay content should abut the reveal area's left edge, but " +
                "overlay=$overlay target=$target",
        )
        assertTrue(
            abs(overlay.center.y - target.center.y) <= TOLERANCE,
            "Overlay content should be vertically centered on the reveal area, but " +
                "overlay=$overlay target=$target",
        )
    }

    @Test
    fun revealAreaStaysWithinTheOverlay() = runComposeUiTest {
        revealAndWaitForOverlay()

        val catcher = onNodeWithTag("overlay").fetchSemanticsNode().boundsInWindow
        val target = onNodeWithTag(TARGET_TAG).fetchSemanticsNode().boundsInWindow

        // The revealable is centered in a full screen Reveal, so its area must land well inside
        // the overlay rather than off in a corner.
        assertTrue(
            target.left >= catcher.left &&
                target.right <= catcher.right &&
                target.top >= catcher.top &&
                target.bottom <= catcher.bottom,
            "Reveal area should be contained by the overlay, but target=$target catcher=$catcher",
        )
    }

    private fun ComposeUiTest.revealAndWaitForOverlay() {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        setContent {
            revealState = rememberRevealState()
            scope = rememberCoroutineScope()
            RevealUnderTest(revealState)
        }

        // Lay out the content first so the revealable is registered via onGloballyPositioned,
        // then reveal it.
        waitForIdle()
        scope.launch { revealState.reveal(KEY) }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Composable
    private fun RevealUnderTest(revealState: RevealState) {
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
                        .size(80.dp)
                        .testTag(OVERLAY_TAG),
                )
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .testTag(TARGET_TAG)
                        .revealable(key = KEY, padding = PaddingValues(0.dp)),
                )
            }
        }
    }

    private companion object {
        const val TARGET_TAG = "target"
        const val OVERLAY_TAG = "overlayContent"
        const val TOLERANCE = 1.0f
        val KEY: Key = "key"
    }
}
