package bulma

import com.centyllion.model.Diff
import com.centyllion.model.DiffAction
import com.centyllion.model.diff
import org.w3c.dom.HTMLElement
import kotlin.properties.Delegates.observable

/** Controller interface that bind a Data to a BulmaElement within a Context */
interface Controller<Data, Context, out Element : BulmaElement> : BulmaElement {

    var data: Data

    var context: Context

    val container: Element

    override val root: HTMLElement get() = container.root

    fun refresh()
}

/** A [Controller] with no context */
abstract class NoContextController<Data, out Element : BulmaElement> : Controller<Data, Unit, Element> {
    override var context: Unit = Unit
}

/** [Controller] that handle a List of [Data]. */
class MultipleController<
        Data, Context, ParentElement : BulmaElement, ItemElement : BulmaElement,
        Ctrl : Controller<Data, Context, ItemElement>
    >(
    initialList: List<Data>, initialContext: Context,
    val header: List<ItemElement>, val footer: List<ItemElement>,
    override val container: ParentElement,
    val controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl,
    val updateParent: (parent: ParentElement, items: List<ItemElement>) -> Unit
) : Controller<List<Data>, Context, ParentElement> {

    override var data: List<Data> by observable(initialList) { _, old, new ->
        if (old != new) {
            refreshControllers(old.diff(new))
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
        refreshControllers(emptyList<Data>().diff(initialList))
    }

    private fun refreshControllers(diff: List<Diff<Data>>) {
        val newControllers = controllers.toMutableList()
        diff.forEach {
            when (it.action) {
                DiffAction.Added -> {
                    val newController = controllerBuilder(it.index, it.element, null)
                    newController.root.onclick = { onClick(newController.data, newController) }
                    newControllers.add(it.index, newController)
                }
                DiffAction.Removed -> {
                    newControllers.removeAt(it.index)
                }
                DiffAction.Replaced -> {
                    val controller = controllerBuilder(it.index, it.element, newControllers[it.index])
                    controller.data = it.element
                    newControllers[it.index] = controller
                }
            }
        }
        controllers = newControllers
        updateParents()
    }

    private fun updateParents() {
        updateParent(container, header + controllers.map { it.container } + footer)
    }

    override fun refresh() {
        controllers.forEach { it.refresh() }
    }
}

fun <Data, Context, Ctrl : Controller<Data, Context, Column>> columnsController(
    initialList: List<Data>, initialContext: Context,
    header: List<Column> = emptyList(), footer: List<Column> = emptyList(),
    container: Columns = Columns().apply { multiline = true },
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = MultipleController(
    initialList, initialContext, header, footer, container, controllerBuilder
) { parent, items -> parent.columns = items }

fun <Data, Ctrl : Controller<Data, Unit, Column>> noContextColumnsController(
    initialList: List<Data>,
    container: Columns = Columns().apply { multiline = true },
    header: List<Column> = emptyList(), footer: List<Column> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = columnsController(initialList, Unit, header, footer, container, controllerBuilder)

fun <Data, Context, Ctrl : Controller<Data, Context, DropdownItem>> dropdownController(
    container: Dropdown, initialList: List<Data>, initialContext: Context,
    header: List<DropdownItem> = emptyList(), footer: List<DropdownItem> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = MultipleController(
    initialList, initialContext, header, footer, container, controllerBuilder
) { parent, items -> parent.items = items }

fun <Data, Ctrl : Controller<Data, Unit, DropdownItem>> noContextDropdownController(
    container: Dropdown, initialList: List<Data>,
    header: List<DropdownItem> = emptyList(), footer: List<DropdownItem> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = dropdownController(container, initialList, Unit, header, footer, controllerBuilder)


fun <Data, Context, Ctrl : Controller<Data, Context, PanelItem>> panelController(
    container: Panel, initialList: List<Data>, initialContext: Context,
    header: List<PanelItem> = emptyList(), footer: List<PanelItem> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = MultipleController(
    initialList, initialContext, header, footer, container, controllerBuilder
) { parent, items -> parent.items = items }

fun <Data, Ctrl : Controller<Data, Unit, PanelItem>> noContextPanelController(
    container: Panel, initialList: List<Data>,
    header: List<PanelItem> = emptyList(), footer: List<PanelItem> = emptyList(),
    controllerBuilder: (index: Int, data: Data, previous: Ctrl?) -> Ctrl
) = panelController(container, initialList, Unit, header, footer, controllerBuilder)
