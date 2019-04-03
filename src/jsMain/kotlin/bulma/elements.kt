package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import kotlin.browser.document

/** [Box](https://bulma.io/documentation/elements/box) element. */
class Box(vararg body: BulmaElement) : BulmaElement {
    override val root: HTMLElement = document.create.div("box")

    var body by bulmaList(body.toList(), root)
}

/** [Button](https://bulma.io/documentation/elements/button) element. */
class Button(
    title: String? = null, icon: Icon? = null,
    color: ElementColor = ElementColor.None,
    rounded: Boolean = false, outlined: Boolean = false,
    inverted: Boolean = false, size: Size = Size.None,
    val onClick: (Button) -> Unit = {}
) : ControlElement {

    override val root: HTMLElement = document.create.button(classes = "button") {
        onClickFunction = { if (!this@Button.disabled) onClick(this@Button) }
    }

    var title by html(title, root, Position.AfterBegin) { document.create.span { +it } }

    /** Left [Icon](https://bulma.io/documentation/form/general/#with-icons) */
    var icon by bulma(icon, root, Position.AfterBegin)

    var rounded by className(rounded, "is-rounded", root)

    var outlined by className(outlined, "is-outlined", root)

    var inverted by className(inverted, "is-inverted", root)

    var loading by className(inverted, "is-loading", root)

    var color by className(color, root)

    var size by className(size, root)

    var disabled by booleanAttribute(false, "disabled", root)

}

fun iconButton(
    icon: Icon? = null, color: ElementColor = ElementColor.None,
    rounded: Boolean = false, outlined: Boolean = false,
    inverted: Boolean = false, size: Size = Size.None,
    onClick: (Button) -> Unit = {}
) = Button(null, icon, color, rounded, outlined, inverted, size, onClick)

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
        onClickFunction = { if (!this@Delete.disabled) onClick(this@Delete) }
    }

    var size by className(Size.None, root)

    var color by className(ElementColor.None, root)

    var disabled by booleanAttribute(false, "disabled", root)

}

/** [Icon](https://bulma.io/documentation/elements/icon) element. */
class Icon(
    icon: String, size: Size = Size.None, color: TextColor = TextColor.None,
    rotate: FaRotate = FaRotate.None, flip: FaFlip = FaFlip.None, spin: Boolean = false
) : ControlElement {
    override val root: HTMLElement = document.create.span("icon") {
        i("fas fa-$icon") {
            attributes["aria-hidden"] = "true"
        }
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

    var rotate by className(rotate, iconNode)

    var flip by className(flip, iconNode)

    var spin by className(spin, "fa-spin", iconNode)

}

/** [Notification](https://bulma.io/documentation/elements/notification) element. */
class Notification(vararg body: BulmaElement, val onDelete: (Notification) -> Unit = {}) : BulmaElement {

    override val root: HTMLElement = document.create.div("notification") {
        button(classes = "delete") {
            onClickFunction = { onDelete(this@Notification) }
        }
    }

    var color by className(ElementColor.None, root)

    var body by bulmaList(body.toList(), root)
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

class Title(text: String, size: TextSize = TextSize.None) : BulmaElement {

    override val root: HTMLElement = document.create.h1("title") { +text }

    var size by className(size, root)
}

class SubTitle(text: String, size: TextSize = TextSize.None) : BulmaElement {

    override val root: HTMLElement = document.create.h1("subtitle") { +text }

    var size by className(size, root)
}
