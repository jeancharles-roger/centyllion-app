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
        // mapped controllers
        val mapped = controllers.map { it.data to it }.toMap()
        // constructs a resized controllers list to match new size
        val resizedControllers = List(data.size) { controllers.getOrNull(it) }
        // reconstructs controllers using the controller at the correct place if it's has the same data
        // or by constructing a new one.
        controllers = data.zip(resizedControllers).mapIndexed { i, (d, c) ->
            if (c == null || c.data != d) controllerBuilder(i, d, mapped.getOrElse(d) { null }) else c
        }
        container.columns = controllers.map { it.container }
    }
}

