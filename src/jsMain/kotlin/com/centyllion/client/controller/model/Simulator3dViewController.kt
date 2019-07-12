package com.centyllion.client.controller.model

import bulma.Div
import bulma.HtmlWrapper
import bulma.canvas
import com.centyllion.client.AppContext
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Asset3d
import com.centyllion.model.Simulator
import com.centyllion.model.minFieldLevel
import info.laht.threekt.THREE.DoubleSide
import info.laht.threekt.THREE.FloatType
import info.laht.threekt.THREE.LuminanceFormat
import info.laht.threekt.THREE.NearestFilter
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.external.loaders.GLTFLoader
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.geometries.CylinderBufferGeometry
import info.laht.threekt.geometries.PlaneBufferGeometry
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.lights.DirectionalLight
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.ColorConstants
import info.laht.threekt.objects.Mesh
import info.laht.threekt.renderers.WebGLRenderer
import info.laht.threekt.renderers.WebGLRendererParams
import info.laht.threekt.scenes.Scene
import kotlinx.io.IOException
import org.khronos.webgl.Float32Array
import org.w3c.dom.HTMLCanvasElement
import threejs.extra.core.Font
import threejs.geometries.TextBufferGeometry
import threejs.geometries.TextGeometryParametersImpl
import threejs.textures.DataTexture
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.math.PI
import kotlin.math.log10
import kotlin.properties.Delegates.observable

open class Simulator3dViewController(
    simulator: Simulator, val appContext: AppContext
) : SimulatorViewController(simulator) {

    class FieldSupport(val mesh: Mesh, val alphaTexture: DataTexture, val array: Float32Array) {
        fun dispose() {
            alphaTexture.dispose()
            mesh.geometry.dispose()
        }
    }

    override var data: Simulator by observable(simulator) { _, _, _ ->
        camera.position.set(0, 1.25 * data.simulation.width, 0.5 * data.simulation.width)
        geometries = geometries()
        materials = materials()
        refreshAssets()
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            // TODO
        }
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 40).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    override val container = Div(
        Div(simulationCanvas, classes = "has-text-centered")
    )

    private var font: Font? = null

    val defaultGeometry = BoxBufferGeometry(1, 1, 1)
    val defaultMaterial = MeshPhongMaterial().apply { color.set("red") }

    val agentMesh: MutableMap<Int, Mesh> = mutableMapOf()

    val scenesCache: MutableMap<String, Scene> = mutableMapOf()

    val assetScenes: MutableMap<Asset3d, Scene> = mutableMapOf()
    init { refreshAssets() }

    var fieldSupports: Map<Int, FieldSupport> by observable(mapOf()) { _, old, _ ->
        old.values.forEach { it.dispose() }
    }

    val camera = PerspectiveCamera(45, 1.0, 00.1, 1000.0).apply {
        position.set(0, 1.25 * data.simulation.width, 0.5 * data.simulation.width)
        lookAt(0, 0, 0)
    }

    val scene = Scene().apply {
        add(AmbientLight(0x444444))

        // adds directional light
        val directionalLight = DirectionalLight(0xffffff, 1)
        directionalLight.position.set(
            1.25 * data.simulation.width,
            2 * data.simulation.width,
            1.25 * data.simulation.width
        )
        directionalLight.castShadow = true
        add(directionalLight)
    }

    val orbitControl = OrbitControls(camera, simulationCanvas.root)

    val renderer = WebGLRenderer(WebGLRendererParams(simulationCanvas.root, antialias = true)).apply {
        setClearColor(ColorConstants.white, 1)
    }

    private var geometries by observable(geometries()) { _, old, _ ->
        old.values.filterNotNull().forEach { it.dispose() }
    }

    private var materials by observable(materials()) { _, old, _ ->
        old.values.forEach { it.dispose() }
    }

    init {
        appContext.getFont("/font/fa-solid-900.json").then {
            font = it
            geometries = geometries()
            refresh()
        }
        materials = materials()
    }

    private fun geometries() = data.model.grains.map { grain ->
        grain.id to font?.let {
            val height = grain.size
            when (grain.icon) {
                "square" -> BoxBufferGeometry(0.8, height, 0.8)
                "square-full" -> BoxBufferGeometry(1, height, 1)
                "circle" -> CylinderBufferGeometry(0.5, 0.5, height)
                else -> TextBufferGeometry(grain.iconString, TextGeometryParametersImpl(it, 0.8, height)).apply {
                    rotateX(-PI / 2.0)
                }
            }

        }
    }.toMap()

    private fun materials() = data.model.grains.map {
        it.id to MeshPhongMaterial().apply {
            color.set(it.color.toLowerCase())
        }
    }.toMap()

    private fun loadAsset(path: String): Promise<Scene> = Promise { resolve, reject ->
        val scene = scenesCache[path]
        if (scene != null) {
            resolve(scene)
        } else {
            GLTFLoader().load(appContext.api.url(path),
                {
                    println("Asset $path loaded.")
                    scenesCache[path] = it.scene
                    resolve(it.scene)
                },
                {}, { reject(IOException(it.toString())) }
            )
        }
    }

    private fun refreshAssets() {
        // clears previous assets
        assetScenes.values.forEach { scene.remove(it) }
        assetScenes.clear()

        // sets new assets
        data.simulation.assets.map { asset ->
            loadAsset(asset.url).then {
                it.position.set(asset.x, asset.y, asset.z)
                it.scale.set(asset.xScale, asset.yScale, asset.zScale)
                it.rotation.set(asset.xRotation, asset.yRotation, asset.zRotation)
                scene.add(it)
                assetScenes[asset] = it
            }.catch { appContext.error(it) }
        }
    }

    fun createMesh(index: Int, grainId: Int, x: Double, y: Double): Mesh {
        // creates mesh
        val mesh = Mesh(geometries[grainId] ?: defaultGeometry, materials[grainId] ?: defaultMaterial)
        mesh.receiveShadows = true
        mesh.castShadow = true

        // positions the mesh
        mesh.position.set(x, 0, y)

        mesh.updateMatrix()
        mesh.matrixAutoUpdate = false

        // adds the mesh to scene and register it
        scene.add(mesh)
        agentMesh[index] = mesh
        return mesh
    }

    fun transformMesh(index: Int, newGrainId: Int) {
        val mesh = agentMesh[index]
        when {
            mesh != null && newGrainId >= 0 -> {
                // transform it to new one
                mesh.geometry = geometries[newGrainId] ?: defaultGeometry
                mesh.material = materials[newGrainId] ?: defaultMaterial
            }
            mesh != null && newGrainId < 0 -> {
                // deletes the mesh
                agentMesh.remove(index)
                scene.remove(mesh)
            }
            mesh == null && newGrainId >= 0 -> {
                val p = data.simulation.toPosition(index)
                createMesh(index, newGrainId, p.x - data.simulation.width / 2.0, p.y - data.simulation.height / 2.0 + 1.0)
            }
        }
    }

    override fun oneStep(applied: List<ApplicableBehavior>, deads: List<Int>) {
        // Updates agents

        // applies behaviors
        applied.forEach { one ->
            // applies main reaction
            transformMesh(one.index, one.behaviour.mainProductId)

            // applies secondary reactions
            // TODO find if the sort can be avoided
            val reactives = one.usedNeighbours.sortedBy { it.id }
            val reactions = one.behaviour.reaction.sortedBy { it.reactiveId }
            reactions.zip(reactives).forEach { (reaction, reactive) ->
                transformMesh(reactive.index, reaction.productId)
            }
        }

        // applies deaths
        deads.forEach { transformMesh(it, -1) }

        // updates fields
        fieldSupports.forEach {
            val id = it.key
            // updates alpha
            it.value.array.set(data.field(id).map {
                when {
                    it > +1f -> 1f
                    it <= minFieldLevel -> 0f
                    else -> 1f / (-log10(it)) / 1.6f
                }
            }.toTypedArray())
            // invalidate texture
            it.value.alphaTexture.needsUpdate = true
        }
    }

    override fun animate() {
        renderer.render(scene, camera)
    }

    override fun refresh() {
        // clear agents
        agentMesh.values.forEach { scene.remove(it) }
        agentMesh.clear()

        // adds agents meshes
        var currentX = -data.simulation.width / 2.0
        var currentY = -data.simulation.height / 2.0 + 1.0
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]

            if (grain != null) {
                createMesh(i, grain.id, currentX, currentY)
            }

            currentX += 1.0
            if (currentX >= data.simulation.width / 2.0) {
                currentX = -data.simulation.width / 2.0
                currentY += 1.0
            }
        }

        // clear fields
        fieldSupports.values.forEach { scene.remove(it.mesh) }

        // adds field meshes
        fieldSupports = data.model.fields.map { field ->
            val levels = data.fields[field.id]!!
            val alpha = Float32Array(levels.map {
                when {
                    it > +1f -> 1f
                    it <= minFieldLevel -> 0f
                    else -> 1f / (-log10(it)) / 1.6f
                }
            }.toTypedArray())

            val alphaTexture = DataTexture(
                alpha, data.simulation.width, data.simulation.height, LuminanceFormat, FloatType
            ).apply {
                needsUpdate = true
                magFilter = NearestFilter
            }

            val material = MeshBasicMaterial().apply {
                side = DoubleSide
                color.set(field.color.toLowerCase())
                this.alphaMap = alphaTexture

                transparent = true
            }

            val geometry = PlaneBufferGeometry(100, 100)
            geometry.rotateX(PI / 2.0)
            geometry.translate(0.0, -0.1, 0.0)
            val mesh = Mesh(geometry, material)
            mesh.receiveShadows = true
            scene.add(mesh)
            field.id to FieldSupport(mesh, alphaTexture, alpha)
        }.toMap()

    }

    override fun dispose() {
        orbitControl.dispose()
        materials = emptyMap()
        defaultMaterial.dispose()
        geometries = emptyMap()
        defaultGeometry.dispose()
    }

}
