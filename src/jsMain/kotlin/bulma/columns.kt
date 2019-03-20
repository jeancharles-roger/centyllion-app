package bulma

import kotlinx.html.dom.create
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import kotlin.browser.document


enum class ColumnSize(override val className: String) : HasClassName {
    None(""),
    ThreeQuarters("is-three-quarters"),
    TwoThirds("is-two-thirds"),
    Half("is-half"),
    OneThird("is-one-third"),
    OneQuarter("is-one-quarter"),
    Full("is-full"),
    FourFifths("is-four-fifths"),
    ThreeFifths("is-three-fifths"),
    TwoFifths("is-two-fifths"),
    OneFifth("is-one-fifth"),
    S1("is-1"),
    S2("is-2"),
    S3("is-3"),
    S4("is-4"),
    S5("is-5"),
    S6("is-6"),
    S7("is-7"),
    S8("is-8"),
    S9("is-9"),
    S10("is-10"),
    S11("is-11"),
    S12("is-12")
}

/** [Columns](https://bulma.io/documentation/columns) element */
class Columns(initialColumn: List<Column> = emptyList()) : BulmaElement {

    override val root: HTMLElement = document.create.div("columns")

    /** [Multiline](https://bulma.io/documentation/columns/options/#multiline) */
    var multiline by className(false, "is-multiline", root)

    var mobile by className(false, "is-mobile", root)

    var desktop by className(false, "is-desktop", root)

    /** Removes [gap](https://bulma.io/documentation/columns/gap/#gapless) between columns */
    var gapless by className(false, "is-gapless", root)

    /** [Centering](https://bulma.io/documentation/columns/options/#centering-columns) */
    var centering by className(false, "is-centering", root)

    // TODO variable gap  https://bulma.io/documentation/columns/gap/#variable-gap

    var columns by bulmaList<Column>(initialColumn, root)
}

fun columns(
    vararg columns: Column,
    multiline: Boolean = false,
    mobile: Boolean = false
) = Columns(columns.toList()).apply {
    this.multiline = multiline
    this.mobile = mobile
}

/** [Column](https://bulma.io/documentation/columns/basics) element */
class Column(initialBody: List<BulmaElement> = emptyList()) : BulmaElement {

    override val root: HTMLElement = document.create.div("column")

    // TODO support offset

    /** [Size](https://bulma.io/documentation/columns/sizes) */
    var size by className(ColumnSize.None, root)

    /** Mobile [Responsiveness size](http://bulma.io/documentation/columns/responsiveness/) */
    var mobileSize by className(ColumnSize.None, root, suffix = "-mobile")

    /** Tablet [Responsiveness size](http://bulma.io/documentation/columns/responsiveness/) */
    var tabletSize by className(ColumnSize.None, root, suffix = "-tablet")

    /** Desktop [Responsiveness size](http://bulma.io/documentation/columns/responsiveness/) */
    var desktopSize by className(ColumnSize.None, root, suffix = "-desktop")

    /** Wide screen [Responsiveness size](http://bulma.io/documentation/columns/responsiveness/) */
    var wideScreenSize by className(ColumnSize.None, root, suffix = "-widescreen")

    /** Full HD [Responsiveness size](http://bulma.io/documentation/columns/responsiveness/) */
    var fullHdSize by className(ColumnSize.None, root, suffix = "-fullhd")

    var narrow by className(false, "is-narrow", root)

    var body by bulmaList(initialBody, root)
}

fun column(
    vararg elements: BulmaElement,
    size: ColumnSize = ColumnSize.None,
    mobileSize: ColumnSize = ColumnSize.None,
    tabletSize: ColumnSize = ColumnSize.None,
    desktopSize: ColumnSize = ColumnSize.None,
    wideScreenSize: ColumnSize = ColumnSize.None,
    fullHdSize: ColumnSize = ColumnSize.None
) = Column(elements.toList()).apply {
    this.size = size
    this.mobileSize = mobileSize
    this.tabletSize = tabletSize
    this.desktopSize = desktopSize
    this.wideScreenSize = wideScreenSize
    this.fullHdSize = fullHdSize
}
