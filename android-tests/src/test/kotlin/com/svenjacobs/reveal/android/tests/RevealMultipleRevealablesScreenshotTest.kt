package com.svenjacobs.reveal.android.tests

import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot regression test for issue #105: two elements revealed at once must produce two
 * cut-outs, each with its own overlay content anchored to its own reveal area, while a third
 * element stays dimmed.
 *
 * Record goldens with `./gradlew :android-tests:recordRoborazziDebug` and verify with
 * `./gradlew :android-tests:verifyRoborazziDebug`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Robolectric 4.16.1 supports SDK 36 at most, so pin it instead of following targetSdk.
@Config(sdk = [36], qualifiers = "w360dp-h720dp-mdpi")
class RevealMultipleRevealablesScreenshotTest {

    private enum class Keys { First, Second, Third }

    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun multipleRevealables() {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        composeRule.setContent {
            revealState = rememberRevealState()
            scope = rememberCoroutineScope()

            Reveal(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                revealState = revealState,
                // Use instant animations so the overlay is fully visible for a deterministic
                // capture.
                overlayEffect = DimRevealOverlayEffect(
                    alphaAnimationSpec = snap(),
                    contentAlphaAnimationSpec = snap(),
                ),
                overlayContent = { key ->
                    when (key) {
                        Keys.First -> OverlayBalloon(
                            modifier = Modifier.align(
                                verticalArrangement = RevealOverlayArrangement.Bottom,
                            ),
                            arrow = Arrow.top(anchorToReveal = true),
                            text = "First element",
                        )

                        Keys.Second -> OverlayBalloon(
                            modifier = Modifier.align(
                                verticalArrangement = RevealOverlayArrangement.Top,
                            ),
                            arrow = Arrow.bottom(anchorToReveal = true),
                            text = "Second element",
                        )
                    }
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    for ((key, alignment) in ALIGNMENTS) {
                        Box(
                            modifier = Modifier
                                .align(alignment)
                                .padding(24.dp)
                                .size(48.dp)
                                .background(Color.Blue)
                                .revealable(key = key),
                        )
                    }
                }
            }
        }

        // Lay out the content first so the revealables are registered, then reveal both of them.
        composeRule.waitForIdle()
        scope.launch { revealState.reveal(Keys.First, Keys.Second) }
        composeRule.waitForIdle()

        captureScreenRoboImage(filePath = "screenshots/multiple_revealables.png")
    }

    private companion object {
        val ALIGNMENTS = mapOf<Keys, Alignment>(
            Keys.First to Alignment.TopStart,
            Keys.Second to Alignment.BottomEnd,
            Keys.Third to Alignment.Center,
        )
    }
}

@Composable
private fun OverlayBalloon(text: String, arrow: Arrow, modifier: Modifier = Modifier) {
    Balloon(
        modifier = modifier.padding(8.dp),
        arrow = arrow,
        backgroundColor = Color(0xFFD0D0FF),
        cornerRadius = 8.dp,
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = text,
        )
    }
}
