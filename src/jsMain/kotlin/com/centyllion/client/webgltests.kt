package com.centyllion.client

import org.khronos.webgl.*
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.browser.window

fun testWebGl() {
    val width = window.innerWidth
    val height = window.innerHeight - 100

    val main = document.querySelector("section.cent-main") as HTMLElement
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    main.appendChild(canvas)

    val gl = canvas.getContext("webgl") as WebGLRenderingContext

    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)

    simpleTutorial(gl)
    //pixelPerfect2d(width, height, gl)
}

fun WebGLRenderingContext.loadShader(source: String, type: Int): WebGLShader? {
    val shader = createShader(type);
    shaderSource(shader, source);
    compileShader(shader);
    val parameter = getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS)
    if (parameter == true) return shader
    // shader failed
    deleteShader(shader)
    return null
}

fun WebGLRenderingContext.loadProgram(vertexShaderSource: String, fragmentShaderSource: String): WebGLProgram? {
    val vertexShader = loadShader(vertexShaderSource, WebGLRenderingContext.VERTEX_SHADER)
    val fragmentShader = loadShader(fragmentShaderSource, WebGLRenderingContext.FRAGMENT_SHADER)
    val program = createProgram()
    attachShader(program, vertexShader)
    attachShader(program, fragmentShader)
    linkProgram(program)
    val parameter = getProgramParameter(program, WebGLRenderingContext.LINK_STATUS)
    if (parameter == true) return program
    // program failed
    deleteProgram(program)
    return null
}

fun WebGLRenderingContext.createMatrix(width: Int = 256, height: Int = 256, type: Int = WebGLRenderingContext.INT): WebGLTexture? {
    val texture = createTexture()
    bindTexture(WebGLRenderingContext.TEXTURE_2D, texture)

    val format = WebGLRenderingContext.RGBA
    val pixels = Int32Array(arrayOf(0, 0, 255, 255))
    texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, format, width, height, 0, format, type, pixels)
    return texture
}



fun simpleTutorial(gl: WebGLRenderingContext) {
    val vertexShader = """
attribute vec2 position;

void main() {
   gl_Position = vec4(position, 0.0, 1.0);
}
""".trimIndent()

    val fragmentShader = """
precision mediump float;
uniform vec4 color;
void main (){
    gl_FragColor = color;
}
""".trimIndent()

    val program = gl.loadProgram(vertexShader, fragmentShader)

    // two triangles to draw a square
    val vertices = Float32Array(
        arrayOf(
            -1f, -1f, -1f, 1f, 1f, 1f,
            -1f, -1f, 1f, -1f, 1f, 1f
        )
    )
    val buffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffer)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, vertices, WebGLRenderingContext.STATIC_DRAW)

    // defines color uniform to use in both shaders
    gl.useProgram(program)
    val color = gl.getUniformLocation(program, "color")
    gl.uniform4fv(color, arrayOf(0f, 1f, 0f, 1f))

    // defines position
    val position = gl.getAttribLocation(program, "position")
    gl.enableVertexAttribArray(position)
    gl.vertexAttribPointer(position, 2, WebGLRenderingContext.FLOAT, false, 0, 0)

    gl.drawArrays(WebGLRenderingContext.TRIANGLES, 0, vertices.length / 2)
}

fun pixelPerfect2d(width: Int, height: Int, gl: WebGLRenderingContext) {
    val positions = Float32Array(arrayOf(0.0f, 0.0f, 50.0f, 0.0f, 50.0f, 50.0f, 0.0f, 50.0f))
    val indices = Uint16Array(shortArrayOf(0, 1, 1, 2, 2, 3, 3, 0).toTypedArray())

    val positionBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, positionBuffer)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, positions, WebGLRenderingContext.STATIC_DRAW)

    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)


    val vertexShader = """
attribute vec4 a_position;
attribute vec2 a_texcoord;

uniform mat4 u_matrix;

varying vec2 v_texcoord;

void main() {
   gl_Position = u_matrix * a_position;
   v_texcoord = a_texcoord;
}
""".trimIndent()

    val fragmentShader = """
precision mediump float;

varying vec2 v_texcoord;

uniform sampler2D u_texture;

void main() {
   gl_FragColor = texture2D(u_texture, v_texcoord);
}
""".trimIndent()

    val program = gl.loadProgram(vertexShader, fragmentShader)
    gl.useProgram(program)

    val positionLocation = gl.getAttribLocation(program, "position")
    gl.enableVertexAttribArray(positionLocation)
    gl.vertexAttribPointer(positionLocation, 2, WebGLRenderingContext.FLOAT, false, 0, 0)
    val ploc = gl.getUniformLocation(program, "projection")
    val mloc = gl.getUniformLocation(program, "model")
    gl.uniformMatrix4fv(
        ploc, false,
        arrayOf(
            2f / width, 0f, 0f, 0f,
            0f, -2f / height, 0f, 0f,
            0f, 0f, 1f, 0f,
            -1f + 1f / width, 1f - 1f / height, 0f, 1f
        )
    )

    (0 until 800).step(50).forEach { ii ->
        gl.uniformMatrix4fv(
            mloc, false,
            arrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                ii.toFloat(), 0f, 0f, 1f
            )
        )
        gl.drawElements(WebGLRenderingContext.LINES, 8, WebGLRenderingContext.UNSIGNED_SHORT, 0)
    }

    (0 until 600).step(50).forEach { ii ->
        gl.uniformMatrix4fv(
            mloc, false,
            arrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, ii.toFloat(), 0f, 1f
            )
        )
        gl.drawElements(WebGLRenderingContext.LINES, 8, WebGLRenderingContext.UNSIGNED_SHORT, 0)
    }
}

