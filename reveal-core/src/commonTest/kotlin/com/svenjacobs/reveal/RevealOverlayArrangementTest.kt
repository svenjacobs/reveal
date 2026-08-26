package com.svenjacobs.reveal

import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the geometry contract of [RevealOverlayArrangement.arrange]: the returned layout area
 * must always lie within the available space and never have a negative width or height, so that its
 * dimensions can be passed to `Constraints` unchecked.
 *
 * Regression tests for issue #376: [computeArea] inflates the reveal area by the revealable's
 * padding (8dp by default) and, for [RevealShape.Circle], by the circle expansion around its
 * centre, without clamping to the overlay bounds. A revealable flush with a window edge therefore
 * produces a reveal area that extends past that edge, and arranging overlay content on the outside
 * of that edge used to return an inverted rect whose negative dimension crashed `Constraints`.
 */
class RevealOverlayArrangementTest {

    @Test
    fun arrangeReturnsUnchangedGeometryForAnInBoundsRevealable() {
        val revealable = IntRect(left = 400, top = 800, right = 600, bottom = 1000)

        // Clamping must be a no-op while the reveal area is fully inside the space.
        assertEquals(
            IntRect(left = 0, top = 800, right = 400, bottom = 1000),
            RevealOverlayArrangement.Start.arrange(revealable, SPACE, true, LayoutDirection.Ltr),
        )
        assertEquals(
            IntRect(left = 0, top = 0, right = 400, bottom = 2000),
            RevealOverlayArrangement.Start.arrange(revealable, SPACE, false, LayoutDirection.Ltr),
        )
        assertEquals(
            IntRect(left = 600, top = 800, right = 1000, bottom = 1000),
            RevealOverlayArrangement.Start.arrange(revealable, SPACE, true, LayoutDirection.Rtl),
        )
        assertEquals(
            IntRect(left = 600, top = 800, right = 1000, bottom = 1000),
            RevealOverlayArrangement.End.arrange(revealable, SPACE, true, LayoutDirection.Ltr),
        )
        assertEquals(
            IntRect(left = 0, top = 800, right = 400, bottom = 1000),
            RevealOverlayArrangement.End.arrange(revealable, SPACE, true, LayoutDirection.Rtl),
        )
        assertEquals(
            IntRect(left = 400, top = 0, right = 600, bottom = 800),
            RevealOverlayArrangement.Top.arrange(revealable, SPACE, true),
        )
        assertEquals(
            IntRect(left = 0, top = 0, right = 1000, bottom = 800),
            RevealOverlayArrangement.Top.arrange(revealable, SPACE, false),
        )
        assertEquals(
            IntRect(left = 400, top = 1000, right = 600, bottom = 2000),
            RevealOverlayArrangement.Bottom.arrange(revealable, SPACE, true),
        )
        assertEquals(
            IntRect(left = 0, top = 1000, right = 1000, bottom = 2000),
            RevealOverlayArrangement.Bottom.arrange(revealable, SPACE, false),
        )
    }

    @Test
    fun arrangeCollapsesTheLayoutAreaWhenTheRevealableCrossesTheArrangedEdge() {
        // Top edge: nothing above a revealable whose inflated area starts above the space.
        val topFlush = IntRect(left = 400, top = -20, right = 600, bottom = 180)
        assertEquals(
            IntRect(left = 400, top = 0, right = 600, bottom = 0),
            RevealOverlayArrangement.Top.arrange(topFlush, SPACE, true),
        )

        // Bottom edge.
        val bottomFlush = IntRect(left = 400, top = 1900, right = 600, bottom = 2020)
        assertEquals(
            IntRect(left = 400, top = 2000, right = 600, bottom = 2000),
            RevealOverlayArrangement.Bottom.arrange(bottomFlush, SPACE, true),
        )

        // Start edge (Ltr), and the same area arranged Rtl, where End is the one that collapses.
        val startFlush = IntRect(left = -20, top = 800, right = 180, bottom = 1000)
        assertEquals(
            IntRect(left = 0, top = 800, right = 0, bottom = 1000),
            RevealOverlayArrangement.Start.arrange(startFlush, SPACE, true, LayoutDirection.Ltr),
        )
        assertEquals(
            IntRect(left = 0, top = 800, right = 0, bottom = 1000),
            RevealOverlayArrangement.End.arrange(startFlush, SPACE, true, LayoutDirection.Rtl),
        )

        // End edge (Ltr).
        val endFlush = IntRect(left = 900, top = 800, right = 1020, bottom = 1000)
        assertEquals(
            IntRect(left = 1000, top = 800, right = 1000, bottom = 1000),
            RevealOverlayArrangement.End.arrange(endFlush, SPACE, true, LayoutDirection.Ltr),
        )
        assertEquals(
            IntRect(left = 1000, top = 800, right = 1000, bottom = 1000),
            RevealOverlayArrangement.Start.arrange(endFlush, SPACE, true, LayoutDirection.Rtl),
        )
    }

    @Test
    fun arrangeClampsTheConfinedCrossAxisToTheSpace() {
        // A revealable crossing the top edge still has room beside it, but the confined cross axis
        // is clamped to the visible part of the reveal area rather than starting off-screen.
        val topFlush = IntRect(left = 400, top = -20, right = 600, bottom = 180)
        assertEquals(
            IntRect(left = 600, top = 0, right = 1000, bottom = 180),
            RevealOverlayArrangement.End.arrange(topFlush, SPACE, true, LayoutDirection.Ltr),
        )

        val bottomFlush = IntRect(left = 400, top = 1900, right = 600, bottom = 2020)
        assertEquals(
            IntRect(left = 0, top = 1900, right = 400, bottom = 2000),
            RevealOverlayArrangement.Start.arrange(bottomFlush, SPACE, true, LayoutDirection.Ltr),
        )

        val startFlush = IntRect(left = -20, top = 800, right = 180, bottom = 1000)
        assertEquals(
            IntRect(left = 0, top = 0, right = 180, bottom = 800),
            RevealOverlayArrangement.Top.arrange(startFlush, SPACE, true),
        )
    }

    @Test
    fun arrangeStaysWithinTheSpaceForEveryArrangementAndRevealable() {
        val revealables = listOf(
            // Fully inside.
            IntRect(left = 400, top = 800, right = 600, bottom = 1000),
            // Crossing each edge.
            IntRect(left = 400, top = -20, right = 600, bottom = 180),
            IntRect(left = 400, top = 1900, right = 600, bottom = 2020),
            IntRect(left = -20, top = 800, right = 180, bottom = 1000),
            IntRect(left = 900, top = 800, right = 1020, bottom = 1000),
            // Entirely outside, as for a revealable scrolled out of the window.
            IntRect(left = -500, top = -500, right = -100, bottom = -100),
            IntRect(left = 1100, top = 2100, right = 1500, bottom = 2500),
            // Larger than the space in both axes.
            IntRect(left = -100, top = -100, right = 1100, bottom = 2100),
        )

        for (revealable in revealables) {
            for ((name, rect) in allArrangements(revealable, SPACE)) {
                assertValid(rect = rect, space = SPACE, description = "$name for $revealable")
            }
        }
    }

    private fun allArrangements(revealable: IntRect, space: IntSize): List<Pair<String, IntRect>> =
        buildList {
            for (confine in listOf(true, false)) {
                for (direction in listOf(LayoutDirection.Ltr, LayoutDirection.Rtl)) {
                    add(
                        "Start(confine=$confine, $direction)" to
                            RevealOverlayArrangement.Start
                                .arrange(revealable, space, confine, direction),
                    )
                    add(
                        "End(confine=$confine, $direction)" to
                            RevealOverlayArrangement.End
                                .arrange(revealable, space, confine, direction),
                    )
                }
                add(
                    "Top(confine=$confine)" to
                        RevealOverlayArrangement.Top.arrange(revealable, space, confine),
                )
                add(
                    "Bottom(confine=$confine)" to
                        RevealOverlayArrangement.Bottom.arrange(revealable, space, confine),
                )
            }
        }

    private fun assertValid(rect: IntRect, space: IntSize, description: String) {
        assertTrue(rect.width >= 0, "$description: width must not be negative, but was $rect")
        assertTrue(rect.height >= 0, "$description: height must not be negative, but was $rect")
        assertTrue(rect.left >= 0, "$description: left must be within the space, but was $rect")
        assertTrue(rect.top >= 0, "$description: top must be within the space, but was $rect")
        assertTrue(
            rect.right <= space.width,
            "$description: right must be within the space, but was $rect",
        )
        assertTrue(
            rect.bottom <= space.height,
            "$description: bottom must be within the space, but was $rect",
        )
    }

    private companion object {
        val SPACE = IntSize(width = 1000, height = 2000)
    }
}
