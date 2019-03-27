package bulma

import kotlinx.html.a
import kotlinx.html.dom.create
import kotlinx.html.js.li
import kotlinx.html.js.nav
import kotlinx.html.ul
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import kotlin.browser.document

enum class BreadcrumbSeparator(override val className: String): HasClassName {
    Default(""), Arrow("has-arrow-separator"), Bullet("has-bullet-separator"),
    Dot("has-dot-separator"), Succeeds("has-succeeds-separator")
}

class BreadcrumbElement(text: String = "", href: String = "", icon: Icon? = null): BulmaElement {

    override val root: HTMLElement = document.create.li {
        a(href = href) { +text }
    }

    private val aNode = root.querySelector("a") as HTMLAnchorElement

    override var text: String
        get() = aNode.innerText
        set(value) { aNode.innerText = value }

    var href: String
        get() = aNode.href
        set(value) { aNode.href = value }

    var icon by bulma(icon, aNode)
}

/** [Breadcrumb](https://bulma.io/documentation/components/breadcrumb) element */
class Breadcrumb(
    vararg body: BreadcrumbElement, separator: BreadcrumbSeparator = BreadcrumbSeparator.Default,
    size: Size = Size.None, centered: Boolean = false, right: Boolean = false
): BulmaElement {

    override val root: HTMLElement = document.create.nav(classes = "breadcrumb") {
        ul()
    }

    private val ulNode = root.querySelector("ul") as HTMLUListElement

    var body by bulmaList(body.toList(), ulNode)

    var separator by className(separator, root)

    var size by className(size, root)

    var centered by className(centered, "is-centered", root)

    var right by className(right, "is-right", root)

}
