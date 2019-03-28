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

class MultipleController<
        Data, Context, ParentElement: BulmaElement, ItemElement: BulmaElement,
        Ctrl : Controller<Data, Context, ItemElement>
>(
    initialList: List<Data>, initialContext: Context,
    val header: List<ItemElement>,
    override val container: ParentElement,
    val controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl,
    val updateParent: (parent: ParentElement, items: List<ItemElement>) -> Unit
) : Controller<List<Data>, Context, ParentElement> {

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

    var onClick: (Data, Ctrl) -> Unit = { _, _ -> }

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
                val newController = controllerBuilder(i, d, previous)
                if (previous == null) {
                    newController.root.onclick = {
                        onClick(newController.data, newController)
                    }
                }
                newController
            }
        }
        updateParent(container, header + controllers.map { it.container })
    }

    override fun refresh() {
        controllers.forEach { it.refresh() }
    }
}

fun <Data, Context, Ctrl : Controller<Data, Context, Column>> columnsController(
    initialList: List<Data>, initialContext: Context,
    header: List<Column> = emptyList(),
    container: Columns = Columns().apply { multiline = true },
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = MultipleController<Data, Context, Columns, Column, Ctrl>(
    initialList, initialContext, header, container, controllerBuilder) { parent, items ->
    parent.columns = items
}

fun <Data, Ctrl : Controller<Data, Unit, Column>> noContextColumnsController(
    initialList: List<Data>,
    container: Columns = Columns().apply { multiline = true },
     header: List<Column> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = columnsController(initialList, Unit, header, container, controllerBuilder)
