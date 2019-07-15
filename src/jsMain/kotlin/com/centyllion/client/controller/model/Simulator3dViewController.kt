package com.centyllion.client.controller.model

import bulma.BulmaElement
import bulma.Control
import bulma.Div
import bulma.Dropdown
import bulma.DropdownSimpleItem
import bulma.ElementColor
import bulma.Field
import bulma.HtmlWrapper
import bulma.Icon
import bulma.Level
import bulma.NoContextController
import bulma.canvas
import bulma.iconButton
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
import info.laht.threekt.materials.Material
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.ColorConstants
import info.laht.threekt.math.Vector2
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.Mesh
import info.laht.threekt.renderers.WebGLRenderer
import info.laht.threekt.renderers.WebGLRendererParams
import info.laht.threekt.scenes.Scene
import kotlinx.io.IOException
import org.khronos.webgl.Float32Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import threejs.core.Raycaster
import threejs.extra.core.Font
import threejs.geometries.TextBufferGeometry
import threejs.geometries.TextGeometryParametersImpl
import threejs.textures.DataTexture
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.properties.Delegates.observable
import kotlin.random.Random

open class Simulator3dViewController(
    simulator: Simulator, val appContext: AppContext,
    var onUpdate: (ended: Boolean, new: Simulator, Simulator3dViewController) -> Unit = { _, _, _ -> }
): NoContextController<Simulator, BulmaElement>() {

    enum class EditTools(val icon: String) {
        Move("arrows-alt"), Pen("pen"), Line("pencil-ruler"), Spray("spray-can"), Eraser("eraser")
    }

    enum class ToolSize(val size: Int) {
        Fine(1), Small(5), Medium(10), Large(20)
    }

    class FieldSupport(val mesh: Mesh, val alphaTexture: DataTexture, val array: Float32Array) {
        fun dispose() {
            alphaTexture.dispose()
            mesh.geometry.dispose()
        }
    }

    override var data: Simulator by observable(simulator) { _, _, new ->
        onUpdate(true, new, this)

        selectedGrainController.context = new.model.grains
        selectedGrainController.data = new.model.grains.firstOrNull()
        geometries = geometries()
        materials = materials()
        refreshAssets()
        refresh()
    }

    override var readOnly: Boolean by observable(false) { _, old, new ->
        if (old != new) {
            editToolbar.invisible = new
        }
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 40).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    fun selectTool(tool: EditTools) {
        orbitControl.enabled = tool == EditTools.Move
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool
    }

    val selectedGrainController = GrainSelectController(simulator.model.grains.firstOrNull(), simulator.model.grains)

    val sizeDropdown = Dropdown(text = ToolSize.Fine.name, rounded = true).apply {
        items = ToolSize.values().map { size ->
            DropdownSimpleItem(size.name) {
                this.text = size.name

                pointer.geometry = CylinderBufferGeometry(0.5 * size.size, 0.5 * size.size,  1.0)
                this.toggleDropdown()
            }
        }
    }

    val toolButtons = EditTools.values().map { tool ->
        iconButton(
            Icon(tool.icon), ElementColor.Primary,
            rounded = true, outlined = tool.ordinal == 0
        ) { selectTool(tool) }
    }

    val clearAllButton = iconButton(Icon("trash"), ElementColor.Danger, true) {
        (0 until data.simulation.dataSize).forEach { data.resetIdAtIndex(it) }
        onUpdate(true, data, this)
        refresh()
    }

    val editToolbar = Level(
        center = listOf(Field(addons = true).apply {
            body = toolButtons.map { Control(it) }
        }, sizeDropdown, selectedGrainController.container, clearAllButton)
    )

    override val container = Div(
        Div(simulationCanvas, classes = "has-text-centered"), editToolbar
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

    val camera = PerspectiveCamera(45, 1.0, 0.1, 1000.0).apply {
        position.set(0, 0.0,  1.25 * data.simulation.width)
        lookAt(0, 0, 0)
    }

    val scene = Scene().apply {
        add(AmbientLight(0x444444))

        // adds directional light
        val directionalLight = DirectionalLight(0xffffff, 1)
        directionalLight.position.set(
            1.25 * data.simulation.width,
            -1.25 * data.simulation.width,
            2 * data.simulation.width
        )
        directionalLight.castShadow = true
        add(directionalLight)
    }

    val plane = Mesh(
        PlaneBufferGeometry(100, 100),
        MeshBasicMaterial().apply { visible = false }
    ).apply {
        scene.add(this)
    }

    val pointer = Mesh(CylinderBufferGeometry(0.5, 0.5, 1.0), MeshBasicMaterial().apply {
        color.set("#ff0000")
        opacity = 0.40
    }).apply {
        visible = false
        renderOrder = 5
        rotateX(PI/2)
        scene.add(this)
    }

    val orbitControl = OrbitControls(camera, simulationCanvas.root).also {
        it.asDynamic().addEventListener("change", this::render)
        Unit
    }

    val renderer = WebGLRenderer(WebGLRendererParams(simulationCanvas.root, antialias = true)).apply {
        setClearColor(ColorConstants.white, 1)
    }

    val rayCaster = Raycaster(Vector3(), Vector3(), 0.1, 1000.0)

    // simulation content edition
    private var selectedTool: EditTools = EditTools.Move

    private var toolElement: Mesh? = null

    var drawStep = -1

    var simulationSourceX = -1
    var simulationSourceY = -1

    var simulationX = -1
    var simulationY = -1

    fun circle(x: Int, y: Int, factor: Int = 1, block: (i: Int, j: Int) -> Unit) {
        val size = ToolSize.valueOf(sizeDropdown.text).size * factor
        val halfSize = (size / 2).coerceAtLeast(1)
        if (halfSize == 1) {
            block(x, y)
        } else {
            for (i in x - halfSize until x + halfSize) {
                for (j in y - halfSize until y + halfSize) {
                    val inCircle = (x - i) * (x - i) + (y - j - 1) * (y - j) + 1 < halfSize * halfSize
                    val inSimulation = data.simulation.positionInside(i, j)
                    if (inCircle && inSimulation) block(i, j)
                }
            }
        }
    }

    fun line(sourceX: Int, sourceY: Int, x: Int, y: Int, block: (i: Int, j: Int) -> Unit) {
        val dx = x - sourceX
        val dy = y - sourceY
        if (dx.absoluteValue > dy.absoluteValue) {
            if (sourceX < x) {
                for (i in sourceX until x) {
                    val j = sourceY + dy * (i - sourceX) / dx
                    block(i, j)
                }
            } else {
                for (i in x until sourceX) {
                    val j = y + dy * (i - x) / dx
                    block(i, j)
                }
            }
        } else {
            if (sourceY < y) {
                for (j in sourceY until y) {
                    val i = sourceX + dx * (j - sourceY) / dy
                    block(i, j)
                }
            } else {
                for (j in y until sourceY) {
                    val i = x + dx * (j - y) / dy
                    block(i, j)
                }
            }
        }
    }

    fun drawOnSimulation(step: Int) {
        when (selectedTool) {
            EditTools.Pen -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    circle(simulationX, simulationY) { i, j ->
                        data.setIdAtIndex(
                            data.simulation.toIndex(i, j),
                            idToSet
                        )
                    }
                }
            }
            EditTools.Line -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    if (step == -1) {
                        // draw the line
                        line(simulationSourceX, simulationSourceY, simulationX, simulationY) { i, j ->
                            data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                        }
                    }
                }

            }
            EditTools.Spray -> {
                val random = Random
                selectedGrainController.data?.id?.let { idToSet ->
                    val sprayDensity = 0.005
                    circle(simulationX, simulationY, 4) { i, j ->
                        if (Random.nextDouble() < sprayDensity) {
                            data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
                        }
                    }
                }
            }
            EditTools.Eraser -> {
                circle(simulationX, simulationY) { i, j ->
                    data.resetIdAtIndex(data.simulation.toIndex(i, j))
                }
            }
            EditTools.Move -> {
            }
        }

        if (step == -1) {
            toolElement = null
        }

        onUpdate(step == -1, data, this)
        refresh()
    }

    private fun mouseChange(event: MouseEvent) {
        // only update if there a tool
        if (selectedTool != EditTools.Move) {
            val rectangle = simulationCanvas.root.getBoundingClientRect()
            val x = ((event.clientX - rectangle.left) / rectangle.width) * 2 - 1
            val y = -((event.clientY - rectangle.top) / rectangle.height) * 2 + 1

            // updates the picking ray with the camera and mouse position
            rayCaster.setFromCamera(Vector2(x, y), camera)

            // calculates objects intersecting the picking ray
            val intersect = rayCaster.intersectObject(plane, false).firstOrNull()
            if (intersect != null) {
                pointer.visible = true

                val planeX = (intersect.point.x - 0.5).roundToInt()
                val planeY = (intersect.point.y - 0.5).roundToInt()
                pointer.position.set(planeX, planeY, 0.0)

                val clicked = event.buttons.toInt() == 1
                val newStep = when {
                    clicked && drawStep >= 0 -> drawStep + 1
                    clicked -> 0
                    drawStep >= 0 -> -1
                    else -> null
                }

                simulationX = planeX + 50
                simulationY = (100 - planeY) - 50 + 1

                if (newStep != null) {
                    if (newStep == 0) {
                        simulationSourceX = simulationX
                        simulationSourceY = simulationY
                    }
                    drawStep = newStep

                    drawOnSimulation(drawStep)
                }

            /* TODO
            toolElement = when {
                selectedTool == EditTools.Pen && selectedGrainController.data != null -> roundDrawElement
                selectedTool == EditTools.Spray && selectedGrainController.data != null -> roundDrawElement
                selectedTool == EditTools.Eraser -> roundDrawElement
                selectedTool == EditTools.Line && drawStep > 0 -> lineDrawElement
                else -> null
            }
             */

                // updates the picking ray with the camera and mouse position
                rayCaster.setFromCamera(Vector2(planeX, planeY), camera)

                render()
            } else {
                if (pointer.visible) {
                    pointer.visible = false
                    render()
                }
            }
        }
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

        simulationCanvas.root.apply {
            onmouseup = { mouseChange(it) }
            onmousedown = { mouseChange(it) }
            onmousemove = { mouseChange(it) }
            onmouseout = {
                toolElement = null
                render()
            }
        }
    }

    private fun geometries() = data.model.grains.map { grain ->
        grain.id to font?.let {
            val height = grain.size
            when (grain.icon) {
                "square" -> BoxBufferGeometry(0.8, 0.8, height)
                "square-full" -> BoxBufferGeometry(1, 1, height)
                "circle" -> CylinderBufferGeometry(0.5, 0.5, height).apply { rotateX(PI/2) }
                else -> TextBufferGeometry(grain.iconString, TextGeometryParametersImpl(it, 0.8, height))
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
                render()
            }.catch { appContext.error(it) }
        }
    }

    fun createMesh(index: Int, grainId: Int, x: Double, y: Double): Mesh {
        // creates mesh
        val material: Material = materials[grainId] ?: defaultMaterial
        val mesh = Mesh(geometries[grainId] ?: defaultGeometry, material.clone())
        mesh.receiveShadows = true
        mesh.castShadow = true

        // positions the mesh
        mesh.position.set(x,  y, 0.0)

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
                createMesh(
                    index,
                    newGrainId,
                    p.x - data.simulation.width / 2.0,
                    - (p.y - data.simulation.height / 2.0 - 1.0)
                )
            }
        }
    }

    fun oneStep(applied: Collection<ApplicableBehavior>, dead: Collection<Int>) {
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
        dead.forEach { transformMesh(it, -1) }

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

        render()
    }

    fun animate() {
    }

    fun render() {
        renderer.render(scene, camera)
    }

    override fun refresh() {
        // clear agents
        agentMesh.values.forEach { scene.remove(it) }
        agentMesh.clear()

        // adds agents meshes
        var currentX = -data.simulation.width / 2.0
        var currentY = data.simulation.height / 2.0 + 1.0
        for (i in 0 until data.currentAgents.size) {
            val grain = data.model.indexedGrains[data.idAtIndex(i)]

            if (grain != null) {
                createMesh(i, grain.id, currentX, currentY)
            }

            currentX += 1.0
            if (currentX >= data.simulation.width / 2.0) {
                currentX = -data.simulation.width / 2.0
                currentY -= 1.0
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
            val mesh = Mesh(geometry, material)
            mesh.rotateX(PI)
            scene.add(mesh)
            field.id to FieldSupport(mesh, alphaTexture, alpha)
        }.toMap()

        render()
    }

    fun dispose() {
        orbitControl.dispose()
        materials = emptyMap()
        defaultMaterial.dispose()
        geometries = emptyMap()
        defaultGeometry.dispose()
    }

}
