package com.svenjacobs.reveal.android.tests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealScope
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

/**
 * Regression tests for issue #338: composables placed inside overlayContent via
 * [com.svenjacobs.reveal.RevealOverlayScope.align] must report their actual drawn bounds (not the
 * full overlay size) so that they are reachable via semantics-based UI automation (Espresso, UI
 * Automator, Maestro), which locate and tap elements using the bounds reported to the accessibility
 * tree.
 */
class RevealOverlayAccessibilityTest {

    private enum class Keys { Target }

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun alignedItemWithTagAppliedBeforeAlignReportsDrawnBoundsAndIsClickable() {
        assertAlignedItemIsAccessible(testTagAppliedBeforeAlign = true)
    }

    @Test
    fun alignedItemWithTagAppliedAfterAlignReportsDrawnBoundsAndIsClickable() {
        assertAlignedItemIsAccessible(testTagAppliedBeforeAlign = false)
    }

    @Test
    fun unalignedSiblingOfAnAlignedItemStaysAccessible() {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope
        var siblingClicked = false

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            revealState = rememberRevealState()

            Reveal(
                modifier = Modifier.fillMaxSize(),
                revealState = revealState,
                overlayContent = {
                    // Composed first so that, under the pre-fix full-screen align() bounds, it
                    // would be pruned from the semantics tree by the aligned item "drawn on top"
                    // of it (semantics siblings are processed back-to-front).
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .testTag(SIBLING_TAG)
                            .clickable { siblingClicked = true },
                    )
                    Box(
                        modifier = Modifier
                            .testTag(OVERLAY_TAG)
                            .align(verticalArrangement = RevealOverlayArrangement.Bottom)
                            .size(width = 120.dp, height = 48.dp),
                    )
                },
            ) {
                RevealTarget(Keys.Target)
            }
        }

        revealTargetAndWaitForOverlay(scope, revealState)

        composeTestRule.onNodeWithTag(SIBLING_TAG)
            .assertExists()
            .performClick()

        assertTrue("Sibling of aligned overlay item was not clickable", siblingClicked)
    }

    private fun assertAlignedItemIsAccessible(testTagAppliedBeforeAlign: Boolean) {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope
        var itemClicked = false
        var overlayClicked = false

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            revealState = rememberRevealState()

            Reveal(
                modifier = Modifier.fillMaxSize(),
                revealState = revealState,
                onOverlayClick = { overlayClicked = true },
                overlayContent = {
                    val itemModifier = if (testTagAppliedBeforeAlign) {
                        Modifier
                            .testTag(OVERLAY_TAG)
                            .align(verticalArrangement = RevealOverlayArrangement.Bottom)
                            .clickable { itemClicked = true }
                    } else {
                        Modifier
                            .align(verticalArrangement = RevealOverlayArrangement.Bottom)
                            .testTag(OVERLAY_TAG)
                            .clickable { itemClicked = true }
                    }

                    Box(modifier = itemModifier.size(width = 120.dp, height = 48.dp))
                },
            ) {
                RevealTarget(Keys.Target)
            }
        }

        revealTargetAndWaitForOverlay(scope, revealState)

        // The overlay is rendered inside its own full screen Popup, which is a separate
        // semantics root, so onRoot() would be ambiguous. Use the library's "overlay" catcher
        // node (a sibling of the overlay content within the same popup) as the window bounds.
        val root = composeTestRule.onNodeWithTag("overlay").getUnclippedBoundsInRoot()
        val overlayBounds = composeTestRule.onNodeWithTag(OVERLAY_TAG).getUnclippedBoundsInRoot()

        assertTrue(
            "Aligned overlay item reports the full overlay width ($overlayBounds) instead of " +
                "its own drawn size, root=$root",
            (overlayBounds.right - overlayBounds.left) < (root.right - root.left),
        )
        assertTrue(
            "Aligned overlay item reports the full overlay height ($overlayBounds) instead of " +
                "its own drawn size, root=$root",
            (overlayBounds.bottom - overlayBounds.top) < (root.bottom - root.top),
        )
        assertTrue(
            "Aligned overlay item is not offset from the top as expected by its arrangement",
            overlayBounds.top > root.top,
        )

        composeTestRule.onNodeWithTag(OVERLAY_TAG)
            .assertExists()
            .performClick()

        assertTrue(
            "Clicking the aligned overlay item at its reported bounds missed it",
            itemClicked,
        )
        assertFalse(
            "Click on the aligned overlay item's own bounds incorrectly fell through to the " +
                "overlay background",
            overlayClicked,
        )
    }

    private fun revealTargetAndWaitForOverlay(scope: CoroutineScope, revealState: RevealState) {
        scope.launch { revealState.reveal(Keys.Target) }
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Composable
    private fun RevealScope.RevealTarget(key: Keys) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(24.dp)
                    .revealable(key = key, padding = PaddingValues(0.dp)),
            )
        }
    }

    private companion object {
        const val OVERLAY_TAG = "overlayContent"
        const val SIBLING_TAG = "overlaySibling"
    }
}
