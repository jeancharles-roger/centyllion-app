package threejs.geometries

import threejs.extra.core.Font

class TextGeometryParametersImpl(
    override var font: Font,
    override var size: Number? = null,
    override var height: Number? = null,
    override var curveSegments: Number? = null,
    override var bevelEnabled: Boolean? = null,
    override var bevelThickness: Number? = null,
    override var bevelSize: Number? = null,
    override var bevelOffset: Number? = null,
    override var bevelSegments: Number? = null
): TextGeometryParameters
