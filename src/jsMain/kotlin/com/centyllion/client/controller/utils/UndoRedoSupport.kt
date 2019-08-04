package com.centyllion.client.controller.utils

import bulma.ElementColor
import bulma.Icon
import bulma.iconButton
import kotlin.properties.Delegates.observable

class UndoRedoSupport<T>(
    val set: (T) -> Unit
) {
    private var undoModel = false

    private var history: List<T> by observable(emptyList()) { _, _, new ->
        undoButton.disabled = new.isEmpty()
    }

    private var future: List<T> by observable(emptyList()) { _, _, new ->
        redoButton.disabled = new.isEmpty()
    }

    val undoButton = iconButton(Icon("undo"), ElementColor.Primary, rounded = true) {
        val restoredModel = history.last()
        history = history.dropLast(1)
        undoModel = true
        set(restoredModel)
        undoModel = false
    }

    val redoButton = iconButton(Icon("redo"), ElementColor.Primary, rounded = true) {
        val restoredModel = future.last()
        set(restoredModel)
    }

    fun changed(old: T, new: T) {
        if (undoModel) {
            future += old
        } else {
            history += old
            if (future.lastOrNull() == new) {
                future = future.dropLast(1)
            } else {
                future = emptyList()
            }
        }
    }

    fun refresh() {
        undoButton.disabled = history.isEmpty()
        redoButton.disabled = future.isEmpty()
    }

    fun reset() {
        history = emptyList()
        future = emptyList()
    }
}
