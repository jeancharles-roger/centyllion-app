import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.singleWindowApplication
import com.centyllion.ui.App
import com.centyllion.ui.AppState
import com.centyllion.ui.MenuBar
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope


class JvmAppState(
    val window: ComposeWindow,
    scope: CoroutineScope,
    pathState: MutableState<PlatformFile?>,
): AppState(scope, pathState) {


    override fun updateName() {
        super.updateName()
        window.title = buildString {
            append("Centyllion - ")
            if (path != null) append(path?.name)
            else append("<not saved>")
        }
    }

}

fun main() = singleWindowApplication {
    val scope = rememberCoroutineScope()
    val appState = remember { JvmAppState(window, scope, mutableStateOf(null)) }
    MenuBar(appState)
    App(appState)
}
