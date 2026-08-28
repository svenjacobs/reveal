package com.svenjacobs.reveal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tests for revealing multiple elements at once (issue #105). Like [RevealOverlayGeometryTest]
 * these live in the desktop source set because it is the only target where the overlay popup can be
 * exercised without a device, and the same caveat applies: `runComposeUiTest` renders without a
 * real window, so these are a guard against gross errors rather than a substitute for looking at
 * the demo app.
 */
@OptIn(ExperimentalTestApi::class)
class RevealMultipleRevealablesTest {

    @Test
    fun overlayContentIsPlacedNextToEachRevealArea() = runComposeUiTest {
        val (revealState, scope) = setUp()

        scope.launch { revealState.reveal(FIRST, SECOND) }
        waitForOverlaysOf(FIRST, SECOND)

        assertEquals(listOf(FIRST, SECOND), revealState.currentRevealableKeys)

        for (key in listOf(FIRST, SECOND)) {
            val target = onNodeWithTag(targetTag(key)).fetchSemanticsNode().boundsInWindow
            val overlay = onNodeWithTag(overlayTag(key)).fetchSemanticsNode().boundsInWindow

            // Each overlay content is aligned to the start of its own reveal area, so it must abut
            // that element and not the other one.
            assertTrue(
                abs(overlay.right - target.left) <= TOLERANCE,
                "Overlay content of $key should abut its reveal area's left edge, but " +
                    "overlay=$overlay target=$target",
            )
            assertTrue(
                abs(overlay.center.y - target.center.y) <= TOLERANCE,
                "Overlay content of $key should be vertically centered on its reveal area, but " +
                    "overlay=$overlay target=$target",
            )
        }
    }

    /**
     * [Key] is `Any`, so a collection of keys is also a valid single key. Overload resolution must
     * pick the `Iterable` overload, otherwise the list itself would be looked up as one key.
     */
    @Test
    fun aCollectionOfKeysRevealsAllOfThem() = runComposeUiTest {
        val (revealState, scope) = setUp()

        scope.launch { revealState.reveal(listOf(FIRST, SECOND)) }
        waitForOverlaysOf(FIRST, SECOND)

        assertEquals(listOf(FIRST, SECOND), revealState.currentRevealableKeys)
    }

    @Test
    fun revealingTheSameKeyAgainDoesNotAlsoFadeItOut() = runComposeUiTest {
        val (revealState, scope) = setUp()

        scope.launch { revealState.reveal(FIRST, SECOND) }
        waitForOverlaysOf(FIRST, SECOND)

        scope.launch { revealState.reveal(SECOND, THIRD) }
        waitForOverlaysOf(SECOND, THIRD)

        assertEquals(listOf(SECOND, THIRD), revealState.currentRevealableKeys)
        assertEquals(listOf(FIRST), revealState.previousRevealableKeys)
    }

    @Test
    fun revealFailsWhenAnyKeyIsUnknown() = runComposeUiTest {
        val (revealState, scope) = setUp()

        var error: IllegalArgumentException? = null
        scope.launch { error = assertFailsWith { revealState.reveal(FIRST, UNKNOWN) } }
        waitForIdle()

        assertTrue(error?.message?.contains(UNKNOWN) == true, "was ${error?.message}")
        assertEquals(emptyList(), revealState.currentRevealableKeys)
        assertFalse(revealState.isVisible)

        var result = true
        scope.launch { result = revealState.tryReveal(FIRST, UNKNOWN) }
        waitForIdle()

        assertFalse(result)
        assertEquals(emptyList(), revealState.currentRevealableKeys)
        assertFalse(revealState.isVisible)
    }

    /**
     * The overlay is faded in and out via its animation specs. If the alpha animation were skipped,
     * the overlay would disappear within a frame of [RevealState.hide] instead of after the
     * animation has run.
     */
    @Test
    fun overlayIsFadedOutInsteadOfDisappearingInstantly() = runComposeUiTest {
        // Default animation specs, unlike the other tests here which use snap().
        val (revealState, scope) = setUp(animationSpec = tween(durationMillis = 500))

        scope.launch { revealState.reveal(FIRST) }
        waitForOverlaysOf(FIRST)

        mainClock.autoAdvance = false
        scope.launch { revealState.hide() }
        mainClock.advanceTimeBy(100)

        assertTrue(
            onAllNodesWithTag(overlayTag(FIRST)).fetchSemanticsNodes().isNotEmpty(),
            "Overlay should still be fading out 100 ms after hide()",
        )

        mainClock.advanceTimeBy(1_000)

        assertTrue(
            onAllNodesWithTag(overlayTag(FIRST)).fetchSemanticsNodes().isEmpty(),
            "Overlay should be gone after the fade out animation finished",
        )
    }

    /**
     * Sets the content and lays it out, so that all revealables are registered via
     * `onGloballyPositioned` before anything is revealed.
     */
    private fun ComposeUiTest.setUp(
        animationSpec: AnimationSpec<Float> = snap(),
    ): Pair<RevealState, CoroutineScope> {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        setContent {
            revealState = rememberRevealState()
            scope = rememberCoroutineScope()
            RevealUnderTest(revealState, animationSpec)
        }

        waitForIdle()

        return revealState to scope
    }

    private fun ComposeUiTest.waitForOverlaysOf(vararg keys: Key) {
        waitUntil(timeoutMillis = 5_000) {
            keys.all { onAllNodesWithTag(overlayTag(it)).fetchSemanticsNodes().isNotEmpty() }
        }
    }

    @Composable
    private fun RevealUnderTest(revealState: RevealState, animationSpec: AnimationSpec<Float>) {
        Reveal(
            modifier = Modifier.fillMaxSize(),
            revealState = revealState,
            // Instant animations by default, so the overlay is fully laid out once it
            // appears.
            overlayEffect = DimRevealOverlayEffect(
                alphaAnimationSpec = animationSpec,
                contentAlphaAnimationSpec = animationSpec,
            ),
            overlayContent = { key ->
                Box(
                    modifier = Modifier
                        .align(horizontalArrangement = RevealOverlayArrangement.Start)
                        .size(40.dp)
                        .testTag(overlayTag(key)),
                )
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                for ((index, key) in KEYS.withIndex()) {
                    Box(
                        modifier = Modifier
                            .align(ALIGNMENTS[index])
                            .size(48.dp)
                            .testTag(targetTag(key))
                            .revealable(key = key, padding = PaddingValues(0.dp)),
                    )
                }
            }
        }
    }

    private companion object {
        const val TOLERANCE = 1.0f
        const val UNKNOWN = "unknown"
        val FIRST: Key = "first"
        val SECOND: Key = "second"
        val THIRD: Key = "third"
        val KEYS = listOf(FIRST, SECOND, THIRD)
        val ALIGNMENTS = listOf(Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd)

        fun targetTag(key: Key): String = "target-$key"

        fun overlayTag(key: Key): String = "overlay-$key"
    }
}
