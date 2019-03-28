package bulma

import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.hr
import kotlinx.html.i
import kotlinx.html.js.div
import kotlinx.html.js.li
import kotlinx.html.js.nav
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.ul
import org.w3c.dom.*
import kotlin.browser.document

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

    var centered by className(centered, "is-centered", root)

    var right by className(right, "is-right", root)

}

// TODO [Card](https://bulma.io/documentation/components/card/) element

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
    rounded: Boolean = false, icon: Icon? = null, dropDownIcon: String = "angle-down"
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
            onClickFunction = { toggleDropdown() }
        }
        div("dropdown-menu") {
            div("dropdown-content")
        }
    }

    private val buttonNode = root.querySelector("button.button") as HTMLButtonElement
    private val titleNode = root.querySelector(".dropdown-title") as HTMLSpanElement
    private val contentNode = root.querySelector(".dropdown-content") as HTMLDivElement

    fun toggleDropdown() {
        root.classList.toggle("is-active")
    }

    override var text: String
        get() = titleNode.innerText
        set(value) {
            titleNode.innerText = value
        }

    var icon = bulma(icon, buttonNode, Position.AfterBegin)

    var items by bulmaList(items.toList(), contentNode) {
        it.root.apply { if (!classList.contains("dropdown-divider")) classList.add("dropdown-item") }
    }

    var rounded by className(rounded, "is-rounded", buttonNode)

}
