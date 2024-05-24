package com.svenjacobs.reveal.android.tests.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		content = content,
	)
}
