@file:JsQualifier("BABYLON")
@file:Suppress("unused", "ConvertSecondaryConstructorToPrimary", "CovariantEquals")
package babylonjs

/**
 * Interface used to define a behavior
 */
external interface Behavior<T> {
    /** gets or sets behavior's name */
    val name: String
    /**
     * Function called when the behavior needs to be initialized (after attaching it to a target)
     */
    fun `init`()
    /**
     * Called when the behavior is attached to a target
     * @param target defines the target where the behavior is attached to
     */
    fun attach(target: T)
    /**
     * Called when the behavior is detached from its target
     */
    fun detach()
}

/**
 * Interface implemented by classes supporting behaviors
 */
external interface IBehaviorAware<T> {
    /**
     * Attach a behavior
     * @param behavior defines the behavior to attach
     * @returns the current host
     */
    fun addBehavior(behavior: Behavior<T>): T
    /**
     * Remove a behavior from the current object
     * @param behavior defines the behavior to detach
     * @returns the current host
     */
    fun removeBehavior(behavior: Behavior<T>): T
    /**
     * Gets a behavior using its name to search
     * @param name defines the name to search
     * @returns the behavior or null if not found
     */
    fun getBehaviorByName(name: String): Behavior<T>?
}
