package bulma

import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable


interface Controller<Data, Context, Element : BulmaElement> : BulmaElement {

    var data: Data

    var context: Context

    val container: Element

    override val root: HTMLElement get() = container.root

    fun refresh()
}

abstract class NoContextController<Data, Element: BulmaElement>: Controller<Data, Unit, Element> {
    override var context: Unit = Unit
}

class ColumnsController<Data, Context, Ctrl : Controller<Data, Context, Column>>(
    initialList: List<Data>, initialContext: Context,
    val header: List<Column> = emptyList(),
    override val container: Columns = Columns().apply { multiline = true },
    val controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) : Controller<List<Data>, Context, Columns> {

    override var data: List<Data> by observable(initialList) { _, old, new ->
        if (old != new) {
            refreshControllers()
            refresh()
        }
    }

    override var context: Context by observable(initialContext) { _, old, new ->
        if (old != new) {
            controllers.forEach { it.context = new }
        }
    }

    private var controllers: List<Ctrl> = listOf()

    val dataControllers: List<Ctrl> get() = controllers

    init {
        refreshControllers()
    }

    fun refreshControllers() {
        // constructs a resized controllers list to match new size and populates with controllers that haven't changed nor moved
        val resizedControllers = List(data.size) { controllers.getOrNull(it) }
            .zip(data).map { (c, d) -> if (c != null && c.data == d) c else null }

        // gets unused controllers
        val availableControllers = controllers.filter { !resizedControllers.contains(it) }.toMutableList()
        // constructs new controller passing already existing one (only once) if available.
        controllers = resizedControllers.zip(data).mapIndexed { i, (c, d) ->
            c ?: availableControllers.let {
                val previous = availableControllers.find { it.data == d }
                availableControllers.remove(previous)
                controllerBuilder(i, d, previous)
            }
        }
        container.columns = header + controllers.map { it.container }
    }

    override fun refresh() {
        controllers.forEach { it.refresh() }
    }
}

fun <Data, Ctrl : Controller<Data, Unit, Column>> noContextColumnsController(
    initialList: List<Data>,
    container: Columns = Columns().apply { multiline = true },
     header: List<Column> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = ColumnsController(initialList, Unit, header, container, controllerBuilder)
