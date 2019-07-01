@file:JsQualifier("THREE")
package threejs.loaders

import info.laht.threekt.loaders.LoadingManager
import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent
import threejs.extra.core.Font

open external class FontLoader(manager: LoadingManager? = definedExternally) {

    var manager: LoadingManager

    fun load(
        url: String,
        onLoad: (( responseFont: Font ) -> Unit)?,
        onProgress: (( event: ProgressEvent ) -> Unit)?,
        onError: (( event: ErrorEvent ) -> Unit)?
    )

    fun parse( json: Any ): Font

}
