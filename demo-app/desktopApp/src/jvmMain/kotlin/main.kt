import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.svenjacobs.reveal.demo.presentation.App

fun main() = application {
	Window(onCloseRequest = ::exitApplication) {
		App()
	}
}
