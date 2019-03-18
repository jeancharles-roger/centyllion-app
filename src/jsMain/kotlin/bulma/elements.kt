package bulma

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.progress
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement
import kotlin.browser.document

/** [Box](https://bulma.io/documentation/elements/box) element. */
class Box : BulmaElement {
    override val root: HTMLElement = document.create.div("box")

    var body by bulmaList(emptyList(), root)
}

/** [Button](https://bulma.io/documentation/elements/button) element. */
class Button(initialText: String, initialColor: ElementColor = ElementColor.None, val onClick: (Button) -> Unit = {}) : BulmaElement {

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

    var color by className(initialColor, root)

    var size by className(Size.None, root)

    var disabled by booleanAttribute(false, "disabled", root)

}

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
class Icon(initialIcon: String) : BulmaElement {
    override val root: HTMLElement = document.create.span("icon") {
        i("fas fa-$initialIcon")
    }

    private val iconNode = root.querySelector(".fas") as HTMLElement

    var icon by className(initialIcon, iconNode, "fa-")

    var size
        get() = outerSize
        set(value) {
            outerSize = value
            iconSize = value.toFas()
        }

    var outerSize by className(Size.None, root)

    var iconSize by className(FasSize.None, iconNode)

    var color by className(TextColor.None, root)

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

    var min by attribute<Int?>(null, "min", root)

    var value by attribute<Int?>(null, "value", root)

    var max by attribute<Int?>(null, "max", root)

}

// TODO adds support for Table

/** [Tag](https://bulma.io/documentation/elements/tag) element. */
class Tag(
    initialText: String,
    initialColor: ElementColor = ElementColor.None,
    initialSize: Size = Size.None
) : BulmaElement {

    override val root: HTMLElement = document.create.span("tag") {
        +initialText
    }

    var text = initialText
        set(value) {
            if (value != field) {
                field = value
                root.innerText = field
            }
        }

    var color by className(initialColor, root)

    var size by className(initialSize, root)

    var rounded by className(false, "is-rounded", root)

    // TODO adds support for delete button
}

/** [Tags](https://bulma.io/documentation/elements/tag/#list-of-tags) element */
class Tags(initialTags: List<Tag> = emptyList()) : BulmaElement {

    override val root: HTMLElement = document.create.div("tags")

    var tags by bulmaList<Tag>(initialTags, root)
}

