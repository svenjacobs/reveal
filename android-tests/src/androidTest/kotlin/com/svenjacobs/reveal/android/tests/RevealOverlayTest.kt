package com.svenjacobs.reveal.android.tests

import androidx.compose.ui.test.onNodeWithText
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.launch
import org.junit.Test

class RevealOverlayTest : BaseRevealTest() {

	@Test
	fun revealShowsOverlay() {
		test { testRule, revealState, scope ->
			scope.launch { revealState.reveal(Keys.Key1) }

			assertTrue(revealState.isVisible)
			assertEquals(null, revealState.previousRevealableKey)
			assertEquals(Keys.Key1, revealState.currentRevealableKey)

			testRule.onNodeWithText("Overlay1").assertExists()
			testRule.onNodeWithText("Overlay2").assertDoesNotExist()

			scope.launch { revealState.reveal(Keys.Key2) }

			assertTrue(revealState.isVisible)
			assertEquals(Keys.Key1, revealState.previousRevealableKey)
			assertEquals(Keys.Key2, revealState.currentRevealableKey)

			testRule.onNodeWithText("Overlay1").assertDoesNotExist()
			testRule.onNodeWithText("Overlay2").assertExists()
		}
	}

	@Test
	fun hideHidesOverlay() {
		test { testRule, revealState, scope ->
			scope.launch { revealState.reveal(Keys.Key1) }

			assertTrue(revealState.isVisible)
			assertEquals(Keys.Key1, revealState.currentRevealableKey)

			scope.launch { revealState.hide() }

			assertFalse(revealState.isVisible)

			testRule.onNodeWithText("Overlay1").assertDoesNotExist()
		}
	}
}
