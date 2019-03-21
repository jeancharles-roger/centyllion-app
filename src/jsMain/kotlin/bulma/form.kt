package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onInputFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.InputEvent
import kotlin.browser.document

interface FieldElement : BulmaElement

/** [Field](https://bulma.io/documentation/form/general) */
class Field(initialBody: List<FieldElement> = emptyList()) : BulmaElement {
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

fun simpleField(label: Label? = null, element: FieldElement, help: Help? = null) =
    Field(listOfNotNull(label, element, help))

/** [Horizontal Field](https://bulma.io/documentation/form/general/#horizontal-form) */
class HorizontalField(initialLabel: Label, initialBody: List<Field> = emptyList()) : BulmaElement {

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

class Label(initialText: String = "") : FieldElement {
    override val root: HTMLElement = document.create.label("label") {
        +initialText
    }
}

class Value(initialText: String = "") : FieldElement {
    override val root: HTMLElement = document.create.span("value") {
        +initialText
    }
}

class Help(initialText: String = "") : FieldElement {
    override val root: HTMLElement = document.create.p("help") {
        +initialText
    }

    var color by className(ElementColor.None, root)
}

interface ControlElement : BulmaElement

/** [Control](https://bulma.io/documentation/form/general/#form-control) */
class Control(initialElement: ControlElement) : FieldElement {
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
class Input(onChange: (event: InputEvent, value: String) -> Unit = { _, _ -> }) : ControlElement {

    override val root: HTMLElement = document.create.input(InputType.text, classes = "input") {
        onInputFunction = {
            val target = it.target
            if (it is InputEvent && target is HTMLInputElement) {
                onChange(it, target.value)
            }
        }
    }

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

/** [Text Area](https://bulma.io/documentation/form/textarea). */
class TextArea(onChange: (event: InputEvent, value: String) -> Unit = { _, _ -> }) : FieldElement {
    override val root: HTMLElement = document.create.textArea(classes = "textarea") {
        onInputFunction = {
            val target = it.target
            if (it is InputEvent && target is HTMLTextAreaElement) {
                onChange(it, target.value)
            }
        }
    }

    var rows by attribute("", "rows", root)

    var value: String = ""
        set(value) {
            if (value != field) {
                field = value
                root.innerText = field
            }
        }

    var placeholder by attribute("", "placeholder", root)

    var color by className(ElementColor.None, root)

    var size by className(Size.None, root)

    var fixedSize by className(false, "has-fixed-size", root)

    var readonly by booleanAttribute(false, "readonly", root)

    var disabled by booleanAttribute(false, "disabled", root)
}

class Option(initialText: String) : BulmaElement {
    override val root: HTMLElement = document.create.option {
        +initialText
    }
}

/** [Select](http://bulma.io/documentation/form/select/) */
class Select(initialOptions: List<Option>, onChange: (event: InputEvent, value: Option) -> Unit = { _, _ -> }) : ControlElement {
    override val root: HTMLElement = document.create.div("select") {
        select() {
            onInputFunction = {
                val target = it.target
                if (it is InputEvent && target is HTMLSelectElement) {
                    // TODO support multiple
                    onChange(it, options[target.selectedIndex])
                }
            }
        }
    }

    private val selectNode = root.querySelector("select") as HTMLElement

    var options by bulmaList(initialOptions, root)

    var color by className(ElementColor.None, root)

    var size by className(Size.None, root)

    var rounded by className(false, "is-rounded", root)

    var loading by className(false, "is-loading", root)

    var multiple
        get() = rootMultiple
        set(value) {
            rootMultiple = value
            selectMultiple = value
        }

    private var rootMultiple by className(false, "is-multiple", root)
    private var selectMultiple by booleanAttribute(false, "multiple", selectNode)

}

/** [Checkbox](https://bulma.io/documentation/form/checkbox) */
class Checkbox(initialText: String, onChange: (event: InputEvent, value: Boolean) -> Unit = { _, _ -> }) : BulmaElement {
    override val root: HTMLElement = document.create.label("checkbox") {
        input(type = InputType.checkBox) {
            onInputFunction = {
                val target = it.target
                if (it is InputEvent && target is HTMLInputElement) {
                    onChange(it, target.checked)
                }
            }
        }
        +initialText
    }

    private val inputNode = root.querySelector("checkbox") as HTMLElement

    var disable
        get() = disabledRoot
        set(_) {
            disabledInput = true
            disabledRoot = true
        }

    private var disabledInput by booleanAttribute(false, "disabled", inputNode)
    private var disabledRoot by booleanAttribute(false, "disabled", inputNode)

}


// TODO radio groups http://bulma.io/documentation/form/radio/

// TODO file https://bulma.io/documentation/form/file/
