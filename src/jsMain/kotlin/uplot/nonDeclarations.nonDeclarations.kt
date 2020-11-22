@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package uplot

import kotlin.js.Date

typealias AlignedData = Any

typealias AxisSplitsFilter = (self: uPlot, splits: Array<Number>, axisIdx: Number, foundSpace: Number, foundIncr: Number) -> Array<Number?>

typealias Hooks = Any

typealias PluginHooks = Any


class ScaleLimit (
    var min: Number,
    var max: Number,
)

class Position (
    var left: Number,
    var top: Number,
)

class SerieOptions(
    var show: Boolean? = null,
    var focus: Boolean? = null,
)

class Size(
    var width: Number,
    var height: Number,
)

class SelectOptions(
    var left: Number,
    var top: Number,
    var width: Number,
    var height: Number,
)

class Scale(
    var time: Boolean? = null,
    var auto: Boolean? = null,
    /*
    var range: dynamic /* dynamic | ((self: uPlot, initMin: Number, initMax: Number, scaleKey: String) -> dynamic)? */
        get() = definedExternally
        set(value) = definedExternally
     */
    var from: String? = null,
    var distr: Number /* 1 | 2 | 3 */ = 1,
    var log: Number /* 10 | 2 */ = 10,
    var min: Number? = null,
    var max: Number? = null,
)

class Axis(
    var show: Boolean? = null,
    var scale: String? = null,
    var side: Number? = null,
    var size: Number? = null,
    var gap: Number? = null,
    var font: Any? = null,
    var stroke: Any? = null,
    var label: String? = null,
    var labelSize: Number? = null,
    var labelFont: Any? = null,
    var space: dynamic = null, /* Number? | ((self: uPlot, axisIdx: Number, scaleMin: Number, scaleMax: Number, plotDim: Number) -> Number)? */
    var incrs: dynamic = null, /* Array<Number>? | ((self: uPlot, axisIdx: Number, scaleMin: Number, scaleMax: Number, fullDim: Number, minSpace: Number) -> Array<Number>)? */
    var splits: dynamic = null, /* Array<Number>? | ((self: uPlot, axisIdx: Number, scaleMin: Number, scaleMax: Number, foundIncr: Number, pctSpace: Number) -> Array<Number>)? */
    var filter: AxisSplitsFilter? = null,
    var values: dynamic = null, /* ((self: uPlot, splits: Array<Number>, axisIdx: Number, foundSpace: Number, foundIncr: Number) -> Array<dynamic /* String? | Number? */>)? | Array<Array<dynamic /* String? | Number? */>>? */
    var rotate: dynamic = null, /* Number? | ((self: uPlot, values: Array<dynamic /* String | Number */>, axisIdx: Number, foundSpace: Number) -> Number)? */
    var grid: uPlot.`T$16`? = null,
    var ticks: uPlot.`T$17`? = null,
)

class Options(
    var title: String? = null,
    var id: String? = null,
    var `class`: String? = null,
    var width: Number,
    var height: Number,
    var data: AlignedData? = null,
    var tzDate: ((ts: Number) -> Date)? = null,
    var fmtDate: ((tpl: String) -> (date: Date) -> String)? = null,
    var series: Array<Series> = emptyArray(),
    var scales: Map<String, Scale>? = null,
    var axes: Array<Axis>? = null,
    var gutters: uPlot.`T$7`? = null,
    var select: uPlot.Select? = null,
    var legend: uPlot.`T$8`? = null,
    var cursor: uPlot.Cursor? = null,
    var focus: uPlot.Focus? = null,
    var hooks: Hooks? = null,
    var plugins: Array<uPlot.`T$9`>? = null,
)

class Series(
    var show: Boolean? = null,
    var `class`: String? = null,
    var scale: String? = null,
    var auto: Boolean? = null,
    //var sorted: dynamic /* Number | Number | String */
    //var sorted: Number = 0,
    var spanGaps: Boolean? = null,
    var label: String? = null,
    var value: dynamic = null, /* String? | ((self: uPlot, rawValue: Number, seriesIdx: Number, idx: Number) -> dynamic)? */
    var values: ((self: uPlot, seriesIdx: Number, idx: Number) -> Any?)? = null,
    var paths: ((self: uPlot, seriesIdx: Number, idx0: Number, idx1: Number) -> uPlot.`T$14`)? = null,
    var points: uPlot.`T$15`? = null,
    var band: Boolean? = null,
    var stroke: Any? = null,
    var width: Any? = null,
    var fill: Any? = null,
    var fillTo: dynamic = null, /* Number? | ((self: uPlot, seriesIdx: Number, dataMin: Number, dataMax: Number) -> Number)? */
    var dash: Array<Number>? = arrayOf(1),
    var alpha: Number? = 1,
    var idxs: dynamic = null, /* JsTuple<Number, Number> */
    var min: Number? = null,
    var max: Number? = null,
)