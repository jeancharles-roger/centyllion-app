package com.centyllion.client.tutorial

import bulma.*
import com.centyllion.client.controller.model.BehaviourEditController
import com.centyllion.client.controller.model.GrainEditController
import com.centyllion.client.controller.model.ReactionEditController
import com.centyllion.client.page.ShowPage
import com.centyllion.client.takeInstance
import com.centyllion.model.Grain
import com.centyllion.model.ModelAndSimulation
import kotlinx.html.*

class BacteriasTutorial(
    override val page: ShowPage
): Tutorial<ShowPage> {
    override val name = "Create a simple bacterias simulation"

    val bacteriaDraw = 15

    private var originalName: String? = null
    private var bacteriaGrain: Grain? = null

    override val introduction: List<BulmaElement> = listOf(
        p(i18n("With this tutorial you will create a simulation bacterias division with only one grain and one behaviour.")),
        Columns(
            Column(Image("https://centyllion.com/assets/images/bacterias-tutorial.jpg"), size = ColumnSize.Half),
            centered = true
        )
    )

    override fun prepare() {
        page.model = ModelAndSimulation.empty
    }

    override val steps: List<TutorialStep> = listOf(
        TutorialStep(
            i18n("Create a bacteria grain"),
            listOf(
                span(i18n("Click on ")),
                Icon("plus", color = TextColor.Primary),
                span(i18n(" to add a grain to the simulation."))
            ),
            { page.modelController.addGrainButton.root },
            { page.model.model.grains.isNotEmpty() },
            {
                bacteriaGrain = page.model.model.grains.first()
                originalName = bacteriaGrain?.name
            }
        ),
        TutorialStep(
            i18n("Change the name"), listOf(span(i18n("You can change the grain name, for instance to 'bact'."))),
            {
                page.modelController.editorController
                    .takeInstance<GrainEditController>()
                    ?.nameController?.root
                    ?:page.modelController.addGrainButton.root
            },
            { page.model.model.grains.first().name != originalName },
            { bacteriaGrain = page.model.model.grains.first() }
        ),
        TutorialStep(
            i18n("Set the speed"), listOf(span(i18n("Change the speed to 1 to let the bacteria move."))),
            {
                page.modelController.editorController
                    .takeInstance<GrainEditController>()
                    ?.speedController?.root
                    ?:page.modelController.addGrainButton.root
            },
            { page.model.model.grains.first().canMove },
            { bacteriaGrain = page.model.model.grains.first() }
        ),
        TutorialStep(
            i18n("Go to simulation"), listOf(span(i18n("Open the simulation page to test the model."))),
            { page.modelController.simulationItem.title.root },
            { page.modelController.editionTabPages.selectedPage == page.modelController.simulationItem }
        ),
        TutorialStep(
            i18n("Draw some grains"), listOf(span(i18n("Draw %0 bacterias with the random spray.", bacteriaDraw + 5))),
            { page.simulationController.simulationViewController.randomAddButton.root },
            { (page.simulationController.simulator.grainsCounts()[bacteriaGrain?.id ?: 0] ?: 0) > bacteriaDraw }
        ),
        TutorialStep(
            i18n("Run the simulation"), listOf(span(i18n("Watch the bacterias move."))),
            { page.simulationController.runButton.root },
            { page.simulationController.simulator.step > 5 }
        ),
        TutorialStep(
            i18n("Stop the simulation"), listOf(span(i18n("Ok, the bacterias moves."))),
            { page.simulationController.stopButton.root },
            { !page.simulationController.isRunning }
        ),
        TutorialStep(
            i18n("Go to model"), listOf(span(i18n("Open the model page to add a division behaviour to make them grow."))),
            { page.modelController.editionItem.title.root },
            { page.modelController.editionTabPages.selectedPage == page.modelController.editionItem }
        ),
        TutorialStep(
            i18n("Create a division behaviour"),
            listOf(
                span(i18n("Click on ")),
                Icon("plus", color = TextColor.Primary),
                span(i18n(" to add a behaviour to the simulation."))
            ),
            { page.modelController.addBehaviourButton.root },
            { page.model.model.behaviours.isNotEmpty() }
        ),
        TutorialStep(
            i18n("First product"), listOf(span(i18n("Select the bacteria grain as first product."))),
            {
                val editor = page.modelController.editorController
                if (editor is BehaviourEditController) editor.mainProductController.root
                else page.modelController.addBehaviourButton.root
            },
            { page.model.model.behaviours.firstOrNull()?.mainProductId == bacteriaGrain?.id }
        ),
        TutorialStep(
            i18n("Adds a second product"),
            listOf(
                span(i18n("Click on ")),
                Icon("plus", color = TextColor.Info),
                span(i18n(" to add a second line in the behaviour"))
            ),
            {
                val editor = page.modelController.editorController
                if (editor is BehaviourEditController) editor.addReactionButton.root
                else page.modelController.addBehaviourButton.root
            },
            { (page.model.model.behaviours.firstOrNull()?.reaction?.size ?: 0) > 0 }
        ),
        TutorialStep(
            i18n("Second product"), listOf(span(i18n("Select the bacteria grain as second product."))),
            {
                page.modelController.editorController
                    .takeInstance<BehaviourEditController>()
                    ?.reactionsController?.dataControllers?.firstOrNull()
                    .takeInstance<ReactionEditController>()
                    ?.productController?.root
                    ?: page.modelController.addBehaviourButton.root
            },
            {
                val reaction = page.model.model.behaviours.firstOrNull()?.reaction?.firstOrNull()
                reaction != null && reaction.productId == bacteriaGrain?.id
            }
        ),
        TutorialStep(
            i18n("Return to simulation"), listOf(span(i18n("Go back to the simulation page."))),
            { page.modelController.simulationItem.title.root },
            { page.modelController.editionTabPages.selectedPage == page.modelController.simulationItem }
        ),
        TutorialStep(
            i18n("Run the simulation"), listOf(span(i18n("Watch the bacteria colony grow."))),
            { page.simulationController.runButton.root },
            { page.simulationController.simulator.step > 20 },
            { page.simulationController.stop() }
        )
    )

    override val conclusion: List<BulmaElement> = listOf(
        SubTitle(i18n("You've just created a simulation with Centyllion, well done 👍.")),
        wrap("content") {
            p { +i18n("You can now for instance:") }
            ul {
                li { +i18n("Set a half-life for bacterias to give them a life-span.") }
                li { +i18n("Add a sugar field to feed the bacterias.") }
                li { +i18n("Create another bacteria to compete with.") }
            }
            p {
                +i18n("You can find some documentation here ")
                a("https://centyllion.com/fr/documentation.html","_blank") {
                    span("icon is-primary") { i("fas fa-book") }
                }
                + "."
            }
        }
    )

    override val next: Tutorial<ShowPage> get() = FieldTutorial(page)
}
