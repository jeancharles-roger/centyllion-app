package bulma

import kotlinx.html.InputType
import kotlinx.html.div
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

    /** Narrow property */
    var narrow by className(false, "is-narrow", root)

    /** See https://bulma.io/documentation/form/general/#form-addons */
    var addons by className(false, "has-addons", root)

    /** See https://bulma.io/documentation/form/general/#form-addons */
    var addonsCentered by className(false, "has-addons-centered", root)

    /** See https://bulma.io/documentation/form/general/#form-addons */
    var addonsRight by className(false, "has-addons-right", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var grouped by className(false, "is-grouped", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var groupedCentered by className(false, "is-grouped-centered", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var groupedRight by className(false, "is-grouped-right", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var groupedMultiline by className(false, "is-grouped-multiline", root)
}

/** [Horizontal Field](https://bulma.io/documentation/form/general/#horizontal-form) */
class HorizontalField(initialLabel: Label, initialBody: List<Field> = emptyList()): BulmaElement {

    override val root: HTMLElement = document.create.div("field is-horizontal") {
        div("field-label")
        div("field-body")
    }

    private val labelNode = root.querySelector(".field-label") as HTMLElement
    private val bodyNode = root.querySelector(".field-body") as HTMLElement

    var label by bulma<Label>(initialLabel, labelNode)

    var labelSize by className(Size.Normal, labelNode)

    var body by bulmaList(initialBody, bodyNode)

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

    var expanded by className(false, "is-expanded", root)

    /** Left [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var leftIcon: Icon? = null
        set(value) {
            if (value != field) {
                updateIcon("left", value)
                field = value
            }
        }

    /** Right [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var rightIcon: Icon? = null
        set(value) {
            if (value != field) {
                updateIcon("right", value)
                field = value
            }
        }

    private fun updateIcon(place: String, icon: Icon?) {
        // removes previous if any
        val previousIcon = root.querySelector("span.is-$place")
        if (previousIcon != null) root.removeChild(previousIcon)

        // sets the has-icons-left class
        root.classList.toggle("has-icons-$place", icon != null)

        if (icon != null) {
            // prepares the new icon
            icon.root.classList.toggle("is-$place", true)
            // adds the new icon
            root.append(icon.root)
        }
    }
}

/** [Input](https://bulma.io/documentation/form/input/) */
class Input: ControlElement {
    override val root: HTMLElement = document.create.input(InputType.text, classes = "input")

    var value by attribute("", "value", root)

    var placeholder by attribute("", "placeholder", root)

    // TODO support input type (text, password, email, tel)

    var color by className(ElementColor.None, root)

    var size by className(Size.None, root)

    var rounded by className(false, "is-rounded", root)

    var loading by className(false, "is-loading", root)

    var disabled by booleanAttribute(false, "disabled", root)

    var readonly by booleanAttribute(false, "readonly", root)

    var static by className(false, "is-static", root)

}
