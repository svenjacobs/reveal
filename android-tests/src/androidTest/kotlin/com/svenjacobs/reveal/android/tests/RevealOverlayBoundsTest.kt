package com.svenjacobs.reveal.android.tests

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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealCanvas
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealCanvasState
import com.svenjacobs.reveal.rememberRevealState
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test

/**
 * Regression tests for issue #194: overlay content positioned next to a reveal area that is close to
 * a screen boundary must not be placed off screen. The overlay should be confined to the available
 * space and shifted back into bounds when it would otherwise overflow.
 */
class RevealOverlayBoundsTest {

	private enum class Keys { Target }

	@get:Rule
	val composeTestRule: ComposeContentTestRule = createComposeRule()

	@Test
	fun topArrangementStaysWithinScreen() {
		// Reveal area near the left edge, plenty of space above: a wide overlay centered above the
		// reveal area would overflow the left boundary without dynamic adjustment.
		assertOverlayWithinScreen(revealableAlignment = Alignment.BottomStart) {
			OverlayBox(
				modifier = Modifier.align(
					verticalArrangement = RevealOverlayArrangement.Top,
				),
			)
		}
	}

	@Test
	fun bottomArrangementStaysWithinScreen() {
		assertOverlayWithinScreen(revealableAlignment = Alignment.TopStart) {
			OverlayBox(
				modifier = Modifier.align(
					verticalArrangement = RevealOverlayArrangement.Bottom,
				),
			)
		}
	}

	@Test
	fun startArrangementStaysWithinScreen() {
		// Reveal area near the top edge, plenty of space to the start: a tall overlay centered
		// vertically would overflow the top boundary without dynamic adjustment.
		assertOverlayWithinScreen(revealableAlignment = Alignment.TopEnd) {
			OverlayBox(
				modifier = Modifier.align(
					horizontalArrangement = RevealOverlayArrangement.Start,
				),
			)
		}
	}

	@Test
	fun endArrangementStaysWithinScreen() {
		assertOverlayWithinScreen(revealableAlignment = Alignment.TopStart) {
			OverlayBox(
				modifier = Modifier.align(
					horizontalArrangement = RevealOverlayArrangement.End,
				),
			)
		}
	}

	@Composable
	private fun OverlayBox(modifier: Modifier) {
		Box(
			modifier = modifier
				.size(width = 300.dp, height = 300.dp)
				.testTag(OVERLAY_TAG),
		)
	}

	private fun assertOverlayWithinScreen(
		revealableAlignment: Alignment,
		overlay: @Composable RevealOverlayScope.() -> Unit,
	) {
		lateinit var revealState: RevealState
		lateinit var scope: CoroutineScope

		composeTestRule.setContent {
			scope = rememberCoroutineScope()
			revealState = rememberRevealState()
			val revealCanvasState = rememberRevealCanvasState()

			RevealCanvas(
				revealCanvasState = revealCanvasState,
				modifier = Modifier.fillMaxSize(),
			) {
				Reveal(
					modifier = Modifier.fillMaxSize(),
					revealCanvasState = revealCanvasState,
					revealState = revealState,
					overlayContent = { overlay() },
				) {
					Box(modifier = Modifier.fillMaxSize()) {
						Box(
							modifier = Modifier
								.align(revealableAlignment)
								.padding(16.dp)
								.size(24.dp)
								.revealable(
									key = Keys.Target,
									padding = PaddingValues(0.dp),
								),
						)
					}
				}
			}
		}

		scope.launch { revealState.reveal(Keys.Target) }
		composeTestRule.waitForIdle()
		composeTestRule.waitUntil(timeoutMillis = 5_000) {
			composeTestRule.onAllNodesWithTag(OVERLAY_TAG).fetchSemanticsNodes().isNotEmpty()
		}

		val root = composeTestRule.onRoot().getUnclippedBoundsInRoot()
		val overlayBounds = composeTestRule.onNodeWithTag(OVERLAY_TAG).getUnclippedBoundsInRoot()

		assertTrue(
			"Overlay overflows left edge: left=${overlayBounds.left}, root left=${root.left}",
			overlayBounds.left.value >= root.left.value - TOLERANCE,
		)
		assertTrue(
			"Overlay overflows top edge: top=${overlayBounds.top}, root top=${root.top}",
			overlayBounds.top.value >= root.top.value - TOLERANCE,
		)
		assertTrue(
			"Overlay overflows right edge: right=${overlayBounds.right}, root right=${root.right}",
			overlayBounds.right.value <= root.right.value + TOLERANCE,
		)
		assertTrue(
			"Overlay overflows bottom edge: bottom=${overlayBounds.bottom}, root bottom=${root.bottom}",
			overlayBounds.bottom.value <= root.bottom.value + TOLERANCE,
		)
	}

	private companion object {
		const val OVERLAY_TAG = "overlayContent"
		const val TOLERANCE = 0.5f
	}
}
