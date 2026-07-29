package com.svenjacobs.reveal.android.tests

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.OnClickListener
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import kotlinx.coroutines.CoroutineScope
import org.junit.Rule

abstract class BaseRevealTest {

    internal enum class Keys { Key1, Key2, Key3 }

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    internal fun test(
        onRevealableClick: OnClickListener = {},
        onOverlayClick: OnClickListener = {},
        body: (
            testRule: ComposeContentTestRule,
            revealState: RevealState,
            scope: CoroutineScope,
        ) -> Unit,
    ) {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            revealState = rememberRevealState()

            Reveal(
                // this must take full screen for correct clicks handling by test runner
                modifier = Modifier.fillMaxSize(),
                onRevealableClick = onRevealableClick,
                onOverlayClick = onOverlayClick,
                revealState = revealState,
                overlayContent = { key ->
                    when (key) {
                        Keys.Key1 -> Text("Overlay1")
                        Keys.Key2 -> Text("Overlay2")
                    }
                },
            ) {
                Text(
                    // Unaligned overlay content (e.g. "Overlay1" in RevealTest) is placed at the
                    // popup's own top-left, which - unlike the app's own window - is not inset by
                    // system bars (the popup deliberately draws behind them). A generous padding
                    // keeps the revealable's mapped area overlapping that corner regardless of
                    // system bar height, so tests relying on that overlap aren't tied to a specific
                    // screen/density/system bar configuration.
                    modifier = Modifier.revealable(key = Keys.Key1, padding = PaddingValues(64.dp)),
                    text = "Element1",
                )

                Text(
                    modifier = Modifier.revealable(key = Keys.Key2, padding = PaddingValues(64.dp)),
                    text = "Element2",
                )
            }
        }

        body(composeTestRule, revealState, scope)
    }
}
