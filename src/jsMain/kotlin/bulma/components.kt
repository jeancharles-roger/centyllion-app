package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.li
import kotlinx.html.js.nav
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.properties.Delegates.observable

enum class BreadcrumbSeparator(override val className: String) : HasClassName {
    Default(""), Arrow("has-arrow-separator"), Bullet("has-bullet-separator"),
    Dot("has-dot-separator"), Succeeds("has-succeeds-separator")
}

class BreadcrumbElement(text: String = "", href: String = "", icon: Icon? = null) : BulmaElement {

    override val root: HTMLElement = document.create.li {
        a(href = href) { +text }
    }

    private val aNode = root.querySelector("a") as HTMLAnchorElement

    override var text: String
        get() = aNode.innerText
        set(value) {
            aNode.innerText = value
        }

    var href: String
        get() = aNode.href
        set(value) {
            aNode.href = value
        }

    var icon by bulma(icon, aNode)
}

/** [Breadcrumb](https://bulma.io/documentation/components/breadcrumb) element */
class Breadcrumb(
    vararg body: BreadcrumbElement, separator: BreadcrumbSeparator = BreadcrumbSeparator.Default,
    size: Size = Size.None, centered: Boolean = false, right: Boolean = false
) : BulmaElement {

    override val root: HTMLElement = document.create.nav(classes = "breadcrumb") {
        ul()
    }

    private val ulNode = root.querySelector("ul") as HTMLUListElement

    var body by bulmaList(body.toList(), ulNode)

    var separator by className(separator, root)

    var size by className(size, root)

    var alignment by className(Alignment.Left, root)
}

class CardHeader(val text: String = "", val icon: String? = null)

/** [Card](https://bulma.io/documentation/components/card/) element */
class Card(
    vararg content: BulmaElement,
    header: CardHeader? = null, footer: List<BulmaElement> = emptyList()
): BulmaElement {
    override val root = document.create.div("card")

    var header by html(header, root, Position.AfterBegin) {
        document.create.header("card-header") {
            p("card-header-title") { +it.text }
            span("icon") {
                i("fas fa-${it.icon}") {
                    attributes["aria-hidden"] = "true"
                }
            }
        }
    }

    var content by embeddedBulmaList(content.toList(), root, Position.AfterBegin) {
        document.create.div("card-content")
    }

    var footer by embeddedBulmaList(
        content.toList(), root, Position.BeforeEnd,
        { it.root.apply { classList.add("card-footer-item") } },
        { document.create.footer("card-footer") }
    )
}

interface DropdownItem : BulmaElement

class DropdownSimpleItem(text: String, icon: Icon? = null, onSelect: (DropdownSimpleItem) -> Unit = {}) : DropdownItem {
    override val root = document.create.a(null, "dropdown-item") {
        +text
        onClickFunction = { onSelect(this@DropdownSimpleItem) }
    }

    var icon by bulma(icon, root, Position.AfterBegin)
}

class DropdownContentItem(vararg body: BulmaElement) : DropdownItem {
    override val root = document.create.div("dropdown-item")
    var body by bulmaList(body.toList(), root)
}

class DropdownDivider : DropdownItem {
    override val root = document.create.hr("dropdown-divider")
}

/** [Dropdown](https://bulma.io/documentation/components/dropdown) element */
class Dropdown(
    text: String, vararg items: DropdownItem,
    rounded: Boolean = false, icon: Icon? = null, dropDownIcon: String = "angle-down",
    onDropdown: (Dropdown) -> Unit = {}
) : ControlElement {

    override val root: HTMLElement = document.create.div(classes = "dropdown") {
        div("dropdown-trigger") {
            button(classes = "button") {
                attributes["aria-haspopup"] = "true"
                attributes["aria-controls"] = "dropdown-menu"
                span("dropdown-title") { +text }
                span("icon is-small") {
                    i("fas fa-$dropDownIcon") {
                        attributes["aria-hidden"] = "true"
                    }
                }
            }
            onClickFunction = {
                if (!active) onDropdown(this@Dropdown)
                toggleDropdown()
            }
        }
        div("dropdown-menu") {
            div("dropdown-content")
        }
    }

    private val buttonNode = root.querySelector("button.button") as HTMLButtonElement
    private val titleNode = root.querySelector(".dropdown-title") as HTMLSpanElement
    private val menuNode = root.querySelector(".dropdown-menu") as HTMLDivElement
    private val contentNode = root.querySelector(".dropdown-content") as HTMLDivElement


    var active: Boolean
        get() = root.classList.contains("is-active")
        set(value) { root.classList.toggle("is-active", value)}

    fun toggleDropdown() {
        active = !active
    }

    override var text: String
        get() = titleNode.innerText
        set(value) {
            titleNode.innerText = value
        }

    var icon by bulma(icon, buttonNode, Position.AfterBegin)

    var items by bulmaList(items.toList(), contentNode) {
        it.root.apply { if (!classList.contains("dropdown-divider")) classList.add("dropdown-item") }
    }

    var rounded by className(rounded, "is-rounded", buttonNode)

    var menuSize: String
        get() = menuNode.style.width
        set(value) {
            menuNode.style.width = value
        }
}

// TODO [Menu](http://bulma.io/documentation/components/menu)

/** [Message](http://bulma.io/documentation/components/message) element */
class Message(
    header: List<BulmaElement> = listOf(), body: List<BulmaElement> = listOf(),
    size: Size = Size.None, color: ElementColor = ElementColor.None
) : BulmaElement {

    override val root: HTMLElement = document.create.article("message")

    var header by embeddedBulmaList(header, root, Position.AfterBegin) {
        document.create.div("message-header")
    }

    var body by embeddedBulmaList(body, root, Position.BeforeEnd) {
        document.create.div("message-body")
    }

    var size by className(size, root)

    var color by className(color, root)

}

/** [Modal](https://bulma.io/documentation/components/modal) */
class Modal(vararg content: BulmaElement) : BulmaElement {
    override val root: HTMLElement = document.create.div("modal") {
        div("modal-background") {
            onClickFunction = { active = false }
        }
        div("modal-content")
        div("modal-close is-large") {
            attributes["aria-label"] = "close"
            onClickFunction = { active = false }
        }
    }

    private val contentNode = root.querySelector(".modal-content") as HTMLDivElement

    var active by className(false, "is-active", root)

    var content by bulmaList(content.toList(), contentNode)
}

/** [Modal Card](https://bulma.io/documentation/components/modal) */
class ModalCard(
    text: String,
    body: List<BulmaElement> = listOf(),
    buttons: List<Button> = listOf()
) : BulmaElement {
    override val root: HTMLElement = document.create.div("modal") {
        div("modal-background") {
            onClickFunction = { active = false }
        }
        div("modal-card") {
            header("modal-card-head") {
                p("modal-card-title") { +text }
                button(classes = "delete") {
                    attributes["aria-label"] = "close"
                    onClickFunction = { active = false }
                }
            }
            section("modal-card-body")
            footer("modal-card-foot")
        }
    }

    private val titleNode = root.querySelector("p.modal-card-title") as HTMLElement
    private val bodyNode = root.querySelector("section.modal-card-body") as HTMLElement
    private val footNode = root.querySelector("footer.modal-card-foot") as HTMLElement

    var active by className(false, "is-active", root)

    override var text: String
        get() = titleNode.innerText
        set(value) {
            titleNode.innerText = value
        }

    var body by bulmaList(body, bodyNode)

    var buttons by bulmaList(buttons, footNode)
}

// TODO [NavBar](https://bulma.io/documentation/components/navbar)

// TODO [Pagination](https://bulma.io/documentation/components/pagination)

interface PanelItem : BulmaElement

class PanelTabsItem(text: String, onClick: (PanelTabsItem) -> Unit = {}) : BulmaElement {

    override val root: HTMLElement = document.create.a {
        +text
        onClickFunction = { onClick(this@PanelTabsItem) }
    }

    var active by className(false, "is-active", root)
}

class PanelTabs(vararg items: PanelTabsItem) : PanelItem {

    override val root: HTMLElement = document.create.p("panel-tabs")

    var items by bulmaList(items.toList(), root)
}

class PanelSimpleBlock(text: String, icon: String? = null) : PanelItem {
    override val root: HTMLElement = document.create.div("panel-block") {
        +text
    }

    var icon = html(icon, root, Position.AfterBegin) {
        document.create.span("panel-icon") {
            i("fas fa-$it") {
                attributes["aria-hidden"] = "true"
            }
        }
    }
}

class PanelContentBlock(vararg content: BulmaElement) : PanelItem {
    override val root: HTMLElement = document.create.div("panel-block")

    var content by bulmaList(content.toList(), root)

}

/** [Panel](https://bulma.io/documentation/components/panel) */
class Panel(text: String, vararg items: PanelItem) : BulmaElement {

    override val root: HTMLElement = document.create.nav("panel") {
        p("panel-heading") { +text }
    }

    var items by bulmaList(items.toList(), root)
}

class TabItem(
    text: String, icon: String? = null, var onClick: (TabItem) -> Unit = {}
) : BulmaElement {

    override val root: HTMLElement = document.create.li {
        a { +text }
        onClickFunction = { onClick(this@TabItem) }
    }

    private val aNode = root.querySelector("a") as HTMLElement

    var icon = html(icon, aNode, Position.AfterBegin) {
        document.create.span("icon") {
            i("fas fa-$it") {
                attributes["aria-hidden"] = "true"
            }
        }
    }

    var active by className(false, "is-active", root)
}

/** [Tabs](http://bulma.io/documentation/components/tabs) */
class Tabs(
    vararg items: TabItem, alignment: Alignment = Alignment.Left, size: Size = Size.None,
    boxed: Boolean = false, toggle: Boolean = false, toggleRounded: Boolean = false,
    fullwidth: Boolean = false
) : BulmaElement {

    override val root: HTMLElement = document.create.div("tabs")

    var items by embeddedBulmaList(items.toList(), root, Position.AfterBegin) { document.create.ul() }

    var alignment by className(alignment, root)

    var size by className(size, root)

    var toggle by className(toggle, "is-toggle", root)

    var toggleRounded by className(toggleRounded, "is-toggle-rounded", root)

    var boxed by className(boxed, "is-boxed", root)

    var fullWidth by className(fullwidth, "is-fullwidth", root)
}

class TabPage(val title: TabItem, val body: BulmaElement)

class TabPages(
    vararg pages: TabPage, val tabs: Tabs = Tabs(), initialTabIndex: Int = 0,
    var onTabChange: (page: TabPage) -> Unit = {}
) : BulmaElement {

    override val root: HTMLElement = document.create.div()

    var pages by observable(pages.toList()) { _, _, new ->
        tabs.items = new.map { it.title }
        pagesContent = new.map { it.body }
        preparePages(new)
    }

    private var pagesContent by bulmaList(pages.map { it.body }, root)

    init {
        tabs.apply { items = pages.map { it.title } }
        preparePages(this.pages)
        pages.getOrNull(initialTabIndex)?.let { selectPage(it) }
        root.insertAdjacentElement(Position.AfterBegin.value, tabs.root)
    }

    private fun preparePages(pages: List<TabPage>) {
        pages.map { page ->
            page.title.onClick = { selectPage(page) }
            page.body.hidden = true
        }
    }

    fun selectPage(page: TabPage) {
        pages.forEach {
            it.body.hidden = true
            it.title.active = false
        }

        page.body.hidden = false
        page.title.active = true
        onTabChange(page)
    }

}
