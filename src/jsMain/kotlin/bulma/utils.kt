package bulma

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
class BulmaElementProperty<T : BulmaElement>(
    initialValue: T?,
    private val parent: HTMLElement,
    private val prepare: (T) -> Unit,
    private val position: Position = Position.BeforeEnd
) : ReadWriteProperty<Any?, T?> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value

            if (oldValue != null && value != null && parent.contains(oldValue.root)) {
                prepare(value)
                parent.replaceChild(oldValue.root, value.root)
            } else {
                if (oldValue != null) {
                    parent.removeChild(oldValue.root)
                }
                if (value != null) {
                    prepare(value)
                    parent.insertAdjacentElement(position.value, value.root)
                }
            }
        }
    }

    init {
        value?.let {
            prepare(it)
            parent.appendChild(it.root)
        }
    }
}

fun <T : BulmaElement> bulma(initialValue: T?, parent: HTMLElement, prepare: (newValue: T) -> Unit = {}) =
    BulmaElementProperty(initialValue, parent, prepare)

class BulmaElementListProperty<T : BulmaElement>(
    initialValue: List<T>, private val parent: HTMLElement, private val before: () -> Element?
) : ReadWriteProperty<Any?, List<T>> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            oldValue.forEach { parent.removeChild(it.root) }
            val reference = before()
            if (reference != null) {
                value.forEach { parent.insertBefore(it.root, reference) }
            } else {
                value.forEach { parent.appendChild(it.root) }
            }
        }
    }

    init {
        val reference = before()
        if (reference != null) {
            value.forEach { parent.insertBefore(it.root, reference) }
        } else {
            value.forEach { parent.appendChild(it.root) }
        }
    }
}

fun <T : BulmaElement> bulmaList(
    initialValue: List<T> = emptyList(), parent: HTMLElement, before: () -> Element? = { null }
) =
    BulmaElementListProperty(initialValue, parent, before)


class BulmaElementEmbeddedListProperty<T : BulmaElement>(
    initialValue: List<T>,
    private val parent: HTMLElement,
    private val before: HTMLElement?,
    private val position: Position = Position.BeforeEnd,
    private val containerBuilder: (List<T>) -> HTMLElement?
) : ReadWriteProperty<Any?, List<T>> {

    private var value = initialValue
    private var container = containerBuilder(initialValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val oldValue = this.value
        if (oldValue != value) {
            val oldContainer = container
            val newContainer = containerBuilder(value)
            container = newContainer
            if (oldContainer != null && newContainer != null && parent.contains(oldContainer)) {
                parent.replaceChild(oldContainer, newContainer)
            } else {
                if (oldContainer != null) {
                    parent.removeChild(oldContainer)
                }
                if (newContainer != null) {
                    value.forEach { newContainer.appendChild(it.root) }
                    if (before != null) {
                        parent.insertBefore(newContainer, before)
                    } else {
                        parent.insertAdjacentElement(position.value, newContainer)
                    }
                }
            }
        }
    }

    init {
        val newContainer = containerBuilder(value)
        container = newContainer
        if (newContainer != null) {
            value.forEach { newContainer.appendChild(it.root) }
            if (before != null) {
                parent.insertBefore(newContainer, before)
            } else {
                parent.insertAdjacentElement(position.value, newContainer)
            }
        }
    }
}

fun <T : BulmaElement> embeddedBulmaList(
    initialValue: List<T> = emptyList(), parent: HTMLElement,
    position: Position = Position.BeforeEnd, containerBuilder: (List<T>) -> HTMLElement?
) =
    BulmaElementEmbeddedListProperty(initialValue, parent, null, position, containerBuilder)


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
