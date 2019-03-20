package bulma

import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable


interface Controller<Data, Element : BulmaElement> : BulmaElement {

    var data: Data

    val container: Element

    override val root: HTMLElement get() = container.root

    fun refresh()
}

class ColumnsController<Data, Element: BulmaElement>(
    initialList: List<Data>,
    override val container: Columns = Columns().apply { multiline = true },
    val columnSize: ColumnSize = ColumnSize.None,
    val controllerBuilder: (Int, Data) -> Controller<Data, Element>
) : Controller<List<Data>, Columns> {

    override var data: List<Data> by observable(initialList) { _, _, _ -> refresh() }

    private var controllers: MutableList<Controller<Data, Element>> = mutableListOf()

    val dataControllers: List<Controller<Data, Element>> get() = controllers

    init {
        refresh()
    }

    // TODO implements controller re-use
    override fun refresh() {
        controllers = data.mapIndexed { index, data -> controllerBuilder(index, data) }.toMutableList()
        container.columns = controllers.map { column(it.container, size = columnSize) }
    }
}

