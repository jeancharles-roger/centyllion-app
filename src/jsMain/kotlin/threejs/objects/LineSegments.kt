@file:JsQualifier("THREE")
@file:Suppress("ConvertSecondaryConstructorToPrimary")
package threejs.objects


import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry
import info.laht.threekt.materials.Material
import info.laht.threekt.objects.Line

open external class LineSegments : Line {

    constructor(geometry: BufferGeometry, material: Material)
    constructor(geometry: Geometry, material: Material)

}
