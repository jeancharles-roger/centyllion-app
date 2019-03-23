package bulma

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.*
import org.w3c.dom.HTMLElement
import kotlin.browser.document

/** [Box](https://bulma.io/documentation/elements/box) element. */
class Box : BulmaElement {
    override val root: HTMLElement = document.create.div("box")

    var body by bulmaList(emptyList(), root)
}

/** [Button](https://bulma.io/documentation/elements/button) element. */
class Button(
    text: String, icon: Icon? = null,
    color: ElementColor = ElementColor.None,
    rounded: Boolean = false, outlined: Boolean = false,
    inverted: Boolean = false, size: Size = Size.None,
    val onClick: (Button) -> Unit = {}
) : ControlElement {

    override val root: HTMLElement = document.create.a(classes = "button") {
        +text
        onClickFunction = { if (!disabled) onClick(this@Button) }
    }

    /** Left [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var icon: Icon? = icon
        set(value) {
            if (value != field) {
                // removes previous if any
                field?.let { root.removeChild(it.root) }
                field = value
                field?.let { root.appendChild(it.root)}
            }
        }

    var rounded by className(rounded, "is-rounded", root)

    var outlined by className(outlined, "is-outlined", root)

    var inverted by className(inverted, "is-inverted", root)

    var loading by className(inverted, "is-loading", root)

    var color by className(color, root)

    var size by className(size, root)

    var disabled by booleanAttribute(false, "disabled", root)

    init {
        icon?.let { root.appendChild(it.root)}
    }
}

fun iconButton(
    icon: Icon? = null, color: ElementColor = ElementColor.None,
    rounded: Boolean = false, outlined: Boolean = false,
    inverted: Boolean = false, size: Size = Size.None,
    onClick: (Button) -> Unit = {}
) = Button("", icon, color, rounded, outlined, inverted, size, onClick)

fun textButton(
    text: String, color: ElementColor = ElementColor.None,
    rounded: Boolean = false, outlined: Boolean = false,
    inverted: Boolean = false, size: Size = Size.None,
    onClick: (Button) -> Unit = {}
) = Button(text, null, color, rounded, outlined, inverted, size, onClick)

/** [Content](https://bulma.io/documentation/elements/content) element. */
class Content(block: DIV.() -> Unit = {}) : BulmaElement {
    override val root: HTMLElement = document.create.div("content") {
        block()
    }
}

/** [Delete](https://bulma.io/documentation/elements/delete) element. */
class Delete(val onClick: (Delete) -> Unit = {}) : BulmaElement {
    override val root: HTMLElement = document.create.button(classes = "delete") {
        onClickFunction = { if (!disabled) onClick(this@Delete) }
    }

    var size by className(Size.None, root)

    var color by className(ElementColor.None, root)

    var disabled by booleanAttribute(false, "disabled", root)

}

/** [Icon](https://bulma.io/documentation/elements/icon) element. */
class Icon(
    icon: String, size: Size = Size.None, color: TextColor = TextColor.None
) : ControlElement {
    override val root: HTMLElement = document.create.span("icon") {
        i("fas fa-$icon")
    }

    private val iconNode = root.querySelector(".fas") as HTMLElement

    var icon by className(icon, iconNode, "fa-")

    var size
        get() = outerSize
        set(value) {
            outerSize = value
            iconSize = value.toFas()
        }

    var outerSize by className(size, root)

    var iconSize by className(size.toFas(), iconNode)

    var color by className(color, root)

}

/** [Notification](https://bulma.io/documentation/elements/notification) element. */
class Notification(val onDelete: (Notification) -> Unit = {}) : BulmaElement {

    override val root: HTMLElement = document.create.div("notification") {
        button(classes = "delete") {
            onClickFunction = { onDelete(this@Notification) }
        }
    }

    var color by className(ElementColor.None, root)

    var body by bulmaList(emptyList(), root)
}

/** [Progress Bar](https://bulma.io/documentation/elements/progress) element. */
class ProgressBar : BulmaElement {

    override val root: HTMLElement = document.create.progress("progress")

    var color by className(ElementColor.None, root)

    var size by className(Size.None, root)

    var min by intAttribute(null, "min", root)

    var value by intAttribute(null, "value", root)

    var max by intAttribute(null, "max", root)

}

// TODO adds support for Table

/** [Tag](https://bulma.io/documentation/elements/tag) element. */
class Tag(
    text: String, color: ElementColor = ElementColor.None,
    size: Size = Size.None, rounded: Boolean = false
) : BulmaElement {

    override val root: HTMLElement = document.create.span("tag") {
        +text
    }

    var color by className(color, root)

    var size by className(size, root)

    var rounded by className(rounded, "is-rounded", root)

    // TODO adds support for delete button
}

/** [Tags](https://bulma.io/documentation/elements/tag/#list-of-tags) element */
class Tags(tags: List<Tag> = emptyList()) : BulmaElement {

    override val root: HTMLElement = document.create.div("tags")

    var tags by bulmaList<Tag>(tags, root)
}

class Title(text: String, size: TextSize = TextSize.None): BulmaElement {

    override val root: HTMLElement = document.create.h1("title") { +text }

    var size by className(size, root)
}

class SubTitle(text: String, size: TextSize = TextSize.None): BulmaElement {

    override val root: HTMLElement = document.create.h1("subtitle") { +text }

    var size by className(size, root)
}
