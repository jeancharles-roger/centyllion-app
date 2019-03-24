package bulma

import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable


interface Controller<Data, Element : BulmaElement> : BulmaElement {

    var data: Data

    val container: Element

    override val root: HTMLElement get() = container.root

    fun refresh()
}

class ColumnsController<Data, Ctrl : Controller<Data, Column>>(
    initialList: List<Data>,
    override val container: Columns = Columns().apply { multiline = true },
    val controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) : Controller<List<Data>, Columns> {

    override var data: List<Data> by observable(initialList) { _, old, new ->
        if (old != new) refresh()
    }

    private var controllers: List<Ctrl> = listOf()

    val dataControllers: List<Ctrl> get() = controllers

    init {
        refresh()
    }

    override fun refresh() {
        // constructs a resized controllers list to match new size and populates with controllers that haven't changed nor moved
        val resizedControllers = List(data.size) { controllers.getOrNull(it) }
            .zip(data).mapIndexed { i, (c, d) -> if (c != null && c.data == d) c else null }

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
        container.columns = controllers.map { it.container }
    }
}

