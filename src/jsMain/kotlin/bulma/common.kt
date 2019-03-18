package bulma

import org.w3c.dom.HTMLElement


interface BulmaElement {
    val root: HTMLElement
}

interface HasClassName {
    val className: String
}

enum class Size(override val className: String) : HasClassName {
    None(""), Small("is-small"), Normal("is-normal"),
    Medium("is-medium"), Large("is-large");

    fun toFas() = when (this) {
        None -> FasSize.None
        Small -> FasSize.Small
        Normal -> FasSize.None
        Medium -> FasSize.Medium
        Large -> FasSize.Large
    }
}

enum class FasSize(override val className: String) : HasClassName {
    None(""), Small("fa-1x"),
    Medium("fa-2x"), Large("fa-3x")
}



enum class ElementColor(override val className: String) : HasClassName {
    None(""),
    White("is-white"), Black("is-black"),
    Light("is-light"), Dark("is-dark"),
    Primary("is-primary"), Info("is-info"),
    Link("is-link"), Success("is-success"),
    Warning("is-warning"), Danger("is-danger");

    fun next() = values().let { it[(ordinal + 1) % it.count()] }
}

enum class TextColor(override val className: String) : HasClassName {
    None(""), White("has-text-white"),
    Black("has-text-black"), Light("has-text-light"),
    Dark("has-text-dark"), Primary("has-text-primary"),
    Info("has-text-info"), Link("has-text-link"),
    Success("has-text-success"), Warning("has-text-warning"), Danger("has-text-danger"),
    BlackBis("has-text-black-bis"), BlackBer("has-text-black-ter"),
    GreyDarker("has-text-grey-darker"), GreyDark("has-text-grey-dark"),
    Grey("has-text-grey"),
    GreyLight("has-text-grey-light"), GreyLighter("has-text-grey-lighter"),
    WhiteTer("has-text-white-ter"), WhiteBis("has-text-white-bis");

    fun next() = values().let { it[(ordinal + 1) % it.count()] }
}
