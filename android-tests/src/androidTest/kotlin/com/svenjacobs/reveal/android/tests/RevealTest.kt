package com.svenjacobs.reveal.android.tests

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.svenjacobs.reveal.Key
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.launch
import org.junit.Test

class RevealTest : BaseRevealTest() {

	@Test
	fun clickCallsOnRevealableClick() {
		var onRevealableClickKey: Key? = null

		test(
			onRevealableClick = { key -> onRevealableClickKey = key },
		) { testRule, revealState, scope ->
			scope.launch { revealState.reveal(Keys.Key1) }

			testRule.onNodeWithText("Overlay1").performClick()

			assertEquals(Keys.Key1, onRevealableClickKey)
		}
	}

	@Test
	fun clickCallsOnOverlayClick() {
		var onOverlayClickKey: Key? = null

		test(
			onOverlayClick = { key -> onOverlayClickKey = key },
		) { testRule, revealState, scope ->
			scope.launch { revealState.reveal(Keys.Key1) }

			testRule.onNodeWithTag("overlay").performClick()

			assertEquals(Keys.Key1, onOverlayClickKey)
		}
	}
}
