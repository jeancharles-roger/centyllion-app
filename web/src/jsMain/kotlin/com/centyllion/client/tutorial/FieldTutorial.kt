package com.centyllion.client.tutorial

import bulma.*
import com.centyllion.client.controller.model.BehaviourEditController
import com.centyllion.client.controller.model.FieldEditController
import com.centyllion.client.controller.model.GrainEditController
import com.centyllion.client.page.ShowPage
import com.centyllion.client.takeInstance
import com.centyllion.model.Grain
import kotlinx.html.*

class FieldTutorial(
    override val page: ShowPage
): Tutorial<ShowPage> {
    override val name = "Feed bacterias with sugar"

    private var originalFieldName: String? = null

    private var sourceGrainId: Int? = null
    val sourceDraw = 5

    override val introduction: List<BulmaElement> = listOf(
        p(i18n("With this tutorial you will add to the simulation a field to feed the bacterias.")),
        Columns(
            Column(Image("https://centyllion.com/assets/images/sugar-tutorial.png"), size = ColumnSize.Half),
            centered = true
        )
    )

    override val steps: List<TutorialStep> = listOf(
        TutorialStep(
            i18n("Expert Mode"), listOf(span(i18n("Activate expert mode to access fields."))),
            { page.expertModeSwitch.root },
            { page.expertMode }
        ),
        TutorialStep(
            i18n("Create a sugar field"),
            listOf(
                span(i18n("Click on ")),
                Icon("plus", color = TextColor.Primary),
                span(i18n(" to add a field to the simulation."))
            ),
            { page.modelController.addFieldButton.root },
            { page.model.model.fields.isNotEmpty() },
            { originalFieldName = page.model.model.fields.first().name }
        ),
        TutorialStep(
            i18n("Change the name"), listOf(span(i18n("You can change the field name, for instance to 'sugar'."))),
            {
                page.modelController.editorController
                    .takeInstance<FieldEditController>()
                    ?.nameController?.root
                    ?: page.modelController.addFieldButton.root
            },
            { page.model.model.fields.first().name != originalFieldName }
        ),
        TutorialStep(
            i18n("Create a source grain"),
            listOf(
                span(i18n("Click on ")),
                Icon("plus", color = TextColor.Primary),
                span(i18n(" to add another grain to produce the 'sugar' field."))
            ),
            { page.modelController.addGrainButton.root },
            { page.model.model.grains.size >= 2 },
            { sourceGrainId = page.model.model.grains[1].id }
        ),
        TutorialStep(
            i18n("Produce 'sugar'"), listOf(span(i18n("Set the production of 'sugar' field to 1."))),
            {
                page.modelController.editorController
                    .takeInstance<GrainEditController>()
                    ?.fieldProductionsController?.dataControllers?.firstOrNull()?.root
                    ?: page.modelController.addGrainButton.root
            },
            { (page.model.model.grains[1].fieldProductions[0] ?: 0f) >= 1f }
        ),
        TutorialStep(
            i18n("Select the bacteria grain"), listOf(span(i18n("Make the bacterias attracted to sugar."))),
            { page.modelController.grainsController.dataControllers.first().root },
            { (page.modelController
                .editorController
                .takeInstance<GrainEditController>()
                ?.data) == page.model.model.grains.first() }
        ),
        TutorialStep(
            i18n("Influenced by 'sugar'"), listOf(span(i18n("Set the influence of 'sugar' above to 0.5."))),
            {
                page.modelController.editorController
                    .takeInstance<GrainEditController>()
                    ?.fieldInfluencesController?.dataControllers?.first()?.root
                    ?: page.modelController.addGrainButton.root
            },
            { (page.model.model.grains[0].fieldInfluences[0] ?: 0f) >= 0.5f }
        ),
        TutorialStep(
            i18n("Select the behavior"), listOf(span(i18n("Let's constrain the division with the 'sugar' field."))),
            { page.modelController.behavioursController.dataControllers.first().root },
            { page.modelController.editorController is BehaviourEditController }
        ),
        TutorialStep(
            i18n("Adds a field threshold"), listOf(span(i18n("Add a field constrain predicate to limit the behaviour to be only executed when the field is present."))),
            {
                page.modelController.editorController
                    .takeInstance<BehaviourEditController>()
                    ?.addFieldPredicateButton?.root
                    ?: page.modelController.addBehaviourButton.root
            },
            { page.model.model.behaviours.first().fieldPredicates.isNotEmpty() }
        ),
        TutorialStep(
            i18n("Sets the threshold to 0.01"), listOf(span(i18n("The field value around a grain that produces it diminishes rapidly."))),
            {
                page.modelController.editorController
                    .takeInstance<BehaviourEditController>()
                    ?.fieldPredicatesController?.dataControllers?.firstOrNull()?.predicateController?.value?.root
                    ?: page.modelController.addBehaviourButton.root
            },
            { page.model.model.behaviours.first().fieldPredicates.first().second.constant.let { it >= 0.01f && it < 0.1f } }
        ),
        TutorialStep(
            i18n("Go to simulation"), listOf(span(i18n("Open the simulation page to test the model."))),
            { page.modelController.simulationItem.title.root },
            { page.modelController.editionTabPages.selectedPage == page.modelController.simulationItem }
        ),
        TutorialStep(
            i18n("Select the source grain"), listOf(span(i18n("Let's add some sources.", sourceDraw + 5))),
            {
                page.modelController.grainsController.dataControllers
                    .find { it.data.id == sourceGrainId }?.root
                    ?: page.modelController.grainsController.root
            },
            { page.modelController.selected?.takeInstance<Grain>()?.id == sourceGrainId }
        ),
        TutorialStep(
            i18n("Draw some source"), listOf(span(i18n("Draw %0 source with the random spray.", sourceDraw))),
            { page.simulationController.simulationViewController.randomAddButton.root },
            { (page.simulationController.simulator.grainsCounts()[sourceGrainId ?: 0] ?: 0) >= sourceDraw }
        ),
        TutorialStep(
            i18n("Run the simulation"), listOf(span(i18n("Watch the bacteria colony grow around the sugar sources."))),
            { page.simulationController.runButton.root },
            { page.simulationController.simulator.step > 120 },
            { page.simulationController.stop() }
        )
    )

    override val conclusion: List<BulmaElement> = listOf(
        SubTitle(i18n("Now you know how to use fields with Centyllion, well done 👍.")),
        wrap("content") {
            p { +i18n("You can now for instance:") }
            ul {
                li { +i18n("Change the field threshold for sugar division (try 0.001 or 1e-6 (0.000001).") }
                li { +i18n("Prevents the sources from moving.") }
                li { +i18n("Makes the bacterias consume the sugar (production to -0.5).") }
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

    override val next: Tutorial<ShowPage>? = null
}
