package com.svenjacobs.reveal.demo.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.svenjacobs.reveal.demo.presentation.theme.DemoTheme

private enum class Screen { Main, MultipleRevealables }

@Composable
fun App(modifier: Modifier = Modifier) {
    var screen by remember { mutableStateOf(Screen.Main) }

    DemoTheme {
        when (screen) {
            Screen.Main -> MainScreen(
                modifier = modifier,
                onShowMultipleRevealables = { screen = Screen.MultipleRevealables },
            )

            Screen.MultipleRevealables -> MultipleRevealablesScreen(
                modifier = modifier,
                onBack = { screen = Screen.Main },
            )
        }
    }
}
