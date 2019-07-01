@file:JsQualifier("THREE")
@file:Suppress("ConvertSecondaryConstructorToPrimary")

package threejs.geometries

import threejs.extra.core.Font

external interface TextGeometryParameters {
    var font: Font
    var size: Number?
    var height: Number?
    var curveSegments: Number?
    var bevelEnabled: Boolean?
    var bevelThickness: Number?
    var bevelSize: Number?
    var bevelOffset: Number?
    var bevelSegments: Number?
}

open external class TextBufferGeometry: ExtrudeBufferGeometry {

    constructor(text: String, parameters: TextGeometryParameters = definedExternally)

    val parameters: TextGeometryParameters?
}

open external class TextGeometry: ExtrudeGeometry {

    constructor(text: String, parameters: TextGeometryParameters = definedExternally)

    val parameters: TextGeometryParameters?

}
