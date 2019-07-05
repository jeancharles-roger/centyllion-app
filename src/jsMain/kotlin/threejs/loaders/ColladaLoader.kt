@file:JsQualifier("THREE")
package threejs.loaders

import info.laht.threekt.animation.AnimationClip
import info.laht.threekt.loaders.LoadingManager
import info.laht.threekt.scenes.Scene
import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent


external interface Collada {
    var animations: Array<AnimationClip>
    var kinematics: Any
    var library: Any
    var scene: Scene
}

external class ColladaLoader(manager: LoadingManager?) {

    var manager: LoadingManager
    var crossOrigin: String
    var path: String
    var resourcePath: String

    fun load(
        url: String, onLoad: (collada: Collada) -> Unit,
        onProgress: ((event: ProgressEvent) -> Unit)? = definedExternally,
        onError: ((event: ErrorEvent) -> Unit)? = definedExternally
    )
    fun setPath(path: String) : ColladaLoader
    fun setResourcePath(path: String) : ColladaLoader
    fun setCrossOrigin(value: String): ColladaLoader

    fun parse(text: String, path: String) : Collada
}
