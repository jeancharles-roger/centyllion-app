package com.centyllion.client.page

import bulma.BulmaElement
import bulma.Title
import bulma.div
import bulma.wrap
import com.centyllion.client.AppContext
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.BoxBufferGeometry
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
import kotlin.browser.document
import kotlin.browser.window

class TestPage(context: AppContext) : BulmaElement {

    val camera = PerspectiveCamera(75, 4.0 / 3.0, 00.1, 1000.0).apply {
        position.setZ(5)
    }

    val cube = Mesh(
        BoxBufferGeometry(1, 1, 1),
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
        add(cube)
    }

    val canvas = document.create.canvas {
        width = "400px"
        height = "300px"
    }

    val renderer = WebGLRenderer(WebGLRendererParams(canvas, antialias = true)).apply {
        setClearColor(ColorConstants.skyblue, 1)
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
        animate()
    }
}
