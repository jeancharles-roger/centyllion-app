package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Title
import bulma.div
import bulma.wrap
import com.centyllion.client.AppContext
import com.centyllion.model.Simulator
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.helpers.GridHelper
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.ColorConstants
import info.laht.threekt.objects.Mesh
import info.laht.threekt.renderers.WebGLRenderer
import info.laht.threekt.renderers.WebGLRendererParams
import info.laht.threekt.scenes.Scene
import kotlinx.html.dom.create
import kotlinx.html.js.canvas
import org.w3c.dom.url.URLSearchParams
import kotlin.browser.document
import kotlin.browser.window

class TestPage(context: AppContext) : BulmaElement {

    val camera = PerspectiveCamera(45, 4.0 / 3.0, 00.1, 1000.0).apply {
        position.setZ(5)
    }

    val grid = GridHelper(100, 10)

    val grainGeometry = BoxBufferGeometry(1, 1, 1)

    val cube = Mesh(
        grainGeometry,
        MeshPhongMaterial().apply { color.set(ColorConstants.darkgreen) }
    ).apply {
        val geometry = geometry as BufferGeometry
        val mesh = Mesh(geometry, MeshBasicMaterial().apply {
            wireframe = true
            color.set(ColorConstants.black)
        })
        add(mesh)
    }

    val scene = Scene().apply {
        add(AmbientLight())
        add(grid)
    }

    val canvas = document.create.canvas {
        width = "800px"
        height = "600px"
    }

    val orbitControl = OrbitControls(camera, canvas)

    val renderer = WebGLRenderer(WebGLRendererParams(canvas, antialias = true)).apply {
        setClearColor(ColorConstants.white, 1)
    }

    val container = div(
        Title("Test"),
        wrap(canvas)
    )

    override val root = container.root

    fun animate() {
        window.requestAnimationFrame {
            cube.rotation.x += 0.01
            cube.rotation.y += 0.01
            animate()
        }
        renderer.render(scene, camera)
    }


    init {
        val params = URLSearchParams(window.location.search)
        val simulationId = params.get("simulation") ?: "778c287d-97fa-4c31-b4d9-5938ed66fbc9"
        context.api.fetchSimulation(simulationId).then { simulation ->
            context.api.fetchGrainModel(simulation.modelId).then { simulation to it }
        }.then {(simulation, model) ->

            // material map
            val materials = model.model.grains.map {
                it to MeshPhongMaterial().apply { color.set(it.color.toLowerCase()) }
            }.toMap()

            val simulator = Simulator(model.model, simulation.simulation)

            var currentX = - simulation.simulation.width/2
            var currentY = - simulation.simulation.height/2
            for (i in 0 until simulator.currentAgents.size) {
                val grain = model.model.indexedGrains[simulator.idAtIndex(i)]

                if (grain != null) {
                    val cube = Mesh(grainGeometry, materials[grain]!!)
                    cube.position.set(currentX, 0, currentY)
                    scene.add(cube)
                }

                currentX += 1
                if (currentX >= simulation.simulation.width/2) {
                    currentX = - simulation.simulation.width/2
                    currentY += 1
                }
            }

        }




        animate()
    }
}
