@file:JsQualifier("THREE")

package threejs.geometries

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry
import info.laht.threekt.extras.core.CurvePath
import info.laht.threekt.extras.core.Shape
import info.laht.threekt.math.Vector2
import info.laht.threekt.math.Vector3

external interface ExtrudeGeometryOptions {
    var curveSegments: Number?
    var steps: Number?
    var depth: Number?
    var bevelEnabled: Boolean?
    var bevelThickness: Number?
    var bevelSize: Number?
    var bevelOffset: Number?
    var bevelSegments: Number?
    var extrudePath: CurvePath<Vector3>?
    var UVGenerator: UVGenerator?
}

external interface UVGenerator {
	fun generateTopUV(
		geometry: ExtrudeBufferGeometry,
		vertices: Array<Number>,
		indexA: Number,
		indexB: Number,
		indexC: Number
	): Array<Vector2>

	fun generateSideWallUV(
		geometry: ExtrudeBufferGeometry,
		vertices: Array<Number>,
		indexA: Number,
		indexB: Number,
		indexC: Number,
		indexD: Number
	): Array<Vector2>
}

open external class ExtrudeBufferGeometry: BufferGeometry {

	constructor(shapes: Shape, options: ExtrudeGeometryOptions? = definedExternally )
	constructor(shapes: Array<Shape>, options: ExtrudeGeometryOptions? = definedExternally)

	companion object {
		val WorldUVGenerator: UVGenerator = definedExternally
	}

	fun addShapeList( shapes: Array<Shape>, options: Any? = definedExternally )
	fun addShape( shape: Shape, options: Any? = definedExternally )
}

open external class ExtrudeGeometry: Geometry {

	constructor( shapes: Shape, options: ExtrudeGeometryOptions? = definedExternally )
	constructor( shapes: Array<Shape>, options: ExtrudeGeometryOptions? = definedExternally )

	companion object {
		val WorldUVGenerator: UVGenerator = definedExternally
	}

	fun addShapeList( shapes: Array<Shape>, options: Any? = definedExternally )
	fun addShape( shape: Shape, options: Any? = definedExternally )
}
