package com.centyllion.client.controller.model

import babylonjs.AbstractMesh
import babylonjs.Animation
import babylonjs.ArcRotateCamera
import babylonjs.AssetsManager
import babylonjs.Axis
import babylonjs.BoxOptions
import babylonjs.Color3
import babylonjs.CylinderOptions
import babylonjs.Engine
import babylonjs.HemisphericLight
import babylonjs.Mesh
import babylonjs.MeshBuilder
import babylonjs.PickingInfo
import babylonjs.PlaneOptions
import babylonjs.PointerEventTypes
import babylonjs.Quaternion
import babylonjs.RawTexture
import babylonjs.Scene
import babylonjs.SceneOptions
import babylonjs.StandardMaterial
import babylonjs.Texture
import babylonjs.Tools
import babylonjs.Vector3
import babylonjs.loaders.GLTFFileLoader
import babylonjs.materials.GridMaterial
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
import com.centyllion.client.download
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Asset3d
import com.centyllion.model.Simulator
import com.centyllion.model.colorNames
import com.centyllion.model.minFieldLevel
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.pointerevents.PointerEvent
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.properties.Delegates.observable
import kotlin.random.Random

class Simulator3dViewController(
    simulator: Simulator, val page: BulmaPage, readOnly: Boolean = true,
    var onUpdate: (ended: Boolean, new: Simulator, Simulator3dViewController) -> Unit = { _, _, _ -> }
) : NoContextController<Simulator, BulmaElement>() {

    enum class EditTools(val icon: String, val factor: Int = 1) {
        Move("arrows-alt"), Pen("pen"),
        Line("pencil-ruler"), Spray("spray-can", 4),
        Eraser("eraser");
    }

    @Suppress("unused")
    enum class ToolSize(val size: Int) {
        Fine(1), Small(5), Medium(10), Large(20)
    }

    class FieldSupport(val mesh: Mesh, val texture: RawTexture, val alpha: Uint8Array) {
        fun dispose() {
            texture.dispose()
            mesh.dispose()
        }
    }

    override var data: Simulator by observable(simulator) { _, old, new ->
        if (old != new) {
            onUpdate(true, new, this)

            selectedGrainController.context = new.model.grains
            if (!new.model.grains.contains(selectedGrainController.data)) {
                selectedGrainController.data = new.model.grains.firstOrNull()
            }

            // only refresh geometries and material if grains changed
            if (old.model.grains != new.model.grains) {
                sourceMeshes = sourceMeshes()
            }

            // only refresh assets if they changed
            if (old.simulation.assets != new.simulation.assets) {
                refreshAssets()
            }

            if (old.fields != new.fields) {
                fieldSupports = fieldSupports()
            }

            if (old.simulation.settings != new.simulation.settings) {
                plane.material?.dispose()
                plane.material = createPlaneMaterial()
                scene.clearColor = colorFromName(data.simulation.settings.backgroundColor ?: "Grey").toColor4(1)
            }

            refresh()
        }
    }

    override var readOnly: Boolean by observable(readOnly) { _, old, new ->
        if (old != new) {
            toolbar.hidden = new
        }
    }

    val simulationCanvas: HtmlWrapper<HTMLCanvasElement> = canvas("cent-simulation") {
        val canvasWidth = (window.innerWidth - 40).coerceAtMost(600)
        width = "$canvasWidth"
        height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    val selectedGrainController = GrainSelectController(simulator.model.grains.firstOrNull(), simulator.model.grains)
    { _, _, _ -> selectPointer() }

    val sizeDropdown = Dropdown(text = page.i18n(ToolSize.Fine.name), rounded = true).apply {
        items = ToolSize.values().map { size ->
            DropdownSimpleItem(page.i18n(size.name)) {
                this.text = page.i18n(size.name)
                selectedSize = size
                selectPointer()
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

    val toolsField = Field(addons = true).apply { body = toolButtons.map { Control(it) } }

    val clearAllButton = iconButton(Icon("trash"), ElementColor.Danger, true) {
        (0 until data.simulation.dataSize).forEach { data.resetIdAtIndex(it) }
        onUpdate(true, data, this)
        refresh()
    }

    val imageButton = iconButton(Icon("file-image"), ElementColor.Info, true) {
        screenshot().then { download("screenshot.jpg", URL.createObjectURL(it)) }
    }

    val toolbar = Level(center = listOf(toolsField, sizeDropdown, selectedGrainController, clearAllButton, imageButton))

    override val container = Div(
        Div(simulationCanvas, classes = "has-text-centered"), toolbar
    )

    val engine = Engine(simulationCanvas.root, true, adaptToDeviceRatio = false).apply {
        val width = (window.innerWidth - 40).coerceAtMost(600)
        val height = simulator.simulation.height * width / simulator.simulation.width
        setSize(width, height)
    }

    var running = false

    private var animated = false

    val sceneOptions = SceneOptions(true, true, true)
    val scene = Scene(engine, sceneOptions).apply {
        autoClear = true
        clearColor = colorFromName(data.simulation.settings.backgroundColor ?: "Grey").toColor4(1)
        autoClearDepthAndStencil = false
        blockfreeActiveMeshesAndRenderingGroups = true
        blockMaterialDirtyMechanism = true

        val width = simulator.simulation.width
        HemisphericLight("light1", Vector3(1.25 * width, -3 * width, 2 * width), this)
    }

    val agentMesh: MutableMap<Int, AbstractMesh> = mutableMapOf()

    val assetsManager = AssetsManager(scene)
    val assetScenes: MutableMap<Asset3d, AbstractMesh> = mutableMapOf()

    private var sourceMeshes by observable(sourceMeshes()) { _, old, _ ->
        old.values.forEach { it?.dispose() }
    }

    private var fieldSupports: Map<Int, FieldSupport?> by observable(mapOf()) { _, old, new ->
        old.values.filterNotNull().forEach {
            scene.removeMesh(it.mesh)
            it.dispose()
        }
        new.values.filterNotNull().forEach {
            scene.addMesh(it.mesh)
        }
    }

    val camera = ArcRotateCamera(
        "Camera", 0, 0, 1.25 * simulator.simulation.width, Vector3(0, 0, 0), scene
    ).apply {
        lowerAlphaLimit = null
        upperAlphaLimit = null
        alpha = 1.5*PI
        lowerBetaLimit = 0.0
        upperBetaLimit = 2 * PI
        beta = PI

        lowerRadiusLimit = 5
        upperRadiusLimit = 500

        angularSensibilityX = - angularSensibilityX.toDouble()
        panningSensibility = 50

        attachControl(simulationCanvas.root, false)
    }

    val plane = MeshBuilder.CreatePlane("ground", PlaneOptions(size = 100, sideOrientation = Mesh.DOUBLESIDE), scene).apply {
        rotate(Axis.X, -PI/2)
        alphaIndex = 0

        this.material = createPlaneMaterial()
    }

    val pointer = MeshBuilder.CreateBox(
        "pointer", BoxOptions(faceColors = Array(6) { Color3.Red().toColor4(0.8) }), scene
    ).apply {
        isVisible = false
    }

    // simulation content edition
    private var selectedTool: EditTools = EditTools.Move
    private var selectedSize: ToolSize = ToolSize.Fine

    var drawStep = -1

    var simulationSourceX = -1
    var simulationSourceY = -1
    var sceneSourceX = -1
    var sceneSourceY = -1

    var simulationX = -1
    var simulationY = -1

    fun circle(x: Int, y: Int, block: (i: Int, j: Int) -> Unit) {
        val size = selectedSize.size * selectedTool.factor
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


    val height = 6.0

    fun changePointer(color: Color3?, size: Int) {
        // clear all children
        while (pointer.getChildMeshes().isNotEmpty()) {
            val mesh = pointer.getChildMeshes().first()
            pointer.removeChild(mesh)
            mesh.dispose()
            pointer.getScene().removeMesh(mesh)
        }

        if (selectedTool != EditTools.Move) {
            val diameter = size * selectedTool.factor

            val color4 = when (selectedTool) {
                EditTools.Eraser -> Color3.Black()
                else -> color ?: Color3.Green()
            }.toColor4(1.0)

            val child = MeshBuilder.CreateCylinder(
                "inner pointer",
                CylinderOptions(height = height, diameter = diameter, faceColors = Array(3) { color4 }),
                scene
            )
            child.visibility = when (selectedTool) {
                EditTools.Eraser -> 0.1
                EditTools.Spray -> 0.4
                else -> 0.7
            }

            child.position.set(pointer.position.x, pointer.position.y.toDouble()-height/2.0, pointer.position.z)
            pointer.addChild(child)
        }
    }

    fun pointerVisibility(visible: Boolean) {
        pointer.getChildMeshes().forEach { it.isVisible = visible }
    }

    fun positionPointer(x: Number, y: Number) {
        pointer.position.set(x.toDouble()+0.5, 0.0, y.toDouble()+0.5)
    }

    fun updatePointer() {
        if (selectedTool == EditTools.Line) {
            if (drawStep == 0) {
                val child = pointer.getChildren({ true }, true).firstOrNull()
                if (child is Mesh) {
                    val mesh = child.clone("line source", pointer)
                    if (mesh is Mesh) {
                        mesh.freezeWorldMatrix()
                        pointer.addChild(mesh)
                    }
                }
            }
            if (drawStep < 0) {
                val child = pointer.getChildren({ it.name == "line source" }, true).firstOrNull()
                if (child is Mesh) {
                    pointer.removeChild(child)
                    child.dispose()
                }
            }
        }
    }

    fun selectPointer() {
        val color = colorFromName(selectedGrainController.data?.color ?: "Green")
        changePointer(color, selectedSize.size)
    }

    fun selectTool(tool: EditTools) {
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool

        //planeBehaviour.enabled = tool != EditTools.Move
        if (tool == EditTools.Move) {
            camera.attachControl(simulationCanvas.root, false)
        } else {
            camera.detachControl(simulationCanvas.root)
        }
        selectPointer()
    }

    fun writeGrain(i: Int, j: Int, id: Int) = data.simulation.toIndex(i, j).let { index ->
        if (data.idAtIndex(index) < 0) data.setIdAtIndex(index, id)
    }

    fun drawOnSimulation() {
        when (selectedTool) {
            EditTools.Pen -> {
                if (drawStep >= 0) {
                    selectedGrainController.data?.id?.let { idToSet ->
                        circle(simulationX, simulationY) { i, j -> writeGrain(i, j, idToSet) }
                    }
                }
            }
            EditTools.Line -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    if (drawStep == -1) {
                        // draw the line
                        line(simulationSourceX, simulationSourceY, simulationX, simulationY) { i, j ->
                            writeGrain(i, j, idToSet)
                        }
                    }
                }

            }
            EditTools.Spray -> {
                selectedGrainController.data?.id?.let { idToSet ->
                    if (drawStep >= 0) {
                        val sprayDensity = 0.005
                        circle(simulationX, simulationY) { i, j ->
                            if (Random.nextDouble() < sprayDensity) {
                                writeGrain(i, j, idToSet)
                            }
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

        onUpdate(drawStep == -1, data, this)
        refresh(false)
    }

    private fun toSimulation(value: Number?) = ((value?.toDouble()?: 0.0) - 0.5).roundToInt()

    @Suppress("UNUSED_PARAMETER")
    private fun onPointerDown(evt: PointerEvent, pickInfo: PickingInfo, type: PointerEventTypes) {
        val ray = pickInfo.ray
        if (ray != null && selectedTool != EditTools.Move) {
            val info = ray.intersectsMesh(plane, true)
            pointerVisibility(info.hit)
            if (info.hit && info.pickedMesh == plane) {
                drawStep = 0

                val x = toSimulation(info.pickedPoint?.x)
                val y = toSimulation(info.pickedPoint?.z)

                simulationX = x + 50
                simulationY = y + 50
                sceneSourceX = x
                sceneSourceY = y
                simulationSourceX = simulationX
                simulationSourceY = simulationY
                updatePointer()
                drawOnSimulation()
                drawStep += 1
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPointerMove(evt: PointerEvent, pickInfo: PickingInfo, type: PointerEventTypes) {
        val ray = pickInfo.ray
        if (ray != null && selectedTool != EditTools.Move) {
            val info = ray.intersectsMesh(plane, true)
            pointerVisibility(info.hit)
            if (info.hit && info.pickedMesh == plane) {
                val x = toSimulation(info.pickedPoint?.x)
                val y = toSimulation(info.pickedPoint?.z)

                positionPointer(x, y)
                if (drawStep > 0) {
                    simulationX = x + 50
                    simulationY = y + 50
                    updatePointer()
                    drawOnSimulation()
                    drawStep += 1
                }
            }

        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPointerUp(evt: PointerEvent, pickInfo: PickingInfo?, type: PointerEventTypes) {
        val ray = pickInfo?.ray
        if (ray != null && selectedTool != EditTools.Move) {
            val info = ray.intersectsMesh(plane, true)
            pointerVisibility(info.hit)
            if (info.hit && info.pickedMesh != null) {
                val x = toSimulation(info.pickedPoint?.x)
                val y = toSimulation(info.pickedPoint?.z)
                simulationX = x + 50
                simulationY = y + 50
                drawStep = -1
                updatePointer()
                drawOnSimulation()
                simulationSourceX = -1
                simulationSourceY = -1
                simulationX = -1
                simulationY = -1
            }
        }
    }

    init {
        console.log(GLTFFileLoader::class.simpleName)

        refreshAssets()

        scene.onPointerMove = this::onPointerMove
        scene.onPointerDown = this::onPointerDown
        scene.onPointerUp = this::onPointerUp

        simulationCanvas.root.onmouseenter = { animated = true; Unit }
        simulationCanvas.root.onmouseleave = { animated = false; Unit }

        engine.runRenderLoop {
             if (
                animated || running ||
                abs(camera.inertialAlphaOffset.toDouble()) > 0 ||
                abs(camera.inertialBetaOffset.toDouble()) > 0 ||
                abs(camera.inertialRadiusOffset.toDouble()) > 0
            ) {
                scene.render()
            }
        }

        window.onresize = {
            resizeSimulationCanvas()
            Unit
        }
    }

    fun screenshot() = Promise<Blob> { resolve, reject ->
        render()
        Tools.ToBlob(simulationCanvas.root, { if (it != null) resolve(it) else reject(Exception("No content")) })
    }

    fun resetCamera() {
        animated = true
        Animation.CreateAndStartAnimation(
            "Reset camera radius", camera, "radius", 25, 25,
            camera.radius, 1.25 * data.simulation.width, 0
        )
        Animation.CreateAndStartAnimation(
            "Reset camera alpha", camera, "alpha", 25, 25,
            camera.alpha, 1.5 * PI, 0
        )
        Animation.CreateAndStartAnimation(
            "Reset camera beta", camera, "beta", 25, 25,
            camera.beta, PI, 0
        )
        Animation.CreateAndStartAnimation(
            "Reset camera target", camera, "target", 26, 26,
            camera.target, Vector3(0, 0, 0), 0, onAnimationEnd = { animated = false }
        )
    }

    fun colorFromName(name: String) =
        colorNames[name]?.let {Color3.FromInts(it.first, it.second, it.third) } ?: Color3.Green()

    private fun sourceMeshes() = data.model.grains.map { grain ->
        if (!grain.invisible) {
            val height = grain.size.coerceAtLeast(0.1)
            val color = colorFromName(grain.color).toColor4(1)
            val mesh = when (grain.icon) {
                "square" -> MeshBuilder.CreateBox(
                    grain.name,
                    BoxOptions(width = 0.8, depth = 0.8, height = height, faceColors = Array(6) { color }),
                    scene
                )
                "square-full" -> MeshBuilder.CreateBox(
                    grain.name,
                    BoxOptions(width = 1, depth = 1, height = height, faceColors = Array(6) { color }),
                    scene
                )
                "circle" -> MeshBuilder.CreateCylinder(
                    grain.name,
                    CylinderOptions(height = height, diameter = 1.0, faceColors = Array(3) { color }),
                    scene
                )
                else -> MeshBuilder.CreateBox(
                    grain.name,
                    BoxOptions(width = 1, depth = 1, height = height, faceColors = Array(6) { color }),
                    scene
                )
            }.apply {
                setEnabled(false)
                convertToUnIndexedMesh()
                cullingStrategy = AbstractMesh.CULLINGSTRATEGY_BOUNDINGSPHERE_ONLY
            }
            grain.id to mesh
        } else {
            grain.id to null
        }
    }.toMap()

    private fun fieldSupports() = data.model.fields.map { field ->
        if (!field.invisible) {
            val levels = data.field(field.id)

            val alpha = Uint8Array(levels.size)
            levels.alpha(alpha)
            val texture = RawTexture.CreateAlphaTexture(
                alpha, data.simulation.width, data.simulation.height,
                scene, false, false, Texture.NEAREST_SAMPLINGMODE
            )
            val material = StandardMaterial("${field.name} material", scene)
            val color = colorFromName(field.color)
            material.diffuseColor = color
            material.opacityTexture = texture

            val mesh = MeshBuilder.CreatePlane(
                "${field.name} mesh",
                PlaneOptions(size = 100, sideOrientation = Mesh.DOUBLESIDE),
                scene
            )
            mesh.material = material
            mesh.rotate(Axis.X, PI / 2)
            mesh.alphaIndex = field.id + 1

            scene.addMesh(mesh)
            field.id to FieldSupport(mesh, texture, alpha)
        } else {
            field.id to null
        }
    }.toMap()


    private fun refreshAssets() {
        // clears previous assets
        assetScenes.values.forEach { scene.removeMesh(it, true) }
        assetScenes.clear()

        // sets new assets
        data.simulation.assets.map { asset ->
            console.log("Loading asset ${asset.url}")
            val task = assetsManager.addMeshTask("Loading ${asset.url}", "", asset.url, "")
            task.runTask(scene,
                {
                    console.log("Success loading of asset ${asset.url}")
                    val mesh = task.loadedMeshes[0] as Mesh
                    mesh.position.set(asset.x, asset.y, asset.z)
                    mesh.scaling.set(asset.xScale, asset.yScale, asset.zScale)
                    if (asset.xRotation != 0.0 || asset.yRotation != 0.0 || asset.zRotation != 0.0) {
                        mesh.rotationQuaternion = Quaternion.FromEulerAngles(asset.xRotation, asset.yRotation, asset.zRotation)
                    }
                    if (asset.opacity < 1.0) {
                        mesh.visibility = asset.opacity
                        mesh.getChildMeshes().forEach { it.visibility = asset.opacity }
                    }
                    scene.addMesh(mesh)
                    assetScenes[asset] = mesh
                    render()
                },
                {m, _ ->
                    page.error("Error loading asset: $m")
                }
            )
        }
    }

    fun createMesh(index: Int, grainId: Int, x: Double, y: Double)=
        sourceMeshes[grainId]?.createInstance("$index")?.also {
            it.metadata = grainId

            // positions the mesh
            val agent = data.model.indexedGrains[grainId]
            it.position.set(x+0.5,  -(agent?.size ?: 1.0) / 2.0, y+0.5)

            it.freezeWorldMatrix()
            it.ignoreNonUniformScaling = true
            it.doNotSyncBoundingInfo = true
            it.cullingStrategy = AbstractMesh.CULLINGSTRATEGY_BOUNDINGSPHERE_ONLY

            //mesh.receiveShadows = true

            // adds the mesh to scene and register it
            scene.addMesh(it)
            agentMesh[index] = it
        }

    fun  transformMesh(index: Int, newGrainId: Int, force: Boolean = false) {
        val mesh = agentMesh[index]
        if (force || newGrainId != mesh?.metadata) {
            // deletes the mesh
            if (mesh != null) {
                agentMesh.remove(index)
                scene.removeMesh(mesh, true)
                mesh.dispose()
            }
            if (newGrainId >= 0) {
                val p = data.simulation.toPosition(index)
                createMesh(
                    index,
                    newGrainId,
                    p.x - data.simulation.width / 2.0,
                    p.y - data.simulation.height / 2.0
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
            it.value?.let { support ->
                data.field(it.key).alpha(support.alpha)
                support.texture.update(support.alpha)
            }
        }

        simulationCanvas.root.classList.add("is-danger")
    }

    fun render() {
        scene.render()
    }

    override fun refresh() = refresh(true)

    fun refresh(force: Boolean) {
        // applies transform mesh to all simulation
        for (i in data.currentAgents.indices) {
            transformMesh(i, data.idAtIndex(i), force)
        }

        fieldSupports.forEach {
            it.value?.let { support ->
                data.field(it.key).alpha(support.alpha)
                support.texture.update(support.alpha)
            }
        }

        simulationCanvas.root.classList.toggle("is-danger", data.step > 0)

        render()
    }

    fun dispose() {
        sourceMeshes = emptyMap()
        fieldSupports = emptyMap()
        camera.detachControl(simulationCanvas.root)
        camera.dispose()
        engine.dispose()
        assetsManager.reset()
        pointer.dispose()
    }

    private fun resizeSimulationCanvas() {
        // resize only if the canvas is actually shown
        if (simulationCanvas.root.offsetParent != null) {
            val canvas = simulationCanvas.root
            val availableWidth = (canvas.parentNode as HTMLElement?)?.offsetWidth ?: 600
            val availableHeight = window.innerHeight
            val ratio = data.simulation.height.toDouble() / data.simulation.width.toDouble()
            if (availableWidth * ratio > availableHeight) {
                // height is the limiting factor
                canvas.width = (availableHeight / ratio).roundToInt()
                canvas.height = availableHeight
            } else {
                // width is the limiting factor
                canvas.width = availableWidth
                canvas.height = (availableWidth * ratio).roundToInt()
            }
            engine.resize()
            render()
        }
    }

    private fun createPlaneMaterial() = data.simulation.settings.gridTextureUrl?.let {
        StandardMaterial("ground material", scene).apply {
            Texture(it, scene, invertY = true, buffer = null as ArrayBuffer?).let {
                diffuseTexture = it
                opacityTexture = it
            }
        }
    } ?: GridMaterial("ground material", scene).apply {
        opacity = if (data.simulation.settings.showGrid) 0.8 else 0.0
        mainColor = Color3.White()
        lineColor = Color3.White()
    }

    /** Computes alpha value for opacity for each field level in the array */
    fun FloatArray.alpha(array: Uint8Array)  {
        for (i in this.indices) {
            val l = this[i]
            val a = when {
                l <= minFieldLevel -> 0f
                l >= 1f -> 200f
                l >= 0.1f -> 111 * l + 89f
                else -> (-100f / log10(l))
            }
            array[i] = a.toByte()
        }
    }
}
