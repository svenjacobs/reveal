package com.svenjacobs.reveal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

public sealed interface OnClick {
    /**
     * Clicks on Revealable are handled by the specified handler.
     */
    @Immutable
    public data class Listener(val listener: OnClickListener) : OnClick

    /**
     * Clicks on Revealable are not handled by Reveal and passed through to underlying
     * composables.
     */
    public data object Passthrough : OnClick
}

/**
 * Registers the element as a revealable item.
 *
 * [key] must be unique in the current scope and should be used for [RevealState.reveal].
 * Registration happens from a layout modifier node, so elements are only known to Reveal after
 * they have been laid out.
 *
 * If the element that this modifier is applied to leaves the composition while the reveal
 * effect is shown for the element, the effect is finished.
 *
 * @param key          Unique key to identify the revealable content. Also see documentation of [Key].
 * @param state        Item is associated with this [RevealState].
 * @param shape        Shape of the reveal effect around the element. Defaults to a rounded rect
 *                     with a corner size of 4 dp.
 * @param padding      Additional padding around the reveal area. Positive values increase area
 *                     while negative values decrease it. Defaults to 8 dp on all sides.
 * @param borderStroke Optional border around the revealable item.
 * @param onClick      If `null` clicks will be handled by `onRevealableClick` of `Reveal`.
 *                     If set to `OnClick.Listener` clicks will be handled by this listener.
 *                     If set to `OnClick.Passthrough` Reveal will not intercept clicks and clicks
 *                     will be passed through to underlying composables.
 *
 * @see Key
 */
public fun Modifier.revealable(
    key: Key,
    state: RevealState,
    shape: RevealShape = RevealShape.RoundRect(4.dp),
    padding: PaddingValues = PaddingValues(8.dp),
    borderStroke: BorderStroke? = null,
    onClick: OnClick? = null,
): Modifier = revealable(
    keys = listOf(key),
    state = state,
    shape = shape,
    padding = padding,
    borderStroke = borderStroke,
    onClick = onClick,
)

/**
 * Registers the element as a revealable item.
 *
 * Each key in [keys] must be unique in the current scope and should be used for
 * [RevealState.reveal]. Registration happens from a layout modifier node, so elements are only
 * known to Reveal after they have been laid out.
 *
 * If the element that this modifier is applied to leaves the composition while the reveal
 * effect is shown for the element, the effect is finished.
 *
 * @param keys         Unique keys to identify the revealable content. Also see documentation of [Key].
 * @param state        Item is associated with this [RevealState].
 * @param shape        Shape of the reveal effect around the element. Defaults to a rounded rect
 *                     with a corner size of 4 dp.
 * @param padding      Additional padding around the reveal area. Positive values increase area
 *                     while negative values decrease it. Defaults to 8 dp on all sides.
 * @param borderStroke Optional border around the revealable item.
 * @param onClick      If `null` clicks will be handled by `onRevealableClick` of `Reveal`.
 *                     If set to `OnClick.Listener` clicks will be handled by this listener.
 *                     If set to `OnClick.Passthrough` Reveal will not intercept clicks and clicks
 *                     will be passed through to underlying composables.
 *
 * @see Key
 */
public fun Modifier.revealable(
    vararg keys: Key,
    state: RevealState,
    shape: RevealShape = RevealShape.RoundRect(4.dp),
    padding: PaddingValues = PaddingValues(8.dp),
    borderStroke: BorderStroke? = null,
    onClick: OnClick? = null,
): Modifier = revealable(
    keys = keys.toList(),
    state = state,
    shape = shape,
    padding = padding,
    borderStroke = borderStroke,
    onClick = onClick,
)

/**
 * Registers the element as a revealable item.
 *
 * Each key specified in [keys] must be unique in the current scope and should be used for
 * [RevealState.reveal]. Registration happens from a layout modifier node, so elements are only
 * known to Reveal after they have been laid out.
 *
 * If the element that this modifier is applied to leaves the composition while the reveal
 * effect is shown for the element, the effect is finished.
 *
 * @param keys         Unique keys to identify the revealable content. Also see documentation of [Key].
 * @param state        Item is associated with this [RevealState].
 * @param shape        Shape of the reveal effect around the element. Defaults to a rounded rect
 *                     with a corner size of 4 dp.
 * @param padding      Additional padding around the reveal area. Positive values increase area
 *                     while negative values decrease it. Defaults to 8 dp on all sides.
 * @param borderStroke Optional border around the revealable item.
 * @param onClick      If `null` clicks will be handled by `onRevealableClick` of `Reveal`.
 *                     If set to `OnClick.Listener` clicks will be handled by this listener.
 *                     If set to `OnClick.Passthrough` Reveal will not intercept clicks and clicks
 *                     will be passed through to underlying composables.
 *
 * @see Key
 */
public fun Modifier.revealable(
    keys: Iterable<Key>,
    state: RevealState,
    shape: RevealShape = RevealShape.RoundRect(4.dp),
    padding: PaddingValues = PaddingValues(8.dp),
    borderStroke: BorderStroke? = null,
    onClick: OnClick? = null,
): Modifier = this.then(
    RevealableElement(
        keys = keys.toList(),
        state = state,
        shape = shape,
        padding = padding,
        borderStroke = borderStroke,
        onClick = onClick,
    ),
)

private data class RevealableElement(
    val keys: List<Key>,
    val state: RevealState,
    val shape: RevealShape,
    val padding: PaddingValues,
    val borderStroke: BorderStroke?,
    val onClick: OnClick?,
) : ModifierNodeElement<RevealableNode>() {

    override fun create(): RevealableNode = RevealableNode(
        keys = keys,
        state = state,
        shape = shape,
        padding = padding,
        borderStroke = borderStroke,
        onClick = onClick,
    )

    override fun update(node: RevealableNode) {
        node.update(
            keys = keys,
            state = state,
            shape = shape,
            padding = padding,
            borderStroke = borderStroke,
            onClick = onClick,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "revealable"
        properties["keys"] = keys
        properties["shape"] = shape
        properties["padding"] = padding
        properties["borderStroke"] = borderStroke
        properties["onClick"] = onClick
    }
}

private class RevealableNode(
    private var keys: List<Key>,
    private var state: RevealState,
    private var shape: RevealShape,
    private var padding: PaddingValues,
    private var borderStroke: BorderStroke?,
    private var onClick: OnClick?,
) : Modifier.Node(),
    GlobalPositionAwareModifierNode {

    /**
     * Geometry as of the last layout pass, retained so that registrations can be replayed when
     * [keys] or [state] change without waiting for another one. `null` until first positioned.
     */
    private var layout: Revealable.Layout? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val layout = Revealable.Layout(
            offset = coordinates.positionInRoot(),
            size = coordinates.size.toSize(),
        )
        this.layout = layout
        register(layout)
    }

    override fun onDetach() {
        keys.forEach(state::removeRevealable)
        layout = null
    }

    fun update(
        keys: List<Key>,
        state: RevealState,
        shape: RevealShape,
        padding: PaddingValues,
        borderStroke: BorderStroke?,
        onClick: OnClick?,
    ) {
        val keysChanged = this.keys != keys
        val stateChanged = this.state !== state

        // Drop registrations this node no longer owns. The previous implementation could not do
        // this: its DisposableEffect was keyed on Unit, so changing `keys` on a node that stays in
        // the composition left the old keys behind until the element was disposed.
        if (keysChanged || stateChanged) {
            val obsolete = if (stateChanged) this.keys else this.keys - keys.toSet()
            obsolete.forEach(this.state::removeRevealable)
        }

        this.keys = keys
        this.state = state
        this.shape = shape
        this.padding = padding
        this.borderStroke = borderStroke
        this.onClick = onClick

        // Only a changed key set or state is replayed here; the other properties are picked up by
        // the next layout pass, as they were before. Registering on every update would write to
        // RevealState on every recomposition, and since OnClick.Listener and RevealShape.Custom
        // wrap caller-supplied lambdas that never compare equal, such a write could never settle.
        if (keysChanged || stateChanged) {
            layout?.let(::register)
        }
    }

    private fun register(layout: Revealable.Layout) {
        keys.forEach { key ->
            state.addRevealable(
                Revealable(
                    key = key,
                    shape = shape,
                    padding = padding,
                    borderStroke = borderStroke,
                    layout = layout,
                    onClick = onClick,
                ),
            )
        }
    }
}
