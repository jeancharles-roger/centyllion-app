package bulma

import org.w3c.dom.HTMLElement
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class HTMLElementProperty<T : HTMLElement>(
    initialValue: T,
    private val parent: HTMLElement,
    private val prepare: (T) -> Unit
) : ReadWriteProperty<Any?, T> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            prepare(value)
            if (parent.contains(oldValue)) {
                parent.replaceChild(oldValue, value)
            } else {
                parent.appendChild(value)
            }
        }
    }

    init {
        prepare(value)
        parent.appendChild(value)
    }
}

fun <T : HTMLElement> html(initialValue: T, parent: HTMLElement, prepare: (newValue: T) -> Unit) =
    HTMLElementProperty(initialValue, parent, prepare)

fun <T : HTMLElement> html(initialValue: T, parent: HTMLElement, vararg classes: String) =
    HTMLElementProperty(initialValue, parent) { node -> classes.forEach { node.classList.toggle(it, true) } }


class BulmaElementProperty<T : BulmaElement>(
    initialValue: T,
    private val parent: HTMLElement,
    private val prepare: (T) -> Unit
) : ReadWriteProperty<Any?, T> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            prepare(value)
            if (parent.contains(oldValue.root)) {
                parent.replaceChild(oldValue.root, value.root)
            } else {
                parent.appendChild(value.root)
            }
        }
    }

    init {
        prepare(value)
        parent.appendChild(value.root)
    }
}

fun <T : BulmaElement> bulma(initialValue: T, parent: HTMLElement, prepare: (newValue: T) -> Unit) =
    BulmaElementProperty(initialValue, parent, prepare)

fun <T : BulmaElement> html(initialValue: T, parent: HTMLElement, vararg classes: String) =
    BulmaElementProperty(initialValue, parent) { node -> classes.forEach { node.root.classList.toggle(it, true) } }

class BulmaElementListProperty<T : BulmaElement>(
    initialValue: List<T>,
    private val parent: HTMLElement,
    private val before: HTMLElement?
) : ReadWriteProperty<Any?, List<T>> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            oldValue.forEach { parent.removeChild(it.root) }
            if (before != null) {
                value.forEach { parent.insertBefore(it.root, before) }
            } else {
                value.forEach { parent.appendChild(it.root) }
            }
        }
    }

    init {
        if (before != null) {
            value.forEach { parent.insertBefore(it.root, before) }
        } else {
            value.forEach { parent.appendChild(it.root) }
        }
    }
}

fun <T : BulmaElement> bulmaList(initialValue: List<T> = emptyList(), parent: HTMLElement, before: HTMLElement? = null) =
    BulmaElementListProperty(initialValue, parent, before)

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
        if ((prefix + value).isNotEmpty()) {
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
            val oldClassName = "$prefix${oldValue.className}$suffix"
            if (oldClassName.isNotEmpty()) {
                node.classList.toggle(oldClassName, false)
            }
            val className = "$prefix${value.className}$suffix"
            if (className.isNotEmpty()) {
                node.classList.toggle(className, true)
            }
        }
    }

    init {
        val className = "$prefix${value.className}$suffix"
        if (className.isNotEmpty()) {
            node.classList.toggle(className, true)
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
class StringAttributeProperty<T>(
    initialValue: T,
    private val attributeName: String,
    private val node: HTMLElement
) : ReadWriteProperty<Any?, T> {

    private var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (oldValue != value) {
            this.value = value
            val string = value?.toString()
            if (string != null) {
                node.setAttribute(attributeName, string)
            } else {
                node.removeAttribute(attributeName)
            }
        }
    }

    init {
        val string = value?.toString()
        if (string != null) {
            node.setAttribute(attributeName, string)
        } else {
            node.removeAttribute(attributeName)
        }
    }
}

fun <T> attribute(initialValue: T, attribute: String, node: HTMLElement) =
    StringAttributeProperty(initialValue, attribute, node)
