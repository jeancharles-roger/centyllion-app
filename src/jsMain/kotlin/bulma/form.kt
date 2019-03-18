package bulma

import kotlinx.html.InputType
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.input
import kotlinx.html.js.label
import kotlinx.html.js.p
import org.w3c.dom.HTMLElement
import kotlin.browser.document

interface FieldElement : BulmaElement

/** [Field](https://bulma.io/documentation/form/general) */
class Field(initialBody: List<FieldElement> = emptyList()): BulmaElement {
    override val root: HTMLElement = document.create.div("field")

    var body by bulmaList(initialBody, root)
}

class Label(initialText: String): FieldElement {
    override val root: HTMLElement = document.create.label("label") {
        +initialText
    }

    var text = initialText
        set(value) {
            if (value != field) {
                field = value
                root.innerText = field
            }
        }
}

class Help(initialText: String): FieldElement {
    override val root: HTMLElement = document.create.p("help") {
        +initialText
    }

    var text = initialText
        set(value) {
            if (value != field) {
                field = value
                root.innerText = field
            }
        }

    var color by className(ElementColor.None, root)

}

interface ControlElement : BulmaElement

/** [Control](https://bulma.io/documentation/form/general/#form-control) */
class Control(initialElement: ControlElement): FieldElement {
    override val root: HTMLElement = document.create.div("control")

    var body by bulma(initialElement, root)

    /** Left [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var leftIcon: Icon? = null
        set(value) {
            if (value != field) {
                // removes previous if any
                val previousIcon = root.querySelector("span.is-left")
                if (previousIcon != null) root.removeChild(previousIcon)

                // sets the has-icons-left class
                root.classList.toggle("has-icons-left", value != null)

                if (value != null) {
                    // prepares the new icon
                    value.root.classList.toggle("is-left", true)
                    // adds the new icon
                    root.append(value.root)
                }
                field = value
            }
        }

    /** Right [Icon](https://bulma.io/documentation/form/general/#with-icons) */
}

/** [Input](https://bulma.io/documentation/form/input/) */
class Input: ControlElement {
    override val root: HTMLElement = document.create.input(InputType.text, classes = "input")

    var value by attribute("", "value", root)

    var placeholder by attribute("", "placeholder", root)

    // TODO support input type (text, password, email, tel)

    var color by className(ElementColor.None, root)
}
