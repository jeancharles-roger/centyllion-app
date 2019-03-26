package com.centyllion.client.controller

import KeycloakInstance
import bulma.*
import com.centyllion.client.fetchGrainModels
import com.centyllion.client.saveGrainModel
import com.centyllion.client.updateGrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Simulation
import com.centyllion.model.Simulator
import com.centyllion.model.sample.emptyModel
import kotlin.properties.Delegates.observable

class ModelPage(val instance: KeycloakInstance): Controller<GrainModelDescription, List<GrainModelDescription>, BulmaElement> {

    var selectingModel = false

    var needSaveStatus = arrayOf<Boolean>()

    override var context by observable(listOf(
        GrainModelDescription("", "", null, null, "", emptyModel)
    )) { _, old, new ->
        if (old != new) {
            needSaveStatus = Array(new.size) { new[it]._id.isEmpty() }
            refresh()
        }
    }

    override var data by observable(context.first()) { _, old, new ->
        if (old != new && !selectingModel) {
            val index = context.indexOf(old)
            val newContext = context.toMutableList()
            newContext[index] = new
            needSaveStatus[index] = true
            context = newContext
            refresh()
        }
    }

    fun selectModel(id: String) {
        selectingModel = true
        val selectedModel = context.find { it._id == id }
        if (selectedModel != null) {
            data = selectedModel
            modelController.data = selectedModel.model
        }
        selectingModel = false
    }


    val simulationController = SimulationRunController()

    val modelController = GrainModelEditController { _, new, _ ->
        data = data.copy(model = new)
        simulationController.data = Simulator(new, simulationController.data.simulation, true)
    }

    val modelSelect = Select(rounded = true) { _, value -> selectModel(value) }

    val modelSave = textButton("Save", color = ElementColor.Primary, rounded = true) {
        val index = context.indexOf(data)
        needSaveStatus[index] = false
        if (data._id.isEmpty()) {
            saveGrainModel(data.model, instance).then { data = it }
        } else {
            updateGrainModel(data, instance)
        }
    }

    val modelField = Field(Control(modelSelect), Control(modelSave), addons = true)

    override val container: BulmaElement = div(
        Level(left = listOf(Title("Model")), right = listOf(modelField)),
        modelController,
        Title("Simulation"), simulationController
    )

    init {
        modelController.data = data.model
        simulationController.data = Simulator(data.model, Simulation(), true)

        refresh()

        fetchGrainModels(instance).then { context += it }
    }

    override fun refresh() {
        modelSelect.options = context.map { Option(it.model.name, it._id) }
        modelSelect.selectedIndex = context.indexOf(data)
    }

}
