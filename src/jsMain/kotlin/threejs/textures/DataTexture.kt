@file:JsQualifier("THREE")
@file:Suppress("ConvertSecondaryConstructorToPrimary")

package threejs.textures

import info.laht.threekt.textures.Texture
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Float64Array
import org.khronos.webgl.Int16Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.Uint32Array
import org.khronos.webgl.Uint8Array
import threejs.Mapping
import threejs.PixelFormat
import threejs.TextureDataType
import threejs.TextureEncoding
import threejs.TextureFilter
import threejs.Wrapping

open external class DataTexture: Texture {

    constructor(
        data: Uint8Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Int8Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Uint16Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Int16Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Uint32Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Int32Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Float32Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    constructor(
        data: Float64Array,
        width: Number,
        height: Number,
        format: PixelFormat? = definedExternally,
        type: TextureDataType? = definedExternally,
        mapping: Mapping? = definedExternally,
        wrapS: Wrapping? = definedExternally,
        wrapT: Wrapping? = definedExternally,
        magFilter: TextureFilter? = definedExternally,
        minFilter: TextureFilter? = definedExternally,
        anisotropy: Number? = definedExternally,
        encoding: TextureEncoding? = definedExternally
    )

    // TODO what to do ?
    //var image: ImageData;

}
