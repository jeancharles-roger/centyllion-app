package com.centyllion.client.controller.model


import bulma.BulmaElement
import bulma.Button
import bulma.Column
import bulma.Columns
import bulma.Control
import bulma.Delete
import bulma.Field
import bulma.Icon
import bulma.Input
import bulma.Label
import bulma.NoContextController
import bulma.Tag
import bulma.Tags
import bulma.iconButton
import kotlin.properties.Delegates.observable

class TagsController(
    tags: String,
    var onUpdate: (old: String, new: String, controller: TagsController) -> Unit = { _, _, _ -> }
) : NoContextController<String, BulmaElement>() {

    override var data: String by observable(tags) { _, old, new ->
        if (old != new) {
            tagsContainer.tags = createTags(new)
            onUpdate(old, new, this@TagsController)
            refresh()
        }
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            addTagField.hidden = new
            tagsContainer.tags = createTags(data)
        }
    }

    val tagsContainer = Tags()

    val input: Input = Input(value = "", placeholder = "New tag", rounded = true) { _, value ->
        addTagButton.disabled = value.isBlank() || data.matches("\\b${value.trim()}\\b")
    }

    val addTagButton: Button = iconButton(Icon("plus"), rounded = true, disabled = true) {
        data += " ${input.value.trim()}"
        it.disabled = true
    }

    val addTagField = Field(
        Control(addTagButton),
        Control(input),
        addons = true
    )

    override val container = Columns(
        Column(Label("Tags")), Column(tagsContainer), Column(addTagField)
    )

    override fun refresh() {
    }

    fun createTags(source: String) = source
        .split(" ").filter { it.isNotBlank() }
        .map {
            Tag(it, rounded = true).apply {
                if (!readOnly) root.appendChild(Delete { data = data.replace(text, "").trim() }.root)
            }
        }
}
