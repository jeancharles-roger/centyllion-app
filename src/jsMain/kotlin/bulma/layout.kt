package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

class HtmlWrapper<Html : HTMLElement>(override val root: Html) : BulmaElement

fun wrap(classes: String? = null, block: DIV.() -> Unit = {}) =
    HtmlWrapper(document.create.div(classes, block) as HTMLDivElement)

fun div(vararg body: BulmaElement, classes: String = "") =
    HtmlWrapper(document.create.div(classes)).apply {
        body.forEach { root.appendChild(it.root) }
    }

fun span(vararg body: BulmaElement, classes: String = "") =
    HtmlWrapper(document.create.span(classes) as HTMLSpanElement).apply {
        body.forEach { root.appendChild(it.root) }
    }

fun p(vararg body: BulmaElement, classes: String = "") = HtmlWrapper(document.create.span(classes)).apply {
    body.forEach { root.appendChild(it.root) }
}

fun canvas(classes: String? = null, block: CANVAS.() -> Unit = {}) =
    HtmlWrapper(document.create.canvas(classes, block) as HTMLCanvasElement)

/** [Container](https://bulma.io/documentation/layout/container) element */
class Container(initialBody: List<BulmaElement> = emptyList()) : BulmaElement {
    override val root: HTMLElement = document.create.div("container")

    var body by bulmaList(initialBody, root)
}

/** [Level](https://bulma.io/documentation/layout/level) element */
class Level(
    left: List<BulmaElement> = emptyList(), center: List<BulmaElement> = emptyList(),
    right: List<BulmaElement> = emptyList(), mobile: Boolean = false
) : BulmaElement {

    override val root: HTMLElement = document.create.div("level")

    var mobile by className(mobile, "is-mobile", root)

    var left by embeddedBulmaList(left, root, Position.AfterBegin) {
        if (it.isNotEmpty()) document.create.div("level-left") else null
    }

    var center by bulmaList(center, root) { root.querySelector(".level-right") }

    var right by embeddedBulmaList(right, root, Position.BeforeEnd) {
        if (it.isNotEmpty()) document.create.div("level-right") else null
    }

}

/** [Media](https://bulma.io/documentation/layout/media) element */
class Media(
    left: List<BulmaElement> = emptyList(), center: List<BulmaElement> = emptyList(), right: List<BulmaElement> = emptyList()
) : BulmaElement {
    override val root: HTMLElement = document.create.article("media") {
        div("media-content")
    }

    private val contentNode = root.querySelector(".media-content") as HTMLElement

    var left by embeddedBulmaList(left, root, Position.AfterBegin) {
        if (it.isNotEmpty()) document.create.figure("media-left") else null
    }

    var center by bulmaList(center, contentNode)

    var right by embeddedBulmaList(right, root, Position.BeforeEnd) {
        if (it.isNotEmpty()) document.create.div("media-right") else null
    }
}

/** [Hero](https://bulma.io/documentation/layout/hero) element */
class Hero : BulmaElement {
    override val root: HTMLElement = document.create.section("hero") {
        div("hero-body")
    }

    private val bodyNode = root.querySelector(".hero-body") as HTMLElement

    var size by className(Size.None, root)

    var fullheight by className(false, "is-fullheight", root)

    var color by className(ElementColor.None, root)

    var head by embeddedBulmaList(emptyList(), root, Position.AfterBegin) {
        if (it.isNotEmpty()) document.create.div("hero-head") else null
    }

    var body by bulmaList(emptyList(), bodyNode)

    var foot by embeddedBulmaList(emptyList(), root, Position.BeforeEnd) {
        if (it.isNotEmpty()) document.create.div("hero-foot") else null
    }
}

/** [Section](https://bulma.io/documentation/layout/section) element */
class Section(initialBody: List<BulmaElement> = emptyList()) : BulmaElement {
    override val root: HTMLElement = document.create.div("section")

    var body by bulmaList(initialBody, root)
}

/** [Footer](https://bulma.io/documentation/layout/footer) element */
class Footer(initialBody: List<BulmaElement> = emptyList()) : BulmaElement {
    override val root: HTMLElement = document.create.footer("footer")

    var body by bulmaList(initialBody, root)
}

/** TODO [Tile](https://bulma.io/documentation/layout/tiles) element */
