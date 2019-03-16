package com.centyllion.client.controller

import KeycloakInstance
import com.centyllion.client.fetchGrainModels
import com.centyllion.client.saveGrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.sample.bacteriaModel
import com.centyllion.model.sample.carModel
import com.centyllion.model.sample.dendriteModel
import com.centyllion.model.sample.immunityModel
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.i
import kotlinx.html.js.onClickFunction
import kotlinx.html.p
import kotlinx.html.span
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.random.Random

class ModelPageController(val instance: KeycloakInstance) : Controller<Unit> {

    override var data: Unit = Unit

    val modelListController = ListController<GrainModelDescription>(
        emptyList(), "is-multiline", size(12)
    ) { i, model -> ModelDisplayController().apply { data = model } }

    override val container: HTMLElement = document.create.columns {
        column(size(3, 3)) {
            div("level is-mobile") {
                div("level-left") {
                    p("level-item title") { +"My models" }
                }
                div("level-right") {
                    a(classes = "level-item button is-rounded is-primary") {
                        span("icon") { i("fas fa-plus") }
                        onClickFunction = {
                            createModel()
                        }
                    }
                }
            }
            div("cent-my-models")
        }
        column {
            div("cent-edit")
        }
    }

    init {
        container.querySelector("div.cent-my-models")?.appendChild(modelListController.container)
        fetchGrainModels(instance).then { modelListController.data = it }
        refresh()
    }

    override fun refresh() {
    }

    fun createModel() {
        val model = when (Random.nextInt(4)) {
            0 -> dendriteModel()
            1 -> carModel()
            2 -> bacteriaModel()
            else -> immunityModel()
        }

        saveGrainModel(model, instance).then {
            modelListController.data = modelListController.data + it
        }
    }
}
