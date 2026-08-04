package com.svenjacobs.reveal.android.tests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.OnClick
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

/**
 * Regression test for issue #100: the reveal effect must be drawn above a `ModalBottomSheet` and
 * clicks must be routed to the correct revealable when [Reveal] and its revealables live inside
 * the sheet's own (dialog window) composition.
 *
 * Before this fix the overlay was hoisted to the activity window via `RevealCanvas`, so it was drawn
 * *beneath* the sheet. Now the overlay renders in a full screen
 * [androidx.compose.ui.window.Popup] created from inside the sheet's own composition, so it always
 * attaches to the same window as the sheet and draws above it.
 */
class RevealInModalBottomSheetTest {

    private enum class Keys { Target }

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun revealableInsideModalBottomSheetReceivesClickAtItsOwnPosition() {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope
        var revealedKey: Key? = null

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            revealState = rememberRevealState()
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            MaterialTheme {
                ModalBottomSheet(
                    onDismissRequest = {},
                    sheetState = sheetState,
                ) {
                    Reveal(
                        modifier = Modifier.fillMaxWidth(),
                        revealState = revealState,
                        overlayContent = {
                            Text(
                                text = "Explanation",
                                modifier = Modifier.testTag(OVERLAY_TAG),
                            )
                        },
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(32.dp)
                                .testTag(TARGET_TAG)
                                .revealable(
                                    key = Keys.Target,
                                    onClick = OnClick.Listener { key -> revealedKey = key },
                                ),
                        ) {
                            Text("Target")
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        scope.launch { revealState.reveal(Keys.Target) }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
        }

        // Proves the overlay is actually laid out and reachable above the sheet, not just present
        // in the composition.
        composeTestRule.onNodeWithTag(OVERLAY_TAG).assertIsDisplayed()

        // boundsInWindow (not boundsInRoot) is used for both nodes because the sheet's own
        // composition root and the popup's composition root are different roots; boundsInWindow is
        // the coordinate space they have in common, since both are hosted by the same dialog window.
        val catcherNode = composeTestRule.onNodeWithTag("overlay").fetchSemanticsNode()
        val targetWindowBounds = composeTestRule.onNodeWithTag(TARGET_TAG)
            .fetchSemanticsNode()
            .boundsInWindow
        val clickOffsetOnCatcher = targetWindowBounds.center - catcherNode.boundsInWindow.topLeft

        composeTestRule.onNodeWithTag("overlay").performTouchInput {
            click(clickOffsetOnCatcher)
        }

        assertEquals(
            "Clicking the revealable's on-screen position inside the ModalBottomSheet did not " +
                "route to its OnClick.Listener; the overlay is likely misaligned",
            Keys.Target,
            revealedKey,
        )
    }

    private companion object {
        const val OVERLAY_TAG = "overlayContent"
        const val TARGET_TAG = "target"
    }
}
