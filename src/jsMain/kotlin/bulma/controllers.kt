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

    override var data: List<Data> by observable(initialList) { _, _, _ -> refresh() }

    private var controllers: MutableList<Controller<Data, Column>> = mutableListOf()

    val dataControllers: List<Controller<Data, Column>> get() = controllers

    init {
        refresh()
    }

    // TODO implements controller re-use
    override fun refresh() {
        controllers = data.mapIndexed { index, data -> controllerBuilder(index, data) }.toMutableList()
        container.columns = controllers.map { it.container }
    }
}

