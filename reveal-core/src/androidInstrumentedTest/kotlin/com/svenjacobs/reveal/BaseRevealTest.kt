package com.svenjacobs.reveal

import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.CoroutineScope
import org.junit.Rule

public abstract class BaseRevealTest {

	internal enum class Keys { Key1, Key2, Key3 }

	@get:Rule
	public val composeTestRule: ComposeContentTestRule = createComposeRule()

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

			val revealCanvasState = rememberRevealCanvasState()

			RevealCanvas(revealCanvasState = revealCanvasState) {
				Reveal(
					onRevealableClick = onRevealableClick,
					onOverlayClick = onOverlayClick,
					revealCanvasState = revealCanvasState,
					revealState = revealState,
					overlayContent = { key ->
						when (key) {
							Keys.Key1 -> Text("Overlay1")
							Keys.Key2 -> Text("Overlay2")
						}
					},
				) {
					Text(
						modifier = Modifier.revealable(key = Keys.Key1),
						text = "Element1",
					)

					Text(
						modifier = Modifier.revealable(key = Keys.Key2),
						text = "Element2",
					)
				}
			}
		}

		body(composeTestRule, revealState, scope)
	}
}
