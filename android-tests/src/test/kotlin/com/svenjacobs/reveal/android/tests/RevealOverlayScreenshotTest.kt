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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealCanvas
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.effect.dim.DimRevealOverlayEffect
import com.svenjacobs.reveal.rememberRevealCanvasState
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import com.svenjacobs.reveal.shapes.balloon.Balloon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot regression tests for issue #194 covering the full matrix of reveal-area positions
 * (3×3 grid) × overlay arrangements (top/bottom/start/end) × arrow modes (anchored / centered).
 *
 * Each case verifies that the balloon stays within the screen and, when `anchorToReveal` is enabled,
 * that the arrow points towards the reveal area while keeping clear of the rounded corners.
 *
 * Record goldens with `./gradlew :android-tests:recordRoborazziDebug` and verify with
 * `./gradlew :android-tests:verifyRoborazziDebug`.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h720dp-mdpi")
class RevealOverlayScreenshotTest(private val case: Case) {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun overlay() {
        lateinit var revealState: RevealState
        lateinit var scope: CoroutineScope

        composeRule.setContent {
            revealState = rememberRevealState()
            scope = rememberCoroutineScope()
            RevealScreenshotContent(case, revealState)
        }

        // Lay out the content first so the revealable is registered, then reveal it.
        composeRule.waitForIdle()
        scope.launch { revealState.reveal(KEY) }
        composeRule.waitForIdle()

        composeRule.onRoot().captureRoboImage("screenshots/${case.id}.png")
    }

    enum class Arrangement { Top, Bottom, Start, End }

    enum class GridPosition(val alignment: Alignment) {
        TopStart(Alignment.TopStart),
        TopCenter(Alignment.TopCenter),
        TopEnd(Alignment.TopEnd),
        CenterStart(Alignment.CenterStart),
        Center(Alignment.Center),
        CenterEnd(Alignment.CenterEnd),
        BottomStart(Alignment.BottomStart),
        BottomCenter(Alignment.BottomCenter),
        BottomEnd(Alignment.BottomEnd),
    }

    data class Case(
        val position: GridPosition,
        val arrangement: Arrangement,
        val anchored: Boolean,
    ) {
        val id: String =
            "${arrangement.name.lowercase()}_${position.name}_${if (anchored) "anchored" else "centered"}"

        override fun toString(): String = id
    }

    companion object {
        const val KEY: String = "target"

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun cases(): List<Case> = buildList {
            for (position in GridPosition.entries) {
                for (arrangement in Arrangement.entries) {
                    for (anchored in listOf(true, false)) {
                        add(Case(position, arrangement, anchored))
                    }
                }
            }
        }
    }
}

@Composable
private fun RevealScreenshotContent(
    case: RevealOverlayScreenshotTest.Case,
    revealState: RevealState,
) {
    val revealCanvasState = rememberRevealCanvasState()

    RevealCanvas(
        revealCanvasState = revealCanvasState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Reveal(
            modifier = Modifier.fillMaxSize(),
            revealCanvasState = revealCanvasState,
            revealState = revealState,
            // Use instant animations so the overlay is fully visible for a deterministic capture.
            overlayEffect = DimRevealOverlayEffect(
                alphaAnimationSpec = snap(),
                contentAlphaAnimationSpec = snap(),
            ),
            overlayContent = { OverlayBalloon(case) },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(case.position.alignment)
                        .padding(24.dp)
                        .size(48.dp)
                        .background(Color.Blue)
                        .revealable(key = RevealOverlayScreenshotTest.KEY),
                )
            }
        }
    }
}

@Composable
private fun RevealOverlayScope.OverlayBalloon(case: RevealOverlayScreenshotTest.Case) {
    val anchored = case.anchored
    val modifier: Modifier
    val arrow: Arrow
    when (case.arrangement) {
        RevealOverlayScreenshotTest.Arrangement.Top -> {
            modifier = Modifier.align(verticalArrangement = RevealOverlayArrangement.Top)
            arrow = Arrow.bottom(anchorToReveal = anchored)
        }

        RevealOverlayScreenshotTest.Arrangement.Bottom -> {
            modifier = Modifier.align(verticalArrangement = RevealOverlayArrangement.Bottom)
            arrow = Arrow.top(anchorToReveal = anchored)
        }

        RevealOverlayScreenshotTest.Arrangement.Start -> {
            modifier = Modifier.align(horizontalArrangement = RevealOverlayArrangement.Start)
            arrow = Arrow.end(anchorToReveal = anchored)
        }

        RevealOverlayScreenshotTest.Arrangement.End -> {
            modifier = Modifier.align(horizontalArrangement = RevealOverlayArrangement.End)
            arrow = Arrow.start(anchorToReveal = anchored)
        }
    }

    Balloon(
        modifier = modifier.padding(8.dp),
        arrow = arrow,
        backgroundColor = Color(0xFFD0D0FF),
        cornerRadius = 8.dp,
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "Explanation text for the revealed item",
        )
    }
}
