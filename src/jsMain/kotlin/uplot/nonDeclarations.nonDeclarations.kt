@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package uplot

typealias AlignedData = Any

typealias AxisSplitsFilter = (self: uPlot, splits: Array<Number>, axisIdx: Number, foundSpace: Number, foundIncr: Number) -> Array<Number?>

typealias Hooks = Any

typealias PluginHooks = Any