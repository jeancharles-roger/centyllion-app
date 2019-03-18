package bulma

import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.HTMLElement
import kotlin.browser.document

class HtmlWrapper(override val root: HTMLElement): BulmaElement

fun wrap(classes : String? = null, block : DIV.() -> Unit = {}) = HtmlWrapper(document.create.div(classes, block))

/** [Container](https://bulma.io/documentation/layout/container) element */
class Container(initialBody: List<BulmaElement> = emptyList()): BulmaElement {
    override val root: HTMLElement = document.create.div("container")

    var body by bulmaList(initialBody, root)
}

/** [Level](https://bulma.io/documentation/layout/level) element */
class Level: BulmaElement {
    override val root: HTMLElement = document.create.div("level") {
        div("level-left")
        div("level-right")
    }

    private val leftNode = root.querySelector(".level-left") as HTMLElement
    private val rightNode = root.querySelector(".level-right") as HTMLElement

    var mobile by className(false, "is-mobile", root)

    var left by bulmaList(emptyList(), leftNode)
    var center by bulmaList(emptyList(), root, rightNode)
    var right by bulmaList(emptyList(), rightNode)
}

/** [Media](https://bulma.io/documentation/layout/media) element */
class Media: BulmaElement {
    override val root: HTMLElement = document.create.article("media") {
        figure("media-left")
        div("media-content")
        div("media-right")
    }

    private val leftNode = root.querySelector(".media-left") as HTMLElement
    private val contentNode = root.querySelector(".media-content") as HTMLElement
    private val rightNode = root.querySelector(".media-right") as HTMLElement

    var left by bulmaList(emptyList(), leftNode)
    var center by bulmaList(emptyList(), contentNode)
    var right by bulmaList(emptyList(), rightNode)
}

/** [Hero](https://bulma.io/documentation/layout/hero) element */
class Hero: BulmaElement {
    override val root: HTMLElement = document.create.section("hero") {
        div("hero-head")
        div("hero-body")
        div("hero-foot")
    }

    private val headNode = root.querySelector(".hero-head") as HTMLElement
    private val bodyNode = root.querySelector(".hero-body") as HTMLElement
    private val footNode = root.querySelector(".hero-foot") as HTMLElement

    var size by className(Size.None, root)

    var fullheight by className(false, "is-fullheight", root)

    var color by className(ElementColor.None, root)

    var head by bulmaList(emptyList(), headNode)
    var body by bulmaList(emptyList(), bodyNode)
    var foot by bulmaList(emptyList(), footNode)
}

/** [Section](https://bulma.io/documentation/layout/section) element */
class Section(initialBody: List<BulmaElement> = emptyList()): BulmaElement {
    override val root: HTMLElement = document.create.div("section")

    var body by bulmaList(initialBody, root)
}

/** [Footer](https://bulma.io/documentation/layout/footer) element */
class Footer(initialBody: List<BulmaElement> = emptyList()): BulmaElement {
    override val root: HTMLElement = document.create.footer("footer")

    var body by bulmaList(initialBody, root)
}

// TODO support