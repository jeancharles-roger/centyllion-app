package com.centyllion.client.controller.model

import bulma.*
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.ModelAndSimulation
import kotlin.js.Promise

class ModelSearchController (
    val page: BulmaPage,
    var onLoad: (loaded: ModelAndSimulation) -> Unit = { _ -> }
) : NoContextController<Unit, Box>() {

    override var data: Unit = Unit

    override var readOnly: Boolean = false

    private fun simulationBox(model: ModelAndSimulation) = Box(
        Level(
            left = listOf(Image("${page.appContext.api.dbUrl}/asset/${model.thumbnail}", size = ImageSize.S128)),
            center = listOf(Label("${model.model.label} / ${model.simulation.label}")),
            right = listOf(Button(
                title = page.i18n("Import"),
                color = ElementColor.Primary,
                rounded = true
            ) {
                page.modalDialog(page.i18n("Load new simulation. Are you sure ?"),
                    listOf(p(page.i18n("You're about to quit the page and some modifications haven't been saved."))),
                    textButton(page.i18n("Yes"), ElementColor.Success) { onLoad(model) },
                    textButton(page.i18n("No")) { /* do nothing */ }
                )

            })
        )
    )

    val input = Input(
        "", page.i18n("Search"), rounded = true
    ) { _, value ->
        // uses a new list to avoid old search to pop in current search
        // adds loading element to indicate user that load is ongoing
        val currentList = mutableListOf(loadingElement)
        result.body = currentList.toList()

        page.appContext.api.searchQuery(value).then { list ->
            // only keep first ten results
            val found = list.map { page.appContext.api.simulationGet(it) }.toTypedArray()
            found.forEach {
                it.then { data ->
                    currentList += simulationBox(data)
                    result.body = currentList.toList()
                }
            }

            // when all promises are completed, remove loading element.
            Promise.all(found).then {
                currentList -= loadingElement
                result.body = currentList.toList()
            }
        }
    }

    val loadingElement = Box(Label(page.i18n("Searching")))

    var result = Container()

    override val container = Box(
        input,
        Box(),
        result,
    )

    override fun refresh() {
    }
}
