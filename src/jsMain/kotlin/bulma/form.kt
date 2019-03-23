package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onInputFunction
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.InputEvent
import kotlin.browser.document

interface FieldElement : BulmaElement

/** [Field](https://bulma.io/documentation/form/general) */
class Field(
    vararg body: FieldElement, narrow: Boolean = false,
    addons: Boolean = false, addonsCentered: Boolean = false,
    addonsRight: Boolean = false, grouped: Boolean = false,
    groupedCentered: Boolean = false, groupedRight: Boolean = false,
    groupedMultiline: Boolean = false
) : BulmaElement {
    override val root: HTMLElement = document.create.div("field")

    var body by bulmaList(body.toList(), root)

    /** Narrow property */
    var narrow by className(narrow, "is-narrow", root)

    /** See https://bulma.io/documentation/form/general/#form-addons */
    var addons by className(addons, "has-addons", root)

    /** See https://bulma.io/documentation/form/general/#form-addons */
    var addonsCentered by className(addonsCentered, "has-addons-centered", root)

    /** See https://bulma.io/documentation/form/general/#form-addons */
    var addonsRight by className(addonsRight, "has-addons-right", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var grouped by className(grouped, "is-grouped", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var groupedCentered by className(groupedCentered, "is-grouped-centered", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var groupedRight by className(groupedRight, "is-grouped-right", root)

    /** See https://bulma.io/documentation/form/general/#form-group */
    var groupedMultiline by className(groupedMultiline, "is-grouped-multiline", root)
}

/** [Horizontal Field](https://bulma.io/documentation/form/general/#horizontal-form) */
class HorizontalField(label: Label, vararg body: Field) : BulmaElement {

    override val root: HTMLElement = document.create.div("field is-horizontal") {
        div("field-label")
        div("field-body")
    }

    private val labelNode = root.querySelector(".field-label") as HTMLElement
    private val bodyNode = root.querySelector(".field-body") as HTMLElement

    var label by bulma<Label>(label, labelNode)

    var labelSize by className(Size.Normal, labelNode)

    var body by bulmaList(body.toList(), bodyNode)

}

class Label(text: String = "") : FieldElement {
    override val root: HTMLElement = document.create.label("label") {
        +text
    }
}

class Value(text: String = "") : FieldElement {
    override val root: HTMLElement = document.create.span("value") {
        +text
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
class Control(
    element: ControlElement, leftIcon: Icon? = null, rightIcon: Icon? = null, expanded: Boolean = false
) : FieldElement {
    override val root: HTMLElement = document.create.div("control")

    var body by bulma(element, root)

    var expanded by className(expanded, "is-expanded", root)

    /** Left [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var leftIcon: Icon? = leftIcon
        set(value) {
            if (value != field) {
                updateIcon("left", value)
                field = value
            }
        }

    /** Right [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var rightIcon: Icon? = rightIcon
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

    init {
        updateIcon("left", leftIcon)
        updateIcon("right", rightIcon)
    }
}

/** [Input](https://bulma.io/documentation/form/input/) */
class Input(
    value: String = "", placeholder: String = "",
    color: ElementColor = ElementColor.None, size: Size = Size.None,
    rounded: Boolean = false, loading: Boolean = false,
    readonly: Boolean = false, static: Boolean = false,
    var onChange: (event: InputEvent, value: String) -> Unit = { _, _ -> }
) : ControlElement {

    override val root = document.create.input(InputType.text, classes = "input") {
        this.value = value
        onInputFunction = {
            val target = it.target
            if (it is InputEvent && target is HTMLInputElement) {
                onChange(it, target.value)
            }
        }
    } as HTMLInputElement

    var value: String
        get() = root.value
        set(value) { root.value = value }

    var placeholder by attribute(placeholder, "placeholder", root)

    // TODO support input type (text, password, email, tel)

    var color by className(color, root)

    var size by className(size, root)

    var rounded by className(rounded, "is-rounded", root)

    var loading by className(loading, "is-loading", root)

    var disabled by booleanAttribute(false, "disabled", root)

    var readonly by booleanAttribute(readonly, "readonly", root)

    var static by className(static, "is-static", root)

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

    private val optionNode = root as HTMLOptionElement

    var value
        get() = optionNode.value
        set(value) {
            optionNode.value = value
        }

    val index get() = optionNode.index
}

/** [Select](http://bulma.io/documentation/form/select/) */
class Select(
    options: List<Option>, color: ElementColor = ElementColor.None,
    size: Size = Size.None, rounded: Boolean = false,
    loading: Boolean = false, multiple: Boolean = false,
    onChange: (event: Event, value: Option) -> Unit = { _, _ -> }
) : ControlElement {

    override val root: HTMLElement = document.create.div("select") {
        select {
            onInputFunction = {
                val target = it.target
                println("Target -> $target")
                println("it -> $it")
                if (target is HTMLSelectElement) {
                    // TODO support multiple
                    onChange(it, options[target.selectedIndex])
                }
            }
        }
    }

    private val selectNode = root.querySelector("select") as HTMLElement

    var options by bulmaList(options, selectNode)

    var color by className(color, root)

    var size by className(size, root)

    var rounded by className(rounded, "is-rounded", root)

    var loading by className(loading, "is-loading", root)

    var multiple
        get() = rootMultiple
        set(value) {
            rootMultiple = value
            selectMultiple = value
        }

    private var rootMultiple by className(multiple, "is-multiple", root)
    private var selectMultiple by booleanAttribute(multiple, "multiple", selectNode)

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
