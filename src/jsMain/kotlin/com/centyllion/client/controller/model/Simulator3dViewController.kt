package com.centyllion.client.controller.model

import bulma.HtmlWrapper
import bulma.canvas
import bulma.div
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Simulator
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.helpers.GridHelper
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.ColorConstants
import info.laht.threekt.objects.Mesh
import info.laht.threekt.renderers.WebGLRenderer
import info.laht.threekt.renderers.WebGLRendererParams
import info.laht.threekt.scenes.Scene
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.window
import kotlin.properties.Delegates

open class Simulator3dViewController(simulator: Simulator) : SimulatorViewController(simulator) {

    override var data: Simulator by Delegates.observable(simulator) { _, _, _ ->
        camera.position.set(0, data.simulation.width, data.simulation.width)
        refresh()
    }

    override var readOnly: Boolean by Delegates.observable(false) { _, old, new ->
        if (old != new) {
            // TODO
        }
    }

    override fun oneStep(applied: List<ApplicableBehavior>) {

        // TODO materials should be a field
        // material map
        val materials = data.model.grains.map {
            it.id to MeshPhongMaterial().apply { color.set(it.color.toLowerCase()) }
        }.toMap()

        applied.forEach { one ->
            agentMesh[one.index]?.let {
                val material = materials[one.behaviour.mainProductId]
                if (material != null) {
                    it.material = material
                } else {
                    agentMesh.remove(one.index)
                    scene.remove(it)
                }
            }

            val reactives = one.usedNeighbours.sortedBy { it.id }
            val reactions = one.behaviour.reaction.sortedBy { it.reactiveId }

            // applies reactions
            reactions.zip(reactives).forEach { (reaction, reactive) ->
                val mesh = agentMesh[reactive.index]
                val material = materials[reaction.productId]
                if (mesh != null) {
                    if (material != null) {
                        mesh.material = material
                    } else {
                        agentMesh.remove(one.index)
                        scene.remove(mesh)
                    }
                } else {
                    if (material != null) {
                        val cube = Mesh(grainGeometry, material)
                        val position = data.simulation.toPosition(reactive.index)
                        cube.position.set(
                            position.x - data.simulation.width/2,
                            0,
                            position.y - data.simulation.height/2
                        )

                        scene.add(cube)
                        agentMesh[reactive.index] = cube
                    }
                }
            }
        }

    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 40).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    override val container = div(
        div(simulationCanvas, classes = "has-text-centered")
    )

    val agentMesh: MutableMap<Int, Mesh> = mutableMapOf()

    val camera = PerspectiveCamera(45, 4.0 / 3.0, 00.1, 1000.0).apply {
        position.set(0, data.simulation.width , data.simulation.width)
        lookAt(0, 0, 0)
    }

    val grid = GridHelper(100, 10)

    val grainGeometry = BoxBufferGeometry(1, 1, 1)

    val scene = Scene().apply {
        add(AmbientLight())
        add(grid)
    }

    val orbitControl = OrbitControls(camera, simulationCanvas.root)

    val renderer = WebGLRenderer(WebGLRendererParams(simulationCanvas.root, antialias = true)).apply {
        setClearColor(ColorConstants.white, 1)
    }

    fun animate() {
        window.requestAnimationFrame {
            animate()
        }
        renderer.render(scene, camera)
    }

    override fun refresh() {
        // clear scene
        agentMesh.values.forEach { scene.remove(it) }
        agentMesh.clear()

        // material map
        val materials = data.model.grains.map {
            it to MeshPhongMaterial().apply { color.set(it.color.toLowerCase()) }
        }.toMap()

        var currentX = - data.simulation.width/2
        var currentY = - data.simulation.height/2
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]

            if (grain != null) {
                val cube = Mesh(grainGeometry, materials[grain]!!)
                cube.position.set(currentX, 0, currentY)

                scene.add(cube)
                agentMesh[i] = cube
            }

            currentX += 1
            if (currentX >= data.simulation.width/2) {
                currentX = - data.simulation.width/2
                currentY += 1
            }
        }
    }

    init { animate() }
}
