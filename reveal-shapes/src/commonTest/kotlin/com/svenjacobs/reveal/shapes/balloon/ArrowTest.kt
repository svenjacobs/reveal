package com.svenjacobs.reveal.shapes.balloon

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for the corner-safe clamping of the dynamically anchored arrow (issue #194).
 */
class ArrowTest {

    // available=200, arrowLength=20, cornerRadius=8, margin=4
    // min = cornerRadius + margin = 12
    // max = available - arrowLength - cornerRadius - margin = 200 - 20 - 8 - 4 = 168

    @Test
    fun centerWithinRangeMapsToLeadingEdge() {
        // center 100 -> leading edge 100 - 10 = 90 (within [12, 168])
        assertEquals(
            expected = 90f,
            actual = clamp(center = 100f),
        )
    }

    @Test
    fun clampsToStartCorner() {
        // center near the start edge would place the arrow over the corner -> clamp to min
        assertEquals(
            expected = 12f,
            actual = clamp(center = 0f),
        )
    }

    @Test
    fun clampsToEndCorner() {
        // center near the end edge would place the arrow over the corner -> clamp to max
        assertEquals(
            expected = 168f,
            actual = clamp(center = 200f),
        )
    }

    @Test
    fun degenerateSpaceFallsBackToMin() {
        // When the bubble is too small for the margins, max < min, so the leading edge is min.
        assertEquals(
            expected = 12f,
            actual = Arrow.clampArrowOffset(
                center = 25f,
                arrowLength = 20f,
                available = 30f,
                cornerRadius = 8f,
                margin = 4f,
            ),
        )
    }

    private fun clamp(center: Float): Float = Arrow.clampArrowOffset(
        center = center,
        arrowLength = 20f,
        available = 200f,
        cornerRadius = 8f,
        margin = 4f,
    )
}
