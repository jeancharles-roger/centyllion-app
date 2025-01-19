import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.singleWindowApplication
import com.centyllion.ui.App
import com.centyllion.ui.AppState
import com.centyllion.ui.MenuBar
import kotlinx.coroutines.CoroutineScope

class JvmAppState(
    val window: ComposeWindow,
    scope: CoroutineScope,
): AppState(scope) {

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
    val app = remember { JvmAppState(window, scope) }
    MenuBar(app)
    App(app)
}
