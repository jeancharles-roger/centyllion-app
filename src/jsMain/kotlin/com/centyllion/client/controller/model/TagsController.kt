package com.centyllion.client.controller.model


import bulma.BulmaElement
import bulma.Button
import bulma.Control
import bulma.Delete
import bulma.ElementColor
import bulma.Field
import bulma.Help
import bulma.Icon
import bulma.Input
import bulma.Label
import bulma.Level
import bulma.NoContextController
import bulma.Tag
import bulma.Tags
import bulma.TagsSize
import bulma.iconButton
import com.centyllion.client.Api
import kotlin.properties.Delegates.observable

class TagsController(
    tags: String, api: Api,
    var onUpdate: (old: String, new: String, controller: TagsController) -> Unit = { _, _, _ -> }
) : NoContextController<String, BulmaElement>() {

    override var data: String by observable(tags) { _, old, new ->
        if (old != new) {
            tagsContainer.tags = createTags(new)
            onUpdate(old, new, this@TagsController)
            refresh()
        }
    }

    val tags get() = data.split(" ").filter { it.isNotBlank() }.map(String::trim)

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            tagsContainer.tags = createTags(data)
            addTagField.hidden = new
            popularLabel.hidden = new
            popularTags.hidden = new
        }
    }

    val tagsContainer = Tags(size = TagsSize.Normal)

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

    val popularLabel = Help("Popular")

    val popularTags = Tags().apply {
        api.modelTags().then {
            this.tags = it.content.take(5)
                .map { tag ->
                    Tag(tag, rounded = true, color = ElementColor.Info).apply {
                        root.style.cursor = "pointer"
                        root.onclick = { if (!data.contains(tag)) data += " ${tag.trim()}" }
                    }
                }
        }
    }

    override val container = Level(
        center = listOf(Label("Tags"), tagsContainer, addTagField, popularLabel, popularTags)
    )

    override fun refresh() {
    }

    fun createTags(source: String) = source
        .split(" ").filter { it.isNotBlank() }
        .map { tag ->
            Tag(
                tag, rounded = true,
                delete = if (!readOnly) Delete { data = data.replace(tag, "").trim() } else null
            )
        }
}
