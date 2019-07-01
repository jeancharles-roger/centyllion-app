package com.centyllion.client.controller.model

import bulma.HtmlWrapper
import bulma.canvas
import bulma.div
import com.centyllion.client.AppContext
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
import threejs.extra.core.Font
import threejs.geometries.TextBufferGeometry
import threejs.geometries.TextGeometryParametersImpl
import kotlin.browser.window
import kotlin.math.PI
import kotlin.properties.Delegates

open class Simulator3dViewController(
    simulator: Simulator, appContext: AppContext
) : SimulatorViewController(simulator) {

    override var data: Simulator by Delegates.observable(simulator) { _, _, _ ->
        camera.position.set(0, data.simulation.width, data.simulation.width)
        geometries = geometries()
        materials = materials()
        refresh()
    }

    override var readOnly: Boolean by Delegates.observable(false) { _, old, new ->
        if (old != new) {
            // TODO
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

    private var font: Font? = null

    val defaultGeometry = BoxBufferGeometry(1, 1, 1)
    val defaultMaterial = MeshPhongMaterial().apply { color.set("red") }

    val agentMesh: MutableMap<Int, Mesh> = mutableMapOf()

    val camera = PerspectiveCamera(45, 4.0 / 3.0, 00.1, 1000.0).apply {
        position.set(0, data.simulation.width, data.simulation.width)
        lookAt(0, 0, 0)
    }

    val grid = GridHelper(100, 10)

    val scene = Scene().apply {
        add(AmbientLight())
        add(grid)
    }

    val orbitControl = OrbitControls(camera, simulationCanvas.root)

    val renderer = WebGLRenderer(WebGLRendererParams(simulationCanvas.root, antialias = true)).apply {
        setClearColor(ColorConstants.white, 1)
    }

    private fun geometries() = data.model.grains.map { grain ->
        grain.id to font?.let {
            val height = grain.size
            TextBufferGeometry(grain.iconString ?: "\uf45c", TextGeometryParametersImpl(it, 1, height))
        }
    }.toMap()

    private fun materials() = data.model.grains.map {
        it.id to MeshPhongMaterial().apply { color.set(it.color.toLowerCase()) }
    }.toMap()

    private var geometries = geometries()
    private var materials = materials()

    fun createMesh(id: Int, x: Number, y: Number) =
        Mesh(geometries[id] ?: defaultGeometry, materials[id] ?: defaultMaterial).apply {
            position.set(x, 0, y)
            rotateX(-PI / 2.0)
        }

    fun morphMesh(mesh: Mesh, id: Int) {
        mesh.geometry = geometries[id] ?: defaultGeometry
        mesh.material = materials[id] ?: defaultMaterial
    }

    override fun oneStep(applied: List<ApplicableBehavior>) {
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
                if (mesh != null) {
                    if (reaction.productId >= 0) {
                        morphMesh(mesh, reaction.productId)
                    } else {
                        agentMesh.remove(one.index)
                        scene.remove(mesh)
                    }
                } else {
                    val position = data.simulation.toPosition(reactive.index)
                    val newMesh = createMesh(
                        reaction.productId,
                        position.x - data.simulation.width / 2,
                        position.y - data.simulation.height / 2
                    )
                    scene.add(newMesh)
                    agentMesh[reactive.index] = newMesh
                }
            }
        }
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

        var currentX = -data.simulation.width / 2
        var currentY = -data.simulation.height / 2
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]

            if (grain != null) {
                val cube = createMesh(grain.id, currentX, currentY)
                scene.add(cube)
                agentMesh[i] = cube
            }

            currentX += 1
            if (currentX >= data.simulation.width / 2) {
                currentX = -data.simulation.width / 2
                currentY += 1
            }
        }
    }

    init {
        appContext.getFont("font/fa-solid-900.json").then {
            font = it
            geometries = geometries()
            refresh()
        }
        materials = materials()
        animate()
    }
}
