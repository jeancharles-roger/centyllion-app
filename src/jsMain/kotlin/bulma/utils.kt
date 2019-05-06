package bulma

import com.centyllion.model.DiffAction
import com.centyllion.model.diff
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Position where to insert on node using `insertAdjacentElement`. */
enum class Position(val value: String) {
    BeforeBegin("beforebegin"), AfterBegin("afterbegin"),
    BeforeEnd("beforeend"), AfterEnd("afterend")
}

/** Property that handle the insertion and the change of a [BulmaElement]. */
class BulmaElementProperty<T>(
    initialValue: T?,
    private val parent: HTMLElement,
    private val prepare: (T) -> HTMLElement?,
    private val position: Position = Position.BeforeEnd
) : ReadWriteProperty<Any?, T?> {

    private var value = initialValue
    private var element = value?.let { prepare(it) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value

            val oldElement = element
            val newElement = value?.let { prepare(it) }
            element = newElement

            if (oldElement != null && newElement != null && parent.contains(oldElement)) {
                parent.replaceChild(newElement, oldElement)
            } else {
                if (oldElement != null) {
                    parent.removeChild(oldElement)
                }
                if (newElement != null) {
                    parent.insertAdjacentElement(position.value, newElement)
                }
            }
        }
    }

    init {
        element?.let {
            parent.insertAdjacentElement(position.value, it)
        }
    }
}

fun <T : BulmaElement> bulma(
    initialValue: T?,
    parent: HTMLElement,
    position: Position = Position.BeforeEnd,
    prepare: (newValue: T) -> HTMLElement? = { it.root }
) = BulmaElementProperty(initialValue, parent, prepare, position)

fun <T> html(
    initialValue: T?,
    parent: HTMLElement,
    position: Position = Position.BeforeEnd,
    prepare: (newValue: T) -> HTMLElement?
) = BulmaElementProperty(initialValue, parent, prepare, position)

class BulmaElementListProperty<T : BulmaElement>(
    initialValue: List<T>, private val parent: HTMLElement,
    private val before: () -> Element?,
    private val position: Position = Position.BeforeEnd,
    private val prepare: (T) -> HTMLElement
) : ReadWriteProperty<Any?, List<T>> {

    private var value = initialValue
    private var elements = value.map { prepare(it) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            // keep old element reference for now
            val oldElements = elements

            // construct new ones
            elements = value.map { prepare(it) }

            // before reference
            val reference = before()

            val diff = oldElements.diff(elements)
            diff.forEach {
                when (it.action) {
                    DiffAction.Added -> when {
                            parent.childElementCount == 0 && reference != null -> parent.insertBefore(it.element, reference)
                            parent.childElementCount == 0 -> parent.insertAdjacentElement(position.value, it.element)
                            it.index < parent.childElementCount && reference != null -> parent.insertBefore(it.element, parent.children.item(it.index))
                            else -> parent.insertAdjacentElement(position.toString(), it.element)
                        }
                    DiffAction.Removed -> parent.removeChild(it.element)
                    DiffAction.Replaced -> parent.replaceChild(it.element, oldElements[it.index])
                }
            }
        }
    }

    init {
        val reference = before()
        elements = value.map { prepare(it) }
        if (reference != null) {
            elements.forEach { parent.insertBefore(it, reference) }
        } else {
            elements.forEach { parent.insertAdjacentElement(position.value, it) }
        }
    }
}

fun <T : BulmaElement> bulmaList(
    initialValue: List<T> = emptyList(), parent: HTMLElement,
    before: () -> Element? = { null },
    prepare: (T) -> HTMLElement = { it.root }
) = BulmaElementListProperty(initialValue, parent, before, Position.BeforeEnd, prepare)

class BulmaElementEmbeddedListProperty<T : BulmaElement>(
    initialValue: List<T>,
    private val parent: HTMLElement,
    private val before: HTMLElement?,
    private val position: Position = Position.BeforeEnd,
    private val prepare: (T) -> HTMLElement,
    private val containerBuilder: (List<T>) -> HTMLElement
) : ReadWriteProperty<Any?, List<T>> {

    private var value = initialValue
    private var elements = value.map { prepare(it) }
    private var container = if (initialValue.isEmpty()) null else containerBuilder(initialValue).apply {
        elements.forEach { appendChild(it) }
        if (before != null) {
            parent.insertBefore(this, before)
        } else {
            parent.insertAdjacentElement(position.value, this)
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value

            val oldContainer = container

            // keep old element reference for now
            val oldElements = elements

            // construct new ones
            elements = value.map { prepare(it) }

            when {
                value.isEmpty() -> {
                    // list is empty, new container is null
                    container = null
                    if (oldContainer != null) parent.removeChild(oldContainer)
                }
                oldValue.isEmpty() -> {
                    // previous list was empty, old container was null
                    container = containerBuilder(value).apply {
                        value.forEach { this.appendChild(prepare(it)) }
                        if (before != null) {
                            parent.insertBefore(this, before)
                        } else {
                            parent.insertAdjacentElement(position.value, this)
                        }
                    }
                }
                else -> container?.let { node ->

                    val diff = oldElements.diff(elements)
                    diff.forEach {
                        when (it.action) {
                            //result.add(it.index, it.element)
                            DiffAction.Added -> when {
                                node.childElementCount == 0 && before != null -> node.insertBefore(it.element, before)
                                node.childElementCount == 0 -> node.insertAdjacentElement(position.value, it.element)
                                it.index < node.childElementCount -> node.insertBefore(it.element, node.children.item(it.index))
                                else -> node.insertAdjacentElement(Position.BeforeEnd.value, it.element)
                            }
                            DiffAction.Removed -> node.removeChild(it.element)
                            DiffAction.Replaced -> node.replaceChild(it.element, oldElements[it.index])
                        }
                    }
                }
            }
        }
    }
}

fun <T : BulmaElement> embeddedBulmaList(
    initialValue: List<T> = emptyList(), parent: HTMLElement,
    position: Position = Position.BeforeEnd, prepare: (T) -> HTMLElement = { it.root },
    containerBuilder: (List<T>) -> HTMLElement
) =
    BulmaElementEmbeddedListProperty(initialValue, parent, null, position, prepare, containerBuilder)


/** Property class delegate that handle a boolean property that set or reset a css class. */
class BooleanClassProperty(
    initialValue: Boolean,
    private val className: String,
    private val node: HTMLElement
) : ReadWriteProperty<Any?, Boolean> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            node.classList.toggle(className, value)
        }
    }

    init {
        node.classList.toggle(className, value)
    }
}

/** Property class delegate that handle a string property that set or reset a css class. */
class StringClassProperty(
    initialValue: String,
    private val node: HTMLElement,
    private val prefix: String = ""
) : ReadWriteProperty<Any?, String> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): String = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            if ((prefix + oldValue).isNotEmpty()) {
                node.classList.toggle(prefix + oldValue, false)
            }
            if ((prefix + value).isNotEmpty()) {
                node.classList.toggle(prefix + value, true)
            }
        }
    }

    init {
        if (value.isNotEmpty()) {
            node.classList.toggle(prefix + value, true)
        }
    }
}

/** Property class delegate that handle an [HasClassName] property value to set css class. */
class ClassProperty<T : HasClassName>(
    initialValue: T,
    private val node: HTMLElement,
    private val prefix: String = "",
    private val suffix: String = ""
) : ReadWriteProperty<Any?, T> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            if (oldValue.className.isNotEmpty()) {
                node.classList.toggle("$prefix${oldValue.className}$suffix", false)
            }
            if (value.className.isNotEmpty()) {
                node.classList.toggle("$prefix${value.className}$suffix", true)
            }
        }
    }

    init {
        if (value.className.isNotEmpty()) {
            node.classList.toggle("$prefix${value.className}$suffix", true)
        }
    }
}

fun className(initialValue: Boolean, className: String, node: HTMLElement) =
    BooleanClassProperty(initialValue, className, node)

fun <T : HasClassName> className(initialValue: T, node: HTMLElement, prefix: String = "", suffix: String = "") =
    ClassProperty(initialValue, node, prefix, suffix)

fun className(initialValue: String, node: HTMLElement, prefix: String = "") =
    StringClassProperty(initialValue, node, prefix)


/** Property class delegate that handle a string property that set or reset an attribute. */
class StringAttributeProperty(
    initialValue: String?,
    private val attributeName: String,
    private val node: HTMLElement
) : ReadWriteProperty<Any?, String?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? =
        node.getAttribute(attributeName) ?: ""


    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (value != null) {
            node.setAttribute(attributeName, value)
        } else {
            node.removeAttribute(attributeName)
        }
    }

    init {
        if (initialValue != null) {
            node.setAttribute(attributeName, initialValue)
        } else {
            node.removeAttribute(attributeName)
        }
    }
}

/** Property class delegate that handle a string property that set or reset an attribute. */
class BooleanAttributeProperty(
    initialValue: Boolean,
    private val attributeName: String,
    private val node: HTMLElement
) : ReadWriteProperty<Any?, Boolean> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = node.hasAttribute(attributeName)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        if (value) {
            node.setAttribute(attributeName, "")
        } else {
            node.removeAttribute(attributeName)
        }
    }

    init {
        if (initialValue) {
            node.setAttribute(attributeName, "")
        } else {
            node.removeAttribute(attributeName)
        }
    }
}

/** Property class delegate that handle a int property that set or reset an attribute. */
class IntAttributeProperty(
    initialValue: Int?,
    private val attributeName: String,
    private val node: HTMLElement
) : ReadWriteProperty<Any?, Int?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int? =
        node.getAttribute(attributeName)?.toIntOrNull()


    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
        if (value != null) {
            node.setAttribute(attributeName, value.toString())
        } else {
            node.removeAttribute(attributeName)
        }
    }

    init {
        if (initialValue != null) {
            node.setAttribute(attributeName, initialValue.toString())
        } else {
            node.removeAttribute(attributeName)
        }
    }
}

fun booleanAttribute(initialValue: Boolean, attribute: String, node: HTMLElement) =
    BooleanAttributeProperty(initialValue, attribute, node)

fun intAttribute(initialValue: Int?, attribute: String, node: HTMLElement) =
    IntAttributeProperty(initialValue, attribute, node)

fun attribute(initialValue: String?, attribute: String, node: HTMLElement) =
    StringAttributeProperty(initialValue, attribute, node)
