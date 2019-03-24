package bulma

import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable


interface Controller<Data, Element : BulmaElement> : BulmaElement {

    var data: Data

    val container: Element

    override val root: HTMLElement get() = container.root

    fun refresh()
}

class ColumnsController<Data>(
    initialList: List<Data>,
    override val container: Columns = Columns().apply { multiline = true },
    val controllerBuilder: (Int, Data) -> Controller<Data, Column>
) : Controller<List<Data>, Columns> {

    override var data: List<Data> by observable(initialList) { _, old, new ->
        if (old != new) refresh()
    }

    private var controllers: List<Controller<Data, Column>> = listOf()

    val dataControllers: List<Controller<Data, Column>> get() = controllers

    init {
        refresh()
    }

    override fun refresh() {
        // constructs a resized controllers list to match new size
        val resizedControllers = List(data.size) { controllers.getOrNull(it) }
        // reconstructs controllers using the controller at the correct place if it's has the same data
        // or by constructing a new one.
        // TODO it's not possible to change a controller's place since it's given in the constructor
        controllers = data.zip(resizedControllers).mapIndexed { i, (d, c) ->
            if (c == null || c.data != d) controllerBuilder(i, d)  else c
        }
        container.columns = controllers.map { it.container }
    }
}

