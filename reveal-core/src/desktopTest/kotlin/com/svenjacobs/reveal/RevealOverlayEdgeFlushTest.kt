package com.svenjacobs.reveal

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Regression tests for issue #376: [Revealable.computeArea] inflates the reveal area by the
 * revealable's padding (8dp by default) — and, for [RevealShape.Circle], by the circle expansion
 * around its centre — without clamping to the overlay bounds, so an element flush with a window
 * edge produces a reveal area that extends past that edge. When [RevealOverlayArrangement] then
 * arranges overlay content on the outside of that edge (e.g. [RevealOverlayArrangement.Top] for a
 * top-flush element), the arranged area used to have a negative width or height, which was passed
 * straight into `Constraints(...)` and crashed with `IllegalArgumentException`.
 *
 * [RevealOverlayArrangementTest] covers the same clamping at the level of the arranged geometry
 * alone; these tests exercise it through a real composition, including the inflation that produces
 * the out-of-bounds reveal area in the first place.
 *
 * These live in the desktop source set for the same reason as [RevealOverlayGeometryTest]: it is
 * the only target where the skiko popup implementation can be exercised without a device.
 */
@OptIn(ExperimentalTestApi::class)
class RevealOverlayEdgeFlushTest {

    @Test
    fun alignTopDoesNotCrashForATopFlushRevealable() = runComposeUiTest {
        revealEdgeFlushAndWaitForOverlay(targetAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .align(verticalArrangement = RevealOverlayArrangement.Top)
                    .size(80.dp)
                    .testTag(OVERLAY_TAG),
            )
        }

        val overlay = onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow
        assertTrue(
            overlay.top >= 0f,
            "Overlay content should stay within the window, but was $overlay",
        )
    }

    @Test
    fun alignBottomDoesNotCrashForABottomFlushRevealable() = runComposeUiTest {
        revealEdgeFlushAndWaitForOverlay(targetAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .align(verticalArrangement = RevealOverlayArrangement.Bottom)
                    .size(80.dp)
                    .testTag(OVERLAY_TAG),
            )
        }

        val window = onNodeWithTag(WINDOW_TAG).fetchSemanticsNode().boundsInWindow
        val overlay = onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow
        assertTrue(
            overlay.bottom <= window.bottom,
            "Overlay content should stay within the window, but overlay=$overlay window=$window",
        )
    }

    @Test
    fun alignStartDoesNotCrashForAStartFlushRevealable() = runComposeUiTest {
        revealEdgeFlushAndWaitForOverlay(targetAlignment = Alignment.CenterStart) {
            Box(
                modifier = Modifier
                    .align(horizontalArrangement = RevealOverlayArrangement.Start)
                    .size(80.dp)
                    .testTag(OVERLAY_TAG),
            )
        }

        val overlay = onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow
        assertTrue(
            overlay.left >= 0f,
            "Overlay content should stay within the window, but was $overlay",
        )
    }

    @Test
    fun alignEndDoesNotCrashForAnEndFlushRevealable() = runComposeUiTest {
        revealEdgeFlushAndWaitForOverlay(targetAlignment = Alignment.CenterEnd) {
            Box(
                modifier = Modifier
                    .align(horizontalArrangement = RevealOverlayArrangement.End)
                    .size(80.dp)
                    .testTag(OVERLAY_TAG),
            )
        }

        val window = onNodeWithTag(WINDOW_TAG).fetchSemanticsNode().boundsInWindow
        val overlay = onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow
        assertTrue(
            overlay.right <= window.right,
            "Overlay content should stay within the window, but overlay=$overlay window=$window",
        )
    }

    @Test
    fun alignTopDoesNotCrashForAWideCircleShapedRevealable() = runComposeUiTest {
        // The other inflation source: RevealShape.Circle expands the area to a square of
        // maxDimension / 2 around its centre, so a wide, short element grows by far more than the
        // padding along the short axis. Here that pushes the reveal area roughly 116dp above the
        // top edge, well past what the 8dp padding alone would do.
        revealEdgeFlushAndWaitForOverlay(
            targetAlignment = Alignment.TopCenter,
            shape = RevealShape.Circle,
            targetWidth = 240.dp,
            targetHeight = 24.dp,
        ) {
            Box(
                modifier = Modifier
                    .align(verticalArrangement = RevealOverlayArrangement.Top)
                    .size(80.dp)
                    .testTag(OVERLAY_TAG),
            )
        }

        val overlay = onNodeWithTag(OVERLAY_TAG).fetchSemanticsNode().boundsInWindow
        assertTrue(
            overlay.top >= 0f,
            "Overlay content should stay within the window, but was $overlay",
        )
    }

    /**
     * Reveals a target of [targetWidth] x [targetHeight] flush with the edge of the window implied
     * by [targetAlignment], using the default 8dp revealable padding so that
     * [Revealable.computeArea] pushes the reveal area past that edge, and waits for the overlay
     * content built by [overlayContent] to appear. [shape] defaults to the same shape as
     * [RevealScope.revealable]; pass [RevealShape.Circle] to additionally exercise the circle
     * expansion in [Revealable.computeArea].
     */
    private fun ComposeUiTest.revealEdgeFlushAndWaitForOverlay(
        targetAlignment: Alignment,
        shape: RevealShape = RevealShape.RoundRect(4.dp),
        targetWidth: Dp = 48.dp,
        targetHeight: Dp = 48.dp,
        overlayContent: @Composable RevealOverlayScope.(key: Key) -> Unit,
    ) {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        setContent {
            revealState = rememberRevealState()
            scope = rememberCoroutineScope()

            Reveal(
                modifier = Modifier.fillMaxSize().testTag(WINDOW_TAG),
                revealState = revealState,
                // Instant animations so the overlay is fully laid out once it appears.
                overlayEffect = DimRevealOverlayEffect(
                    alphaAnimationSpec = snap(),
                    contentAlphaAnimationSpec = snap(),
                ),
                overlayContent = overlayContent,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(targetAlignment)
                            .size(width = targetWidth, height = targetHeight)
                            .testTag(TARGET_TAG)
                            .revealable(key = KEY, shape = shape),
                    )
                }
            }
        }

        waitForIdle()
        scope.launch { revealState.reveal(KEY) }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val WINDOW_TAG = "window"
        const val TARGET_TAG = "target"
        const val OVERLAY_TAG = "overlayContent"
        val KEY: Key = "key"
    }
}
