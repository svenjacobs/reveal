package com.svenjacobs.reveal

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Holds the position of the current reveal area relative to the overlay content that is aligned next
 * to it via [RevealOverlayScope.align].
 *
 * Values are expressed as a **center-relative offset in pixels**: the signed distance from the
 * reveal area center to the outer center of the aligned composable along the relevant axis. A
 * consumer converts this to a shape-local coordinate by adding half the shape size on that axis.
 *
 * This is provided through [LocalRevealOverlayArrowAnchor] so that overlay content (for instance a
 * balloon with an arrow) can position a pointer towards the center of the reveal area, even when the
 * content was shifted to stay within the screen boundaries.
 *
 * - [offsetX] is the horizontal center-relative offset and is set when the content is aligned via
 *   [RevealOverlayArrangement.Vertical] (above or below the reveal area).
 * - [offsetY] is the vertical center-relative offset and is set when the content is aligned via
 *   [RevealOverlayArrangement.Horizontal] (to the start or end of the reveal area).
 *
 * A value is `null` while it is unknown or not applicable for the current arrangement.
 */
@Stable
public class RevealOverlayArrowAnchor internal constructor() {

	public var offsetX: Float? by mutableStateOf(null)
		internal set

	public var offsetY: Float? by mutableStateOf(null)
		internal set
}

/**
 * Provides the [RevealOverlayArrowAnchor] of the currently aligned overlay content. Returns `null`
 * when accessed outside of overlay content provided to a reveal effect.
 *
 * @see RevealOverlayArrowAnchor
 */
public val LocalRevealOverlayArrowAnchor: ProvidableCompositionLocal<RevealOverlayArrowAnchor?> =
	compositionLocalOf { null }
