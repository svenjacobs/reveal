package com.svenjacobs.reveal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density

/**
 * Scope for overlay content which provides Modifiers to align an element relative to the
 * reveal area.
 *
 * @see align
 */
@Immutable
public interface RevealOverlayScope {

    /**
     * Aligns the element horizontally either to the start or end of the reveal area.
     * Additionally the element is vertically aligned in relation to the reveal area via
     * [verticalAlignment]. Set [confineHeight] to `false` to not confine the height to the height
     * of the reveal area. For instance use it with a vertical alignment of [Alignment.Top] to
     * implement a custom alignment.
     *
     * Must be applied to a direct child of the overlay content; the modifier has no effect on
     * elements nested further down the tree. The position within the modifier chain does not
     * matter.
     *
     * @param horizontalArrangement Horizontal arrangement (start, end)
     * @param verticalAlignment Vertical alignment of element in relation to reveal area
     * @param confineHeight Confine height of element to height of reveal area
     *
     * @see RevealOverlayArrangement.Horizontal
     */
    public fun Modifier.align(
        horizontalArrangement: RevealOverlayArrangement.Horizontal,
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        confineHeight: Boolean = true,
    ): Modifier

    /**
     * Aligns the element vertically either to the top or bottom of the reveal area.
     * Additionally the element is horizontally aligned in relation to the reveal area via
     * [horizontalAlignment]. Set [confineWidth] to `false` to not confine the width to the width
     * of the reveal area. For instance use it with a horizontal alignment of [Alignment.Start] to
     * implement a custom alignment.
     *
     * Must be applied to a direct child of the overlay content; the modifier has no effect on
     * elements nested further down the tree. The position within the modifier chain does not
     * matter.
     *
     * @param verticalArrangement Vertical arrangement (top, bottom)
     * @param horizontalAlignment Horizontal alignment in relation to reveal area
     * @param confineWidth Confine width of element to width of reveal area
     *
     * @see RevealOverlayArrangement.Vertical
     */
    public fun Modifier.align(
        verticalArrangement: RevealOverlayArrangement.Vertical,
        horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        confineWidth: Boolean = true,
    ): Modifier
}

internal object RevealOverlayScopeInstance : RevealOverlayScope {

    override fun Modifier.align(
        horizontalArrangement: RevealOverlayArrangement.Horizontal,
        verticalAlignment: Alignment.Vertical,
        confineHeight: Boolean,
    ): Modifier = this.then(
        RevealOverlayAlignElement(
            alignment = RevealOverlayAlignmentHorizontal(
                arrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
                confineHeight = confineHeight,
            ),
        ),
    )

    override fun Modifier.align(
        verticalArrangement: RevealOverlayArrangement.Vertical,
        horizontalAlignment: Alignment.Horizontal,
        confineWidth: Boolean,
    ): Modifier = this.then(
        RevealOverlayAlignElement(
            alignment = RevealOverlayAlignmentVertical(
                arrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                confineWidth = confineWidth,
            ),
        ),
    )
}

/**
 * Parent data attached by [RevealOverlayScope.align], read by [RevealOverlayLayout] to measure and
 * place its children at their real coordinates. Using parent data (rather than [Modifier.layout])
 * ensures that the layout node of an aligned element always reports its actual size and on-screen
 * position, so that semantics/accessibility bounds match what is drawn regardless of the order in
 * which modifiers are applied.
 */
internal sealed interface RevealOverlayAlignment

internal data class RevealOverlayAlignmentHorizontal(
    val arrangement: RevealOverlayArrangement.Horizontal,
    val verticalAlignment: Alignment.Vertical,
    val confineHeight: Boolean,
) : RevealOverlayAlignment

internal data class RevealOverlayAlignmentVertical(
    val arrangement: RevealOverlayArrangement.Vertical,
    val horizontalAlignment: Alignment.Horizontal,
    val confineWidth: Boolean,
) : RevealOverlayAlignment

internal val IntrinsicMeasurable.revealOverlayAlignment: RevealOverlayAlignment?
    get() = parentData as? RevealOverlayAlignment

private data class RevealOverlayAlignElement(val alignment: RevealOverlayAlignment) :
    ModifierNodeElement<RevealOverlayAlignNode>() {

    override fun create(): RevealOverlayAlignNode = RevealOverlayAlignNode(alignment)

    override fun update(node: RevealOverlayAlignNode) {
        node.alignment = alignment
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "align"
        value = alignment
    }
}

private class RevealOverlayAlignNode(var alignment: RevealOverlayAlignment) :
    Modifier.Node(),
    ParentDataModifierNode {

    override fun Density.modifyParentData(parentData: Any?): Any? = alignment
}
