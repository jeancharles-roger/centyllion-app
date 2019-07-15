@file:JsQualifier("THREE")
@file:Suppress("ConvertSecondaryConstructorToPrimary")
package threejs.lights

import info.laht.threekt.lights.Light
import info.laht.threekt.lights.LightShadow


external class PointLightShadow: LightShadow {
    // TODO make cameri LightShadow open
    //override var camera: PerspectiveCamera
}

/**
 * Affects objects using {@link MeshLambertMaterial} or {@link MeshPhongMaterial}.
 *
 * @example
 * var light = new THREE.PointLight( 0xff0000, 1, 100 );
 * light.position.set( 50, 50, 50 );
 * scene.add( light );
 */
external class PointLight: Light {

    constructor(
        color: String = definedExternally, intensity: Number? = definedExternally,
        distance: Number? = definedExternally, decay: Number? = definedExternally
    );

    /**
     * If non-zero, light will attenuate linearly from maximum intensity at light position down to zero at distance.
     * Default — 0.0.
     */
    var distance: Number

    var decay: Number
    var shadow: PointLightShadow
    var power: Number

}
