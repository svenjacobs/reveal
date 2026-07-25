package com.svenjacobs.reveal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize

/**
 * Lays out the [content] of an overlay, placing every direct child at the position derived from the
 * [RevealOverlayAlignment] parent data attached via [RevealOverlayScope.align]. Children without
 * that parent data are placed at the top start with loose constraints, matching a plain [Box].
 *
 * Positioning children itself (rather than reporting the full overlay size from within a
 * [Modifier.layout] on each child, as before) ensures that every child's layout node reports its
 * actual size and on-screen position, so that semantics/accessibility bounds match what is drawn.
 */
@Composable
internal fun RevealOverlayLayout(
    revealableRect: IntRect,
    arrowAnchor: RevealOverlayArrowAnchor,
    modifier: Modifier = Modifier,
    content: @Composable RevealOverlayScope.() -> Unit,
) {
    Layout(
        content = { RevealOverlayScopeInstance.content() },
        modifier = modifier,
    ) { measurables, constraints ->
        val space = IntSize(width = constraints.maxWidth, height = constraints.maxHeight)
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        val layoutRects = arrayOfNulls<IntRect>(measurables.size)

        measurables.forEachIndexed { index, measurable ->
            when (val alignment = measurable.revealOverlayAlignment) {
                is RevealOverlayAlignmentHorizontal -> {
                    val layoutSize = alignment.arrangement.arrange(
                        revealable = revealableRect,
                        space = space,
                        confineHeight = alignment.confineHeight,
                        layoutDirection = layoutDirection,
                    )
                    layoutRects[index] = layoutSize
                    // Loose constraints (min = 0): the incoming constraints are fixed to the full
                    // overlay size (RevealOverlayLayout itself uses matchParentSize()), but the
                    // child should only be bounded by, not forced to, the arranged layout area.
                    placeables[index] = measurable.measure(
                        Constraints(maxWidth = layoutSize.width, maxHeight = space.height),
                    )
                }

                is RevealOverlayAlignmentVertical -> {
                    val layoutSize = alignment.arrangement.arrange(
                        revealable = revealableRect,
                        space = space,
                        confineWidth = alignment.confineWidth,
                    )
                    layoutRects[index] = layoutSize
                    placeables[index] = measurable.measure(
                        Constraints(maxWidth = space.width, maxHeight = layoutSize.height),
                    )
                }

                null -> placeables[index] = measurable.measure(
                    Constraints(maxWidth = space.width, maxHeight = space.height),
                )
            }
        }

        layout(space.width, space.height) {
            measurables.forEachIndexed { index, measurable ->
                val placeable = placeables[index] ?: return@forEachIndexed

                when (val alignment = measurable.revealOverlayAlignment) {
                    is RevealOverlayAlignmentHorizontal -> {
                        val layoutSize = layoutRects[index] ?: return@forEachIndexed
                        val x = alignment.arrangement.align(
                            size = placeable.width,
                            layout = layoutSize.width,
                            space = space.width,
                        )
                        val y = layoutSize.top +
                            alignment.verticalAlignment.align(
                                size = placeable.height,
                                space = layoutSize.height,
                            )
                        val placedX = x.coerceWithin(size = placeable.width, space = space.width)
                        val placedY = y.coerceWithin(size = placeable.height, space = space.height)
                        // The content is placed to the side of the reveal area, so the arrow points
                        // horizontally and slides along the vertical axis towards the reveal center.
                        // The offset is stored relative to the composable's outer center so that the
                        // BalloonShape can recover the correct shape-local coordinate via size/2 + offset
                        // without needing to know the caller's outer padding value.
                        arrowAnchor.offsetX = null
                        arrowAnchor.offsetY =
                            (revealableRect.center.y - placedY - placeable.height / 2).toFloat()
                        placeable.placeRelative(x = placedX, y = placedY)
                    }

                    is RevealOverlayAlignmentVertical -> {
                        val layoutSize = layoutRects[index] ?: return@forEachIndexed
                        // Using place() instead of placeRelative() because layoutSize and the value
                        // returned by horizontalAlignment.align() are RTL-aware
                        val x = layoutSize.left +
                            alignment.horizontalAlignment.align(
                                size = placeable.width,
                                space = layoutSize.width,
                                layoutDirection = layoutDirection,
                            )
                        val y = alignment.arrangement.align(
                            size = placeable.height,
                            layout = layoutSize.height,
                            space = space.height,
                        )
                        val placedX = x.coerceWithin(size = placeable.width, space = space.width)
                        val placedY = y.coerceWithin(size = placeable.height, space = space.height)
                        // The content is placed above/below the reveal area, so the arrow points
                        // vertically and slides along the horizontal axis towards the reveal center.
                        // The offset is stored relative to the composable's outer center so that the
                        // BalloonShape can recover the correct shape-local coordinate via size/2 + offset
                        // without needing to know the caller's outer padding value.
                        arrowAnchor.offsetX =
                            (revealableRect.center.x - placedX - placeable.width / 2).toFloat()
                        arrowAnchor.offsetY = null
                        placeable.place(x = placedX, y = placedY)
                    }

                    null -> placeable.place(x = 0, y = 0)
                }
            }
        }
    }
}

/**
 * Coerces this offset so that an element of [size] is fully contained within [space], shifting it
 * back into bounds when it would otherwise overflow the start or end of the available space.
 */
private fun Int.coerceWithin(size: Int, space: Int): Int =
    coerceIn(0, (space - size).coerceAtLeast(0))
