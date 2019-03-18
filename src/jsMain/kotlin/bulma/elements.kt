package bulma

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement
import kotlin.browser.document

class Box: BulmaElement {
    override val root: HTMLElement = document.create.div("box")

    var body by bulmaList(emptyList(), root)
}

class Button(initialText: String, val onClick: (Button) -> Unit = {}) : BulmaElement {

    override val root: HTMLElement = document.create.a(classes = "button") {
        +initialText
        onClickFunction = { if (!disabled) onClick(this@Button) }
    }

    var text = initialText
        set(value) {
            if (value != field) {
                field = value
                root.innerText = field
            }
        }

    var rounded by className(false, "is-rounded", root)

    var outlined by className(false, "is-outlined", root)

    var inverted by className(false, "is-inverted", root)

    var color by className(ElementColor.None, root)

    var size by className(Size.None, root)

    var disabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (field) {
                    root.setAttribute("disabled", "")
                } else {
                    root.removeAttribute("disabled")
                }
            }
        }
}

class Content(block : DIV.() -> Unit = {}): BulmaElement {
    override val root: HTMLElement = document.create.div("content") {
        block()
    }
}

class Delete(val onClick: (Delete) -> Unit = {}) : BulmaElement {
    override val root: HTMLElement = document.create.button (classes = "delete") {
        onClickFunction = { if (!disabled) onClick(this@Delete) }
    }

    var size by className(Size.None, root)

    var color by className(ElementColor.None, root)

    var disabled: Boolean = false
        set(value) {
            if (value != field) {
                field = value
                if (field) {
                    root.setAttribute("disabled", "")
                } else {
                    root.removeAttribute("disabled")
                }
            }
        }
}

class Icon(initialIcon: String) : BulmaElement {
    override val root: HTMLElement = document.create.span ("icon") {
        i("fas fa-$initialIcon")
    }

    private val iconNode = root.querySelector(".fas") as HTMLElement

    var icon by className(initialIcon, iconNode, "fa-")

    var size get() = outerSize
        set(value) {
            outerSize = value
            iconSize = value.toFas()
        }

    var outerSize by className(Size.None, root)

    var iconSize by className(FasSize.None, iconNode)

    var color by className(TextColor.None, root)

}

class Notification(val onDelete: (Notification) -> Unit = {}): BulmaElement {

    override val root: HTMLElement = document.create.div ("notification") {
        button (classes = "delete") {
            onClickFunction = { onDelete(this@Notification) }
        }
    }

    var color by className(ElementColor.None, root)

    var body by bulmaList(emptyList(), root)
}

