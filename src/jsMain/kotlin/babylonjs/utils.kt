@file:Suppress("unused")
package babylonjs

// TODO some clever type definition are Any fornow
/**
 * Alias type for number that are floats
 * @ignorenaming
 */
typealias Float = Number
/**
 * Alias type for number that are doubles.
 * @ignorenaming
 */
typealias Double = Number
/**
 * Alias type for number that are integer
 * @ignorenaming
 */
typealias Int = Number
/** Alias type for number array or Float32Array */
typealias FloatArray = Array<Number> /*| Float32Array*/
/** Alias type for number array or Float32Array or Int32Array or Uint32Array or Uint16Array */
typealias IndicesArray = Array<Number> /*| Int32Array | Uint32Array | Uint16Array*/
/**
 * Alias for types that can be used by a Buffer or VertexBuffer.
 */
typealias DataArray = Array<Number> /*| ArrayBuffer | ArrayBufferView*/
/**
 * Alias type for primitive types
 * @ignorenaming
 */
//type Primitive = undefined | null | boolean | string | number | Function;
/**
 * Type modifier to make all the properties of an object Readonly
 */
typealias Immutable<T> = Any // T extends Primitive ? T : T extends Array<infer U> ? ReadonlyArray<U> : DeepImmutable<T>;
/**
 * Type modifier to make all the properties of an object Readonly recursively
 */
typealias DeepImmutable<T> = Any //  T extends Primitive ? T : T extends Array<infer U> ? DeepImmutableArray<U> : DeepImmutableObject<T>;


/**
 * Defines how a node can be built from a string name.
 */
typealias NodeConstructor = ((name: String, scene: Scene, options: Any?) -> Unit) -> Node

interface MinMax {
    val minimum: Vector3
    val maximum: Vector3
}
