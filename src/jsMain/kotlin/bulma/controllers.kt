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

    var readOnly: Boolean

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
    val controllerBuilder: (MultipleController<Data, Context, ParentElement, ItemElement, Ctrl>, Data) -> Ctrl,
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

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            controllers.forEach { it.readOnly = new }
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
                    val newController = controllerBuilder(this, it.element)
                    newController.root.onclick = { onClick(newController.data, newController) }
                    newController.readOnly = readOnly
                    newControllers.add(it.index, newController)
                }
                DiffAction.Removed -> {
                    newControllers.removeAt(it.index)
                }
                DiffAction.Replaced -> {
                    val controller = newControllers[it.index]
                    controller.data = it.element
                    newControllers[it.index] = controller
                }
            }
        }



        controllers = newControllers
        updateParent(container, header + controllers.map { it.container } + footer)
    }

    override fun refresh() {
        controllers.forEach { it.refresh() }
    }

    fun indexOf(controller: Ctrl) = controllers.indexOf(controller)
}

fun <Data, Context, Ctrl : Controller<Data, Context, Column>> columnsController(
    initialList: List<Data>, initialContext: Context,
    header: List<Column> = emptyList(), footer: List<Column> = emptyList(),
    container: Columns = Columns().apply { multiline = true },
    controllerBuilder: (MultipleController<Data, Context, Columns, Column, Ctrl>, data: Data) -> Ctrl
) = MultipleController(
    initialList, initialContext, header, footer, container, controllerBuilder
) { parent, items -> parent.columns = items }

fun <Data, Ctrl : Controller<Data, Unit, Column>> noContextColumnsController(
    initialList: List<Data>,
    container: Columns = Columns().apply { multiline = true },
    header: List<Column> = emptyList(), footer: List<Column> = emptyList(),
    controllerBuilder: (MultipleController<Data, Unit, Columns, Column, Ctrl>, data: Data) -> Ctrl
) = columnsController(initialList, Unit, header, footer, container, controllerBuilder)

fun <Data, Context, Ctrl : Controller<Data, Context, DropdownItem>> dropdownController(
    container: Dropdown, initialList: List<Data>, initialContext: Context,
    header: List<DropdownItem> = emptyList(), footer: List<DropdownItem> = emptyList(),
    controllerBuilder: (MultipleController<Data, Context, Dropdown, DropdownItem, Ctrl>, data: Data) -> Ctrl
) = MultipleController(
    initialList, initialContext, header, footer, container, controllerBuilder
) { parent, items -> parent.items = items }

fun <Data, Ctrl : Controller<Data, Unit, DropdownItem>> noContextDropdownController(
    container: Dropdown, initialList: List<Data>,
    header: List<DropdownItem> = emptyList(), footer: List<DropdownItem> = emptyList(),
    controllerBuilder: (MultipleController<Data, Unit, Dropdown, DropdownItem, Ctrl>, data: Data) -> Ctrl
) = dropdownController(container, initialList, Unit, header, footer, controllerBuilder)

fun <Data, Context, Ctrl : Controller<Data, Context, PanelItem>> panelController(
    container: Panel, initialList: List<Data>, initialContext: Context,
    header: List<PanelItem> = emptyList(), footer: List<PanelItem> = emptyList(),
    controllerBuilder: (MultipleController<Data, Context, Panel, PanelItem, Ctrl>, data: Data) -> Ctrl
) = MultipleController(
    initialList, initialContext, header, footer, container, controllerBuilder
) { parent, items -> parent.items = items }

fun <Data, Ctrl : Controller<Data, Unit, PanelItem>> noContextPanelController(
    container: Panel, initialList: List<Data>,
    header: List<PanelItem> = emptyList(), footer: List<PanelItem> = emptyList(),
    controllerBuilder: (MultipleController<Data, Unit, Panel, PanelItem, Ctrl>, data: Data) -> Ctrl
) = panelController(container, initialList, Unit, header, footer, controllerBuilder)
