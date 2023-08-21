package com.svenjacobs.reveal.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.svenjacobs.reveal.demo.ui.App

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		WindowCompat.setDecorFitsSystemWindows(window, false)

		setContent {
			App()
		}
	}
}
