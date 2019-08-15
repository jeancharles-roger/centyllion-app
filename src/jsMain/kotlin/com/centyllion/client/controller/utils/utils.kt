package com.centyllion.client.controller.utils

import bulma.Controller
import bulma.Delete
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> Array<T>.push(e: T): Int = asDynamic().push(e) as Int
fun <T> Array<T>.pop(): T = asDynamic().pop() as T

class DeleteCallbackProperty<T>(
    val controller: Controller<T, *, *>,
    val deleteSetter: (old: Delete?, new: Delete?) -> Unit
): ReadWriteProperty<Any, ((T) -> Unit)?> {

    private var callback: ((T) -> Unit)? = null
    private var delete: Delete? = null

    var readOnly by Delegates.observable(false) { _, old, new ->
        if (old != new) delete?.hidden = new
    }

    override fun getValue(thisRef: Any, property: KProperty<*>) = callback

    override fun setValue(thisRef: Any, property: KProperty<*>, value: ((T) -> Unit)?) {
        if (callback != value) {
            val oldDelete = delete
            if (value != null) {
                delete = Delete { value(controller.data) }
                delete?.hidden = controller.readOnly
            } else {
                delete = null
            }
            callback = value
            if (oldDelete != delete) deleteSetter(oldDelete, delete)
        }
    }
}
