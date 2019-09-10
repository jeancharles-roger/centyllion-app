package com.centyllion.client.controller.navigation

import bulma.Card
import bulma.CardContent
import bulma.Div
import bulma.Help
import bulma.Label
import bulma.NoContextController
import bulma.Tag
import bulma.Tags
import com.centyllion.client.markdownToHtml
import com.centyllion.model.GrainModelDescription
import kotlin.properties.Delegates.observable

class GrainModelDisplayController(modelDescription: GrainModelDescription) :
    NoContextController<GrainModelDescription, Card>() {

    override var data by observable(modelDescription) { _, old, new ->
        if (old != new) {
            refresh()
        }
    }

    override var readOnly = false

    val name = Label(data.model.name)
    val description = Div().apply { root.innerHTML = markdownToHtml(data.model.description) }
    val tags = Tags(createTags(modelDescription.tags))
    val author = Help(data.info.user?.name?.let {"by $it"} ?: "")


    override val container = Card(CardContent(name, description, author), CardContent(tags))

    override fun refresh() {
        name.text = data.name
        description.root.innerHTML = markdownToHtml(data.model.description)
        tags.tags = createTags(data.tags)
        author.text = data.info.user?.name?.let {"by $it"} ?: ""
    }

    fun createTags(source: String) =
        source.split(" ").filter { it.isNotBlank() }
              .map { tag -> Tag(tag, rounded = true) }
}
