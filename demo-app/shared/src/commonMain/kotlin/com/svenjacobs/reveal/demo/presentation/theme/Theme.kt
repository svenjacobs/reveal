package com.svenjacobs.reveal.demo.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DemoTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		content = content,
	)
}
