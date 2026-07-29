package com.svenjacobs.reveal.demo.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.demo.presentation.theme.DemoTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    DemoTheme {
        MainScreen(modifier = modifier)
    }
}
