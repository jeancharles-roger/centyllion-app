package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.client.fetchGrainModel
import com.centyllion.model.Behaviour
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import kotlin.properties.Delegates.observable

class FeaturedController(
    featured: FeaturedDescription, val instance: KeycloakInstance?, size: ColumnSize = ColumnSize.Half
) : NoContextController<FeaturedDescription, Column>() {

    override var data by observable(featured) { _, old, new ->
        if (old != new)  refresh()
    }

    val name = SubTitle(data.name)
    val description = Label(data.description)

    fun behaviourElement(model: GrainModel, behaviour: Behaviour): BulmaElement {
        val reactives =
            (listOf(behaviour.mainReactiveId) + behaviour.reaction.map { it.reactiveId})
            .mapNotNull { model.indexedGrains[it] }
            .map { Icon("circle").apply { root.style.color = it.color } }
            .toTypedArray()

        val products =
            (listOf(behaviour.mainProductId) + behaviour.reaction.map { it.productId})
            .mapNotNull { model.indexedGrains[it] }
            .map { Icon("circle").apply { root.style.color = it.color } }
            .toTypedArray()

        val center =
            listOf(Help(behaviour.name)) + div(*reactives) +
            Icon("arrow-right") + div(*products)
        return Level(center = center)
    }

    val author = Label(data.authorName)

    fun thumbnail() =
        if (data.thumbnailId.isNotEmpty()) Image("/api/asset/${data.thumbnailId}", ImageSize.S128) else null

    val body = Media(
        left = listOfNotNull(thumbnail()),
        center = listOf(name, description),
        right = listOf(author)
    )

    fun fetchBehaviours() {
        fetchGrainModel(data.modelId, instance).then { model ->
            body.center = listOf(name, description) + model.model.behaviours.map { behaviourElement(model.model, it) }
        }
    }

    override val container = Column(body, size = size)

    init {
        fetchBehaviours()
    }

    override fun refresh() {
        body.left = listOfNotNull(thumbnail())
        name.text = data.name
        description.text = data.description
        author.text = data.authorName
        fetchBehaviours()
    }
}
