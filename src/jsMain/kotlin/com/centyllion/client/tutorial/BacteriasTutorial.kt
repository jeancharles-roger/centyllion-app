package com.centyllion.client.tutorial

import bulma.BulmaElement
import bulma.Icon
import bulma.SubTitle
import bulma.p
import bulma.span
import bulma.wrap
import com.centyllion.client.controller.model.BehaviourEditController
import com.centyllion.client.controller.model.GrainEditController
import com.centyllion.client.controller.model.Simulator3dViewController
import com.centyllion.client.page.ShowPage
import com.centyllion.model.Grain
import kotlinx.html.li
import kotlinx.html.ul

class BacteriasTutorial(
    override val page: ShowPage
): Tutorial<ShowPage> {
    override val name = i18n("Create a simple bacterias simulation")

    private var originalName: String? = null
    private var bacteriaGrain: Grain? = null

    override val introduction: List<BulmaElement> = listOf(
        p("With this tutorial you will create a grain and a behaviour to simulate bacterias division")
    )

    override val steps: List<TutorialStep> = listOf(
        TutorialStep(
            "Create a bacteria grain", listOf(span("Adds a grain to the simulation")),
            { page.modelController.addGrainButton.root },
            { page.model.model.grains.isNotEmpty() },
            {
                bacteriaGrain = page.model.model.grains.first()
                originalName = bacteriaGrain?.name
            }
        ),
        TutorialStep(
            "Change the name", listOf(span("You can change the grain name, for instance to 'bact'")),
            {
                val editor = page.modelController.editor
                if (editor is GrainEditController) editor.nameController.root
                else page.modelController.addGrainButton.root
            },
            { page.model.model.grains.first().name != originalName },
            { bacteriaGrain = page.model.model.grains.first() }
        ),
        TutorialStep(
            "Go to simulation", listOf(span("Open the simulation page")),
            { page.simulationPage.title.root },
            { page.editionTab.selectedPage == page.simulationPage }
        ),
        TutorialStep(
            "Draw some grains",
            listOf(span("Select the "), Icon(Simulator3dViewController.EditTools.Pen.icon), span(" tool")),
            { page.simulationController.simulationViewController.toolButtons[1].root },
            { page.simulationController.simulationViewController.tool == Simulator3dViewController.EditTools.Pen }
        ),
        TutorialStep(
            "Draw some grains", listOf(span("Draw about 20 bacterias")),
            { page.simulationController.simulationViewController.simulationCanvas.root },
            { page.simulationController.simulator.grainsCounts()[bacteriaGrain?.id ?: 0] ?: 0 > 15 }
        ),
        TutorialStep(
            "Run the simulation", listOf(span("Watch the bacterias move")),
            { page.simulationController.runButton.root },
            { page.simulationController.simulator.step > 5 }
        ),
        TutorialStep(
            "Stop the simulation", listOf(span("Let's add a behaviour for bacteria division")),
            { page.simulationController.stopButton.root },
            { !page.simulationController.isRunning }
        ),
        TutorialStep(
            "Go to model", listOf(span("Open the model page to add a behaviour")),
            { page.modelPage.title.root },
            { page.editionTab.selectedPage == page.modelPage }
        ),
        TutorialStep(
            "Create a division behaviour", listOf(span("Add a behaviour to the simulation")),
            { page.modelController.addBehaviourButton.root },
            { page.model.model.behaviours.isNotEmpty() }
        ),
        TutorialStep(
            "First product", listOf(span("Select the bacteria grain as first product")),
            {
                val editor = page.modelController.editor
                if (editor is BehaviourEditController) editor.mainProductController.root
                else page.modelController.addBehaviourButton.root
            },
            { page.model.model.behaviours.firstOrNull()?.mainProductId == bacteriaGrain?.id }
        ),
        TutorialStep(
            "Adds a second product", listOf(span("Add a second line in the behaviour")),
            {
                val editor = page.modelController.editor
                if (editor is BehaviourEditController) editor.addReactionButton.root
                else page.modelController.addBehaviourButton.root
            },
            { (page.model.model.behaviours.firstOrNull()?.reaction?.size ?: 0) > 0 }
        ),
        TutorialStep(
            "Second product", listOf(span("Select the bacteria grain as second product")),
            {
                page.modelController.editor?.let {
                    if (it is BehaviourEditController)
                        it.reactionsController.dataControllers.firstOrNull()?.productController?.root
                    else null
                } ?: page.modelController.addBehaviourButton.root
            },
            {
                val reaction = page.model.model.behaviours.firstOrNull()?.reaction?.firstOrNull()
                reaction != null && reaction.productId == bacteriaGrain?.id
            }
        ),
        TutorialStep(
            "Go to simulation", listOf(span("Open the simulation page")),
            { page.simulationPage.title.root },
            { page.editionTab.selectedPage == page.simulationPage }
        ),
        TutorialStep(
            "Run the simulation", listOf(span("Watch the bacteria colony grow")),
            { page.simulationController.runButton.root },
            { page.simulationController.simulator.step > 30 },
            { page.simulationController.stop() }
        )
    )

    override val conclusion: List<BulmaElement> = listOf(
        SubTitle("You've just created a simulation with Centyllion, well done 👍."),
        p("You can now for instance:"),
        wrap {
            ul {
                li { +"test" }
            }
        }
    )

    override fun toString(): String = "bacterias Tutorial (${bacteriaGrain?.name})"
}
