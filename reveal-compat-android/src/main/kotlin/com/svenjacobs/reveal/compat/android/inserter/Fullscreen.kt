package com.svenjacobs.reveal.compat.android.inserter

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext

/**
 * Places [content] in a new [ComposeView] which is added to the root content view, hence
 * content is rendered above all other views.
 */
@Composable
internal fun Fullscreen(content: @Composable () -> Unit) {
	val context = LocalContext.current
	val compositionContext = rememberCompositionContext()
	val composeView = remember {
		ComposeView(context).apply {
			id = View.generateViewId()
			layoutParams = ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT,
			)
			setParentCompositionContext(compositionContext)
			setContent(content)
		}
	}

	DisposableEffect(Unit) {
		val container =
			context.findActivity()?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)
				?: throw IllegalStateException("Root content view with ID android.R.id.content not found")

		container.addView(composeView)

		onDispose {
			container.removeView(composeView)
		}
	}
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
	is Activity -> this
	is ContextWrapper -> baseContext.findActivity()
	else -> null
}
