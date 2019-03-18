package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.HTMLElement
import kotlin.browser.document

sealed class PanelItem(mainClass: String) : BulmaElement {

    var icon: String? = null
        set(value) {
            if (value != field) {
                field = value

                iconI = field?.let {
                    document.create.span("panel-icon") {
                        i("fas $field")
                    }
                }
            }
        }

    private var iconI: HTMLElement? = null
        set(value) {
            if (value != field) {
                field?.let { root.removeChild(it) }
                field = value
                field?.let { root.insertAdjacentElement("afterbegin", it) }
            }
        }

    final override val root: HTMLElement = document.create.nav(mainClass)

    var content by html(document.create.div(), root)

    init {
        root.append(content)
    }
}

class PanelTab : PanelItem("panel-tabs") {

}

class PanelBlock : PanelItem("panel-block")

class Panel : BulmaElement {

    /*
    var heading: HTMLElement = document.create.p("panel-heading")
        set(value) {
            if (value != field) {
                root.removeChild(field)
                field = value
                field.classList.toggle("panel-heading", true)
                root.appendChild(field)
            }
        }
    */

    var blocks: List<PanelItem> = emptyList()
        set(value) {
            if (value != field) {
                field.forEach { root.removeChild(it.root) }
                field = value
                field.forEach { root.appendChild(it.root) }
            }
        }

    override val root: HTMLElement = document.create.nav("panel")

    var heading by html(document.create.p("panel-heading"), root, "panel-heading")

    init {
        root.appendChild(heading)
    }

}
