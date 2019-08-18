@file:JsQualifier("BABYLON")
@file:Suppress("unused")
package babylonjs

import org.khronos.webgl.WebGLContextAttributes
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement

/** Interface defining initialization parameters for Engine class */
external interface EngineOptions: WebGLContextAttributes {
    /**
     * Defines if the engine should no exceed a specified device ratio
     * @see https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio
     */
    var limitDeviceRatio: Number?
    /**
     * Defines if webvr should be enabled automatically
     * @see http://doc.babylonjs.com/how_to/webvr_camera
     */
    var autoEnableWebVR: Boolean?
    /**
     * Defines if webgl2 should be turned off even if supported
     * @see http://doc.babylonjs.com/features/webgl2
     */
    var disableWebGL2Support: Boolean?
    /**
     * Defines if webaudio should be initialized as well
     * @see http://doc.babylonjs.com/how_to/playing_sounds_and_music
     */
    var audioEngine: Boolean?
    /**
     * Defines if animations should run using a deterministic lock step
     * @see http://doc.babylonjs.com/babylon101/animations#deterministic-lockstep
     */
    var deterministicLockstep: Boolean?
    /** Defines the maximum steps to use with deterministic lock step mode */
    var lockstepMaxSteps: Number?
    /**
     * Defines that engine should ignore context lost events
     * If this event happens when this parameter is true, you will have to reload the page to restore rendering
     */
    var doNotHandleContextLost: Boolean?
    /**
     * Defines that engine should ignore modifying touch action attribute and style
     * If not handle, you might need to set it up on your side for expected touch devices behavior.
     */
    var doNotHandleTouchAction: Boolean?
    /**
     * Defines that engine should compile shaders with high precision floats (if supported). True by default
     */
    var useHighPrecisionFloats: Boolean?
}

external class Engine {

    /**
     * Creates a new engine
     * @param canvas defines the canvas use for rendering.
     * @param antialias defines enable antialiasing (default: false)
     * @param options defines further options to be sent to the getContext() function
     * @param adaptToDeviceRatio defines whether to adapt to the device's viewport characteristics (default: false)
     */
    constructor(canvas: HTMLCanvasElement?, antialias: Boolean? = definedExternally, options: EngineOptions? = definedExternally, adaptToDeviceRatio: Boolean? = definedExternally)

    /**
     * Creates a new engine
     * @param context defines WebGL context to use for rendering. Babylon.js will not hook events on the canvas (like pointers, keyboards, etc...) so no event observables will be available. This is mostly used when Babylon.js is used as a plugin on a system which alreay used the WebGL context
     * @param antialias defines enable antialiasing (default: false)
     * @param options defines further options to be sent to the getContext() function
     * @param adaptToDeviceRatio defines whether to adapt to the device's viewport characteristics (default: false)
     */
    constructor(context: WebGLRenderingContext, antialias: Boolean? = definedExternally, options: EngineOptions? = definedExternally, adaptToDeviceRatio: Boolean? = definedExternally)

}
