
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.centyllion.ui.App
import com.centyllion.ui.AppState
import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi


@OptIn(ExperimentalComposeUiApi::class, DelicateCoroutinesApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val scope = rememberCoroutineScope()
        val appState = remember { AppState(scope) }
        App(appState)
    }
}