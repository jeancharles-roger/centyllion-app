package com.centyllion.client.controller.model

import babylonjs.*
import babylonjs.loaders.GLTFFileLoader
import babylonjs.materials.GridMaterial
import bulma.*
import bulma.Field
import com.centyllion.client.page.BulmaPage
import com.centyllion.model.*
import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.pointerevents.PointerEvent
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.Promise
import kotlin.js.json
import kotlin.math.*
import kotlin.properties.Delegates.observable
import kotlin.random.Random

class Simulator3dViewController(
    simulator: Simulator, val page: BulmaPage, readOnly: Boolean = true,
    initialSelectedGrain: Grain? = null,
    val onPointerMove: (x: Int, y: Int) -> Unit = { _, _ -> },
    var onUpdate: (ended: Boolean, new: Simulator, Simulator3dViewController) -> Unit = { _, _, _ -> }
) : NoContextController<Simulator, BulmaElement>() {

    // simulation content edition
    private var selectedTool: EditTool = EditTool.Pen
    private var selectedSize: ToolSize = ToolSize.Fine

    var selectedGrain: Grain? by observable(initialSelectedGrain) { _, old, new ->
        if (old != new) {
            val color = new?.color?.let { colorFromName(it) }
            changePointer(color)
        }
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
                plane.dispose()
                plane = buildPlane()
                scene.clearColor = colorFromName(data.simulation.settings.backgroundColor ?: "Grey").toColor4(1)

                // clears meshes out of bounds of simulation if simulation settings where changed
                if (old.simulation.dataSize != new.simulation.dataSize) {
                    for (i in old.simulation.agents.indices - new.simulation.agents.indices) {
                        transformMesh(i, -1, true)
                    }
                    resetCamera()
                }
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
        this.width = "$canvasWidth"
        this.height = "${simulator.simulation.height * canvasWidth / simulator.simulation.width}"
    }

    val sizeDropdown = Dropdown(text = page.i18n(ToolSize.Fine.name), rounded = true).apply {
        items = ToolSize.entries.map { size ->
            DropdownSimpleItem(page.i18n(size.name)) {
                this.text = page.i18n(size.name)
                selectedSize = size
                selectPointer()
                this.toggleDropdown()
            }
        }
    }

    val toolButtons = EditTool.entries.map { tool ->
        iconButton(
            Icon(tool.icon), ElementColor.Primary,
            rounded = true, outlined = tool.ordinal == selectedTool.ordinal
        ) { selectTool(tool) }
    }

    val toolsField = Field(addons = true).apply { body = toolButtons.map { Control(it) } }

    val randomAddCountInput: Input = Input("20", rounded = true, columns = 3) { _, v ->
        val isNotPositiveInt = v.toIntOrNull()?.let { it <= 0 } ?: true
        randomAddButton.disabled = isNotPositiveInt
        randomAddButton.color = if (isNotPositiveInt) ElementColor.Danger else ElementColor.Primary
    }

    val randomAddButton = iconButton(Icon("spray-can"), ElementColor.Primary, rounded = true) {
        val grainId = selectedGrain?.id
        val count = randomAddCountInput.value.toIntOrNull()
        if (count != null && count > 0 && grainId != null){
            repeat(count) {
                // try at most 5 times to find a free place
                for (i in 0 until 5) {
                    val index = Random.nextInt(data.simulation.dataSize)
                    if (data.idAtIndex(index) < 0) {
                        data.setIdAtIndex(index, grainId)
                        break
                    }
                }
            }
            onUpdate(true, data, this)
            refresh(false)
        }
    }

    val randomAddField = Field(
        Control(randomAddCountInput),
        Control(randomAddButton),
        addons = true
    )

    val clearAllButton = iconButton(Icon("trash"), ElementColor.Danger, true) {
        (0 until data.simulation.dataSize).forEach { data.resetIdAtIndex(it) }
        onUpdate(true, data, this)
        refresh()
    }

    val toolbar = Level(
        center = listOf(toolsField, sizeDropdown, randomAddField, clearAllButton)
    )

    override val container = Div(
        Div(simulationCanvas, classes = "has-text-centered"), toolbar
    )

    // { preserveDrawingBuffer: true, stencil: true }
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val engineOptions = json(
        "preserveDrawingBuffer" to true,
         "stencil" to true
    ) as EngineOptions

    val engine = Engine(simulationCanvas.root, true, options = engineOptions, adaptToDeviceRatio = false).apply {
        val width = (window.innerWidth - 40).coerceAtMost(600)
        val height = simulator.simulation.height * width / simulator.simulation.width
        setSize(width, height)
    }

    var running = false

    private var animated = false

    val sceneOptions = BasicSceneOptions(
        useGeometryUniqueIdsMap = true,
        useMaterialMeshMap = true,
        useClonedMeshMap = true,
        virtual = null
    )
    
    val scene = Scene(engine, sceneOptions).apply {
        autoClear = true
        clearColor = colorFromName(data.simulation.settings.backgroundColor ?: "Grey").toColor4(1)
        blockfreeActiveMeshesAndRenderingGroups = true
        blockMaterialDirtyMechanism = true

        val width = simulator.simulation.width
        HemisphericLight("light1", Vector3(1.25 * width, -3 * width, 2 * width), this)
    }

    val agentMesh: MutableMap<Int, AbstractMesh> = mutableMapOf()

    val assetsManager = AssetsManager(scene)
    val assetScenes: MutableMap<Asset3d, AbstractMesh> = mutableMapOf()

    private var sourceMeshes by observable(sourceMeshes()) { _, old, _ ->
        old.values.forEach { it.dispose() }
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
    }

    private fun buildPlane() = MeshBuilder.CreatePlane(
        name = "ground",
        options = BasicPlaneOptions(size = data.simulation.settings.size, sideOrientation = Mesh.DOUBLESIDE),
        scene = scene
    ).apply {
        rotate(Axis.X, -PI/2)
        alphaIndex = 0

        this.material = createPlaneMaterial()
    }

    var plane = buildPlane()

    val pointer = MeshBuilder.CreateBox(
        name = "pointer",
        options = BasicBoxOptions(faceColors = Array(6) { Color3.Red().toColor4(0.8) }),
        scene = scene,
    ).apply { isVisible = false }

    val tool get() = selectedTool
    val size get() = selectedSize

    private var drawStep = -1

    private var simulationSourceX = -1
    private var simulationSourceY = -1
    private var sceneSourceX = -1
    private var sceneSourceY = -1

    private var simulationX = -1
    private var simulationY = -1

    fun circle(x: Int, y: Int, block: (i: Int, j: Int) -> Unit) {
        val size = selectedTool.actualSize(selectedSize)
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


    fun square(sourceX: Int, sourceY: Int, x: Int, y: Int, block: (i: Int, j: Int) -> Unit) {
        if (size == ToolSize.Fine) {
            for (i in min(sourceX, x)..max(sourceX, x)) {
                block(i, sourceY)
                block(i, y)
            }

            for (j in min(sourceY, y)..max(sourceY, y)) {
                block(sourceX, j)
                block(x, j)
            }
        } else {
            for (i in min(sourceX, x)..max(sourceX, x)) {
                for (j in min(sourceY, y)..max(sourceY, y)) {
                    block(i, j)
                }
            }
        }
    }


    val height = 6.0

    fun changePointer(color: Color3?) {
        // clear all children
        while (pointer.getChildMeshes().isNotEmpty()) {
            val mesh = pointer.getChildMeshes().first()
            pointer.removeChild(mesh)
            mesh.dispose()
            pointer.getScene().removeMesh(mesh)
        }

        if (selectedTool != EditTool.Move) {
            val diameter = selectedTool.actualSize(selectedSize)
            val color4 = when (selectedTool) {
                EditTool.Eraser -> Color3.Black()
                else -> color ?: Color3.Green()
            }.toColor4(1.0)

            val child = MeshBuilder.CreateCylinder(
                "inner pointer",
                BasicCylinderOptions(height = height, diameter = diameter, faceColors = Array(3) { color4 }),
                scene
            )
            child.visibility = when (selectedTool) {
                EditTool.Eraser -> 0.1
                EditTool.Spray -> 0.4
                else -> 0.7
            }

            child.position.set(pointer.position.x, pointer.position.y.toDouble()-height/2.0, pointer.position.z)
            pointer.addChild(child)
        }
    }

    fun pointerVisibility(visible: Boolean) {
        //pointer.isVisible = visible
        pointer.getChildMeshes().forEach { it.isVisible = visible }
    }

    fun positionPointer(x: Number, y: Number) {
        pointer.position.set(x.toDouble()+0.5, 0.0, y.toDouble()+0.5)
    }

    fun updatePointer() {
        if (selectedTool == EditTool.Line || selectedTool == EditTool.Square) {
            if (drawStep == 0) {
                val child = pointer.getChildren({ true }, true).firstOrNull()
                if (child is Mesh) {
                    val mesh = child.clone("draw source", pointer)
                    if (mesh is Mesh) {
                        mesh.freezeWorldMatrix()
                        pointer.addChild(mesh)
                    }
                }
            }
            if (drawStep < 0) {
                val child = pointer.getChildren({ it.name == "draw source" }, true).firstOrNull()
                if (child is Mesh) {
                    pointer.removeChild(child)
                    child.dispose()
                }
            }
        }
    }

    fun selectPointer() {
        val color = colorFromName(selectedGrain?.color ?: "Gray")
        changePointer(color)
    }

    fun selectTool(tool: EditTool) {
        toolButtons.forEach { it.outlined = false }
        toolButtons[tool.ordinal].outlined = true
        selectedTool = tool

        //planeBehaviour.enabled = tool != EditTools.Move
        if (tool == EditTool.Move) {
            camera.attachControl(simulationCanvas.root, false)
        } else {
            camera.detachControl(simulationCanvas.root)
        }
        selectPointer()

        refresh(true)
    }

    fun writeGrain(i: Int, j: Int, id: Int) = data.simulation.toIndex(i, j).let { index ->
        if (data.idAtIndex(index) < 0) data.setIdAtIndex(index, id)
    }

    fun drawOnSimulation() {
        when (selectedTool) {
            EditTool.Pen -> {
                if (drawStep >= 0) {
                    selectedGrain?.id?.let { idToSet ->
                        circle(simulationX, simulationY) { i, j -> writeGrain(i, j, idToSet) }
                    }
                }
            }
            EditTool.Line -> {
                selectedGrain?.id?.let { idToSet ->
                    if (drawStep == -1) {
                        // draw the line
                        line(simulationSourceX, simulationSourceY, simulationX, simulationY) { i, j ->
                            writeGrain(i, j, idToSet)
                        }
                    }
                }
            }
            EditTool.Square -> {
                selectedGrain?.id?.let { idToSet ->
                    if (drawStep == -1) {
                        // draw the line
                        square(simulationSourceX, simulationSourceY, simulationX, simulationY) { i, j ->
                            writeGrain(i, j, idToSet)
                        }
                    }
                }
            }
            EditTool.Spray -> {
                selectedGrain?.id?.let { idToSet ->
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
            EditTool.Eraser -> {
                circle(simulationX, simulationY) { i, j ->
                    data.resetIdAtIndex(data.simulation.toIndex(i, j))
                }
            }
            EditTool.Move -> {
            }
        }

        onUpdate(drawStep == -1, data, this)
        refresh(false)
    }

    private fun toSimulation(value: Number?) = (
            (value?.toDouble()?: 0.0) - (data.simulation.settings.size/200.0)
    ).roundToInt()

    @Suppress("UNUSED_PARAMETER")
    private fun onPointerDown(evt: PointerEvent, pickInfo: PickingInfo, type: PointerEventTypes) {
        val ray = pickInfo.ray
        if (ray != null && selectedTool != EditTool.Move) {
            val info = ray.intersectsMesh(plane, true)
            pointerVisibility(info.hit)
            if (info.hit && info.pickedMesh == plane) {
                drawStep = 0

                val x = toSimulation(info.pickedPoint?.x)
                val y = toSimulation(info.pickedPoint?.z)

                simulationX = x + data.simulation.width / 2
                simulationY = y + data.simulation.height / 2
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
        if (ray != null) {
            val info = ray.intersectsMesh(plane, true)
            pointerVisibility(info.hit)

            val x = toSimulation(info.pickedPoint?.x)
            val y = toSimulation(info.pickedPoint?.z)
            positionPointer(x, y)

            if (info.hit && info.pickedMesh == plane) {
                if (selectedTool != EditTool.Move && drawStep > 0) {
                    simulationX = x + data.simulation.width / 2
                    simulationY = y + data.simulation.height / 2
                    updatePointer()
                    drawOnSimulation()
                    drawStep += 1
                }
                onPointerMove(x + data.simulation.width / 2 , y + data.simulation.height / 2)
            } else {
                onPointerMove(-1, -1)
                drawStep = -1
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPointerUp(evt: PointerEvent, pickInfo: PickingInfo?, type: PointerEventTypes) {
        val ray = pickInfo?.ray
        if (ray != null && selectedTool != EditTool.Move) {
            val info = ray.intersectsMesh(plane, true)
            pointerVisibility(info.hit)
            if (info.hit && info.pickedMesh != null) {
                val x = toSimulation(info.pickedPoint?.x)
                val y = toSimulation(info.pickedPoint?.z)
                simulationX = x + data.simulation.width / 2
                simulationY = y + data.simulation.height / 2
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

    private val resizeCallback: (Event) -> Unit = { resize() }

    init {
        // Force loading of GLTF loader plugin
        GLTFFileLoader::class.simpleName

        refreshAssets()

        scene.onPointerMove = this::onPointerMove
        scene.onPointerDown = this::onPointerDown
        scene.onPointerUp = this::onPointerUp

        simulationCanvas.root.onmouseenter = {
            animated = true
            drawStep = -1
            asDynamic()
        }
        simulationCanvas.root.onmouseleave = {
            animated = false
            drawStep = -1
            asDynamic()
        }

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

        window.addEventListener("resize", resizeCallback)

        container.root.onmouseleave = {
            drawStep = -1
            pointerVisibility(false)
        }
        window.onmouseleave = {
            drawStep = -1
            pointerVisibility(false)
        }
    }

    fun thumbnail() = screenshot(400, 267)

    fun screenshot(width: Int, height: Int) = screenshotURL(width, height).then {
        val buffer = Tools.DecodeBase64(it)
        Blob(arrayOf(buffer), object: BlobPropertyBag { override var type: String? = "image/webp" })
    }

    fun screenshotURL(width: Int = 1200, height: Int = 800) = Promise { resolve, _ ->
        animated = true
        Tools.CreateScreenshotUsingRenderTarget(
            engine, camera, json("width" to width, "height" to height), { resolve(it) },
            "image/webp", null, true, null
        )
        window.setTimeout( { animated = false }, 1000)
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
        camera.target = Vector3(0, 0, 0)
        window.setTimeout( { animated = false }, 1000)
    }

    fun colorFromName(name: String) =
        colorNames[name]?.let {Color3.FromInts(it.first, it.second, it.third) } ?: Color3.Green()

    private fun sourceMeshes(): Map<Int, Mesh> = data.model.grains.associate { grain ->
        val height = grain.size.coerceAtLeast(0.1)
        val color = colorFromName(grain.color)
        val color4 = color.toColor4(1)
        val mesh = when (grain.icon) {
            "square" -> MeshBuilder.CreateBox(
                grain.name,
                BasicBoxOptions(width = 0.8, depth = 0.8, height = height, faceColors = Array(6) { color4 }),
                scene
            )
            "square-full" -> MeshBuilder.CreateBox(
                grain.name,
                BasicBoxOptions(width = 1, depth = 1, height = height, faceColors = Array(6) { color4 }),
                scene
            )
            "circle" -> MeshBuilder.CreateCylinder(
                grain.name,
                BasicCylinderOptions(height = height, diameter = 1.0, faceColors = Array(3) { color4 }),
                scene
            )
            else -> MeshBuilder.CreateBox(
                grain.name,
                BasicBoxOptions(width = 1, depth = 1, height = height, faceColors = Array(6) { color4 }),
                scene
            )
        }.apply {
            material = StandardMaterial("material ${grain.name}", scene).apply {
                diffuseColor = color
                wireframe = grain.invisible
            }
            setEnabled(false)
            convertToUnIndexedMesh()
            cullingStrategy = AbstractMesh.CULLINGSTRATEGY_BOUNDINGSPHERE_ONLY
        }
        grain.id to mesh
    }

    private fun fieldSupports() = data.model.fields.associate { field ->
        if (!field.invisible) {
            val levels = data.field(field.id)

            val alpha = Uint8Array(levels.size)
            levels.alpha(alpha)
            val texture = RawTexture.CreateAlphaTexture(
                data = alpha, width = data.simulation.width, height = data.simulation.height,
                scene = scene, generateMipMaps = false,
                invertY = false, samplingMode = Texture.NEAREST_SAMPLINGMODE
            )
            val material = StandardMaterial("${field.name} material", scene)
            val color = colorFromName(field.color)
            material.diffuseColor = color
            material.opacityTexture = texture

            val mesh = MeshBuilder.CreatePlane(
                name = "${field.name} mesh",
                options = BasicPlaneOptions(size = data.simulation.settings.size, sideOrientation = Mesh.DOUBLESIDE),
                scene = scene
            )
            mesh.material = material
            mesh.rotate(Axis.X, PI / 2)
            mesh.alphaIndex = field.id + 1

            scene.addMesh(mesh)
            field.id to FieldSupport(mesh, texture, alpha)
        } else {
            field.id to null
        }
    }


    private fun refreshAssets() {
        // clears previous assets
        assetScenes.values.forEach { scene.removeMesh(it, true) }
        assetScenes.clear()

        // sets new assets
        data.simulation.assets.map { asset ->
            val task = assetsManager.addMeshTask("Loading ${asset.url}", "", page.appContext.api.translateUrl(asset.url), "")
            task.runTask(scene,
                {
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
                    animated = true
                    render()
                    scene.executeWhenReady { animated = false }
                },
                {m, _ ->
                    page.error("Error loading asset: $m")
                }
            )
        }
    }

    fun createMesh(index: Int, grainId: Int, x: Double, y: Double) {
        val agent = data.grainForId(grainId)
        if (selectedTool != EditTool.Move || (agent?.invisible != true) ) {
            sourceMeshes[grainId]?.createInstance("$index")?.also {
                it.metadata = grainId

                // positions the mesh
                it.position.set(x + 0.5, -(agent?.size ?: 1.0) / 2.0, y + 0.5)

                it.freezeWorldMatrix()
                it.ignoreNonUniformScaling = true
                it.doNotSyncBoundingInfo = true
                it.cullingStrategy = AbstractMesh.CULLINGSTRATEGY_BOUNDINGSPHERE_ONLY

                //mesh.receiveShadows = true

                // adds the mesh to scene and register it
                scene.addMesh(it)
                agentMesh[index] = it
            }
        }
    }

    fun transformMesh(index: Int, newGrainId: Int, force: Boolean = false) {
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
            val reactives = one.usedNeighbours.sortedBy { it.reactiveId }
            val reactions = one.behaviour.reaction.sortedBy { it.reactiveId }
            reactions.zip(reactives).forEach { (reaction, reactive) ->
                transformMesh(reactive.index, reaction.productId)
            }
        }

        // applies deaths
        dead.forEach { transformMesh(it, -1) }

        // updates field
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
        window.removeEventListener("resize", resizeCallback)
    }

    fun resizeToFullscreen() {
        resizeSimulationCanvas(window.innerWidth, window.innerHeight - 120)
    }

    fun resize() {
        val canvas = simulationCanvas.root
        val availableWidth = (canvas.parentNode as HTMLElement?)?.offsetWidth ?: 600
        val availableHeight = ((0.80) * window.innerHeight).toInt() - 100
        resizeSimulationCanvas(availableWidth, availableHeight)
    }

    private fun resizeSimulationCanvas(availableWidth: Int, availableHeight: Int) {
        // resize only if the canvas is actually shown
        val canvas = simulationCanvas.root
        canvas.width = availableWidth
        canvas.height = availableHeight
        engine.resize()
        render()
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
            array[i] = a.toInt().toByte()
        }
    }
}
