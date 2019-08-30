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
import babylonjs.InstancedMesh
import babylonjs.Mesh
import babylonjs.MeshBuilder
import babylonjs.PickingInfo
import babylonjs.PlaneOptions
import babylonjs.PointLight
import babylonjs.PointerEventTypes
import babylonjs.Quaternion
import babylonjs.RawTexture
import babylonjs.Scene
import babylonjs.SceneOptions
import babylonjs.StandardMaterial
import babylonjs.Texture
import babylonjs.Tools
import babylonjs.Vector3
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
import com.centyllion.client.toFixed
import com.centyllion.model.ApplicableBehavior
import com.centyllion.model.Asset3d
import com.centyllion.model.Simulator
import com.centyllion.model.colorNames
import com.centyllion.model.minFieldLevel
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.pointerevents.PointerEvent
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.browser.window
import kotlin.js.Promise
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.round
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

        val height = 6.0

        fun changePointer(pointer: Mesh, color: Color3?, size: Int) {
            /*
            // clear all children
            while (pointer.children.isNotEmpty()) {
                pointer.remove(pointer.children.first())
            }
            if (this != Move) {
                val radius = 0.5 * size * factor
                val geometry = CylinderBufferGeometry(radius, radius, height, 16)
                geometry.translate(0.5, height / 2.0, 0.5)
                val material = sourceMaterial?.clone() ?: MeshPhongMaterial().apply { color.set("#ff0000") }
                val mesh = Mesh(geometry, material).apply {
                    renderOrder = 5
                    material.opacity = when (this@EditTools) {
                        Eraser -> 0.0
                        Spray -> 0.4
                        else -> 0.7
                    }
                    material.transparent = true
                }

                val wireMaterial = LineBasicMaterial().apply {
                    color.set("grey")
                    transparent = true
                    opacity = 0.4
                }
                mesh.add(LineSegments(WireframeGeometry(geometry), wireMaterial))
                pointer.add(mesh)
            }
            */
        }

        fun positionPointer(pointer: Mesh, x: Number, y: Number) {
            pointer.position.set(x, 0.0, y)
            //pointer.children.firstOrNull()?.position?.set(x, 0.0, y)
        }

        fun updatePointer(pointer: Mesh, step: Int) {
            /*
            val mesh = pointer.children.firstOrNull()
            if (mesh != null && this == Line) {
                if (step == 0) {
                    val newMesh = mesh.clone()
                    pointer.add(newMesh)
                }
                if (step < 0) {
                    pointer.remove(pointer.children.last())
                }
            }
             */
        }
    }

    enum class ToolSize(val size: Int) {
        Fine(1), Small(5), Medium(10), Large(20)
    }

    class FieldSupport(val mesh: Mesh, val texture: RawTexture) {
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

    val engine = Engine(simulationCanvas.root, true).apply {
        val width = (window.innerWidth - 40).coerceAtMost(600)
        val height = simulator.simulation.height * width / simulator.simulation.width
        setSize(width, height)
    }

    var running = false

    private var animated = false

    val sceneOptions = SceneOptions(true, true, true)
    val scene = Scene(engine, sceneOptions).apply {
        autoClear = false
        autoClearDepthAndStencil = false
        blockfreeActiveMeshesAndRenderingGroups = true

        val position = simulator.simulation.width.let { Vector3(1.25 * it, -2 * it, 1.25 * it) }
        HemisphericLight("light1", position, this)
        PointLight("light2", position, this)
    }

    /*
    private var font: Font? = null
    */
    val defaultMesh =  MeshBuilder.CreateBox(
        "default",
        BoxOptions(size = 0.8, faceColors = arrayOf(Color3.Green().toColor4(1))),
        scene
    ).apply {
        isVisible = false
    }

    val agentMesh: MutableMap<Int, AbstractMesh> = mutableMapOf()

    val assetsManager = AssetsManager(scene)
    val assetScenes: MutableMap<Asset3d, AbstractMesh> = mutableMapOf()


    private var sourceMeshes by observable(sourceMeshes()) { _, old, _ ->
        old.values.forEach { it.dispose() }
    }

    private var fieldSupports: Map<Int, FieldSupport> by observable(mapOf()) { _, old, new ->
        old.values.forEach {
            scene.removeMesh(it.mesh)
            it.dispose()
        }
        new.values.forEach {
            scene.addMesh(it.mesh)
        }
    }

    val camera = ArcRotateCamera(
        "Camera", 0, 0, 1.25 * simulator.simulation.width, Vector3(0, 0, 0), scene
    ).apply {
        lowerAlphaLimit = 0
        upperAlphaLimit = 2*PI
        alpha = 1.5*PI
        lowerBetaLimit = -2*PI
        upperBetaLimit = 2*PI
        beta = PI

        panningSensibility = 50

        attachControl(simulationCanvas.root, false)
    }

    val plane = MeshBuilder.CreatePlane("plane", PlaneOptions(size = 100), scene).apply {
        rotate(Axis.X, PI/2)

        val material = StandardMaterial("plane material", scene)
        material.emissiveColor = Color3.Black()
        material.wireframe = true
        this.material = material
    }

    //val pointer = Mesh("pointer", scene)
    val pointer = MeshBuilder.CreateBox("pointer", BoxOptions(faceColors = Array(6) { Color3.Red().toColor4(0.8) }), scene)

    // simulation content edition
    private var selectedTool: EditTools = EditTools.Move
    private var selectedSize: ToolSize = ToolSize.Fine

    var drawStep = -1

    var simulationSourceX = -1
    var simulationSourceY = -1

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

    fun selectPointer() {
        val color = colorFromName(selectedGrainController.data?.color ?: "Green")
        selectedTool.changePointer(pointer, color, selectedSize.size)
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

    fun drawOnSimulation(step: Int) {
        when (selectedTool) {
            EditTools.Pen -> {
                if (step >= 0) {
                    selectedGrainController.data?.id?.let { idToSet ->
                        circle(simulationX, simulationY) { i, j ->
                            data.setIdAtIndex(
                                data.simulation.toIndex(i, j),
                                idToSet
                            )
                        }
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
                selectedGrainController.data?.id?.let { idToSet ->
                    if (step >= 0) {
                        val sprayDensity = 0.005
                        circle(simulationX, simulationY) { i, j ->
                            if (Random.nextDouble() < sprayDensity) {
                                data.setIdAtIndex(data.simulation.toIndex(i, j), idToSet)
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

        onUpdate(step == -1, data, this)
        refresh()
    }

    private fun onPointerDown(evt: PointerEvent, pickInfo: PickingInfo, type: PointerEventTypes) {
        val info = pickInfo.ray?.intersectsMesh(plane, true)
        if (selectedTool != EditTools.Move && info?.hit == true && info.pickedMesh == plane) {
            drawStep = 0

        }
    }

    private fun onPointerMove(evt: PointerEvent, pickInfo: PickingInfo, type: PointerEventTypes) {
        val ray = pickInfo.ray
        if (ray != null && selectedTool != EditTools.Move) {
            val info = ray.intersectsMesh(plane, true)
            if (info.pickedMesh != null) {
                val x = round(info.pickedPoint?.x?.toDouble() ?: 0.0)
                val y = round(info.pickedPoint?.z?.toDouble() ?: 0.0)

                //simulationX = x + 50
                //simulationY = y + 50

                console.log("${info.pickedPoint?.x} x ${info.pickedPoint?.z} -> ${x.toFixed(2)} x ${y.toFixed(2)}")
                selectedTool.positionPointer(pointer, x, y)
                if (drawStep >= 0) {
                    drawStep += 1
                }
            }

        }
    }
    private fun onPointerUp(evt: PointerEvent, pickInfo: PickingInfo?, type: PointerEventTypes) {
        drawStep = -1
    }

    private fun mouseChange(event: MouseEvent) {
        // only update if there a tool
        if (selectedTool != EditTools.Move) {
            val rectangle = simulationCanvas.root.getBoundingClientRect()
            val x = ((event.clientX - rectangle.left) / rectangle.width) * 2 - 1
            val y = -((event.clientY - rectangle.top) / rectangle.height) * 2 + 1

            val picked = scene.pick(x, y, { it == plane }, true, camera)
            if (picked != null) {
                pointer.isVisible = true

            }
            /*
            // updates the picking ray with the camera and mouse position
            rayCaster.setFromCamera(Vector2(x, y), camera)

            // calculates objects intersecting the picking ray
            val intersect = rayCaster.intersectObject(plane, false).firstOrNull()
            if (intersect != null) {
                pointer.visible = true

                val planeX = (intersect.point.x - 0.5).roundToInt()
                val planeY = (intersect.point.z - 0.5).roundToInt()

                selectedTool.positionPointer(pointer, planeX, planeY)

                val clicked = event.buttons.toInt() == 1
                val newStep = when {
                    clicked && drawStep >= 0 -> drawStep + 1
                    clicked -> 0
                    drawStep >= 0 -> -1
                    else -> null
                }

                simulationX = planeX + 50
                simulationY = planeY + 50

                if (newStep != null) {
                    if (newStep == 0) {
                        simulationSourceX = simulationX
                        simulationSourceY = simulationY
                    }
                    drawStep = newStep

                    selectedTool.updatePointer(pointer, drawStep)
                    drawOnSimulation(drawStep)
                }

                render()
            } else {
                if (pointer.visible) {
                    pointer.visible = false
                    render()
                }
            }
             */
        }
    }

    init {

        /*
        page.appContext.getFont("/font/fa-solid-900.json").then {
            font = it
            geometries = geometries()
            refresh()
        }
        simulationCanvas.root.apply {
            onmouseup = { mouseChange(it) }
            onmousedown = { mouseChange(it) }
            onmousemove = { mouseChange(it) }
            onmouseout = {
                pointer.visible = false
                render()
            }
        }
        */

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
        grain.id to /*font?*/null.let {
            val height = grain.size.coerceAtLeast(0.1)
            val color = colorFromName(grain.color).toColor4(1)
            when (grain.icon) {
                "square" -> MeshBuilder.CreateBox(grain.name, BoxOptions(size = 0.8, faceColors = Array(6) {color}), scene)
                "square-full" -> MeshBuilder.CreateBox(grain.name, BoxOptions(size = 1.0, faceColors = Array(6) {color}), scene)
                "circle" -> MeshBuilder.CreateCylinder(grain.name, CylinderOptions(height = height, diameter = 1.0, faceColors = Array(3) {color}), scene)
                else -> MeshBuilder.CreateBox(grain.name, BoxOptions(size = 1.0, faceColors = Array(6) {color}), scene)
                    /*
                    TextBufferGeometry(grain.iconString, TextGeometryParametersImpl(it, 0.8, height)).apply {
                        // moves the geometry into place
                        rotateX(PI / 2)
                        rotateY(PI)
                        translate(0.5, height / 2.0, 0.5)
                        rotateZ(PI)
                    }
                     */
            }.apply {
                isVisible = false
                //translate(Axis.Y, height / 2.0)
                //rotate(Axis.X, PI/2)
            }
        }
    }.toMap()

    private fun fieldSupports() = data.model.fields.map { field ->
        val levels = data.field(field.id)

        val texture = RawTexture.CreateAlphaTexture(
            levels.alpha(), data.simulation.width, data.simulation.height,
            scene, false, false, Texture.NEAREST_SAMPLINGMODE
        )
        val material = StandardMaterial("${field.name} material", scene)
        val color = colorFromName(field.color)
        material.diffuseColor = color
        //material.emissiveColor = color
        //material.ambientColor = color
        material.opacityTexture = texture

        val mesh = MeshBuilder.CreatePlane("${field.name} mesh", PlaneOptions(size = 100, sideOrientation = Mesh.DOUBLESIDE), scene)
        mesh.material = material
        mesh.rotate(Axis.X, PI/2)
        scene.addMesh(mesh)
        field.id to FieldSupport(mesh, texture)
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
                    val mesh = task.loadedMeshes[0]
                    // clone scene to allow multiple occurrences of the same asset
                    val cloned = mesh
                    cloned.position.set(asset.x, asset.y, asset.z)
                    cloned.scaling.set(asset.xScale, asset.yScale, asset.zScale)
                    if (asset.xRotation != 0.0 || asset.yRotation != 0.0 || asset.zRotation != 0.0) {
                        cloned.rotationQuaternion = Quaternion.FromEulerAngles(asset.xRotation, asset.yRotation, asset.zRotation)
                    }
                    if (asset.opacity < 1.0) {
                        cloned.visibility = asset.opacity
                        cloned.getChildMeshes().forEach { it.visibility = asset.opacity }
                    }
                    scene.addMesh(cloned)
                    assetScenes[asset] = cloned
                    render()
                },
                {m, _ ->
                    page.error("Error loading asset: $m")
                }
            )
        }
    }

    fun createMesh(index: Int, grainId: Int, x: Double, y: Double): InstancedMesh {
        // creates mesh
        val mesh = (sourceMeshes[grainId] ?: defaultMesh).createInstance("$index")
        //mesh.receiveShadows = true

        if (grainId == 0) {
            console.log("Grain pos: $x x $y")
        }
        // positions the mesh
        mesh.position.set(x,  0, y)

        // adds the mesh to scene and register it
        scene.addMesh(mesh)
        agentMesh[index] = mesh
        return mesh
    }

    fun transformMesh(index: Int, newGrainId: Int) {
        val mesh = agentMesh[index]
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

        /*
        when {
            mesh != null && newGrainId >= 0 -> {
                // transform it to new one
                //mesh.geometry = geometries[newGrainId] ?: defaultGeometry
                //mesh.material = materials[newGrainId] ?: defaultMaterial
            }
            mesh != null && newGrainId < 0 -> {
                // deletes the mesh
                agentMesh.remove(index)
                scene.removeMesh(mesh, true)
                mesh.dispose()
            }
            mesh == null && newGrainId >= 0 -> {
                val p = data.simulation.toPosition(index)
                createMesh(
                    index,
                    newGrainId,
                    p.x - data.simulation.width / 2.0 + 0.5,
                    p.y - data.simulation.height / 2.0 + 0.5
                )
            }
        }
         */
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
        fieldSupports.forEach { it.value.texture.update(data.field(it.key).alpha()) }
    }

    fun render() {
        scene.render()
    }

    override fun refresh() {
        // applies transform mesh to all simulation
        for (i in data.currentAgents.indices) {
            transformMesh(i, data.idAtIndex(i))
        }

        fieldSupports.forEach { it.value.texture.update(data.field(it.key).alpha()) }
        render()
    }

    fun dispose() {
        sourceMeshes = emptyMap()
        defaultMesh.dispose()
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

    /** Computes alpha value for opacity for each field level in the array */
    fun FloatArray.alpha() = Uint8Array(this.size).also {
        for (i in this.indices) {
            val l = this[i]
            val a = when {
                l <= minFieldLevel -> 0
                l >= 0.1f -> 64 + (80*l - 8).roundToInt()
                else -> (64 / -log10(l)).roundToInt()
            }
            it[i] = a.toByte()
        }
    }

}
