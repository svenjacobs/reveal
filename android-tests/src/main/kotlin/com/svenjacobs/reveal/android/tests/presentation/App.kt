package com.svenjacobs.reveal.android.tests.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.android.tests.presentation.theme.AppTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        MainScreen(modifier = modifier)
    }
}
