@file:JsModule("uplot")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package uplot

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.Path2D
import uplot.uPlot.Scale
import kotlin.js.Date

external interface `T$0` {
    @nativeGetter
    operator fun get(key: String): Scale?
    @nativeSetter
    operator fun set(key: String, value: Scale)
}

external interface `T$1` {
    var min: Number
    var max: Number
}

external interface `T$2` {
    var left: Number
    var top: Number
}

external interface `T$3` {
    var show: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var focus: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

external interface `T$4` {
    var left: Number
    var top: Number
    var width: Number
    var height: Number
}

external interface Size {
    var width: Number
    var height: Number
}

@JsName("default")
external open class uPlot {
    constructor(opts: Options, data: AlignedData, targ: HTMLElement = definedExternally)
    constructor(opts: Options, data: AlignedData, targ: (self: uPlot, init: Function<*>) -> Unit = definedExternally)
    open var root: HTMLElement
    open var width: Number
    open var height: Number
    open var ctx: CanvasRenderingContext2D
    open var bbox: BBox
    open var select: BBox
    open var cursor: Cursor
    open var series: Array<Series>
    open var scales: `T$0`
    open var axes: Array<Axis>
    open var hooks: Hooks
    open var data: AlignedData
    open fun redraw(rebuildPaths: Boolean = definedExternally)
    open fun batch(txn: Function<*>)
    open fun destroy()
    open fun setData(data: AlignedData, resetScales: Boolean = definedExternally)
    open fun setScale(scaleKey: String, limits: `T$1`)
    open fun setCursor(opts: `T$2`)
    open fun setSeries(seriesIdx: Number, opts: `T$3`)
    open fun addSeries(opts: Series, seriesIdx: Number = definedExternally)
    open fun delSeries(seriesIdx: Number)
    open fun setSelect(opts: `T$4`, fireHook: Boolean = definedExternally)
    open fun setSize(opts: Size)
    open fun posToIdx(left: Number): Number
    open fun posToVal(leftTop: Number, scaleKey: String): Number
    open fun valToPos(param_val: Number, scaleKey: String, canvasPixels: Boolean = definedExternally): Number
    open fun valToIdx(param_val: Number): Number
    open fun syncRect()
    interface DateNames {
        var MMMM: Array<String>
        var MMM: Array<String>
        var WWWW: Array<String>
        var WWW: Array<String>
    }
    interface `T$7` {
        var x: Number?
            get() = definedExternally
            set(value) = definedExternally
        var y: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$8` {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var live: Boolean?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$9` {
        var opts: ((self: uPlot, opts: Options) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var hooks: PluginHooks
    }
    interface Options {
        var title: String?
            get() = definedExternally
            set(value) = definedExternally
        var id: String?
            get() = definedExternally
            set(value) = definedExternally
        var `class`: String?
            get() = definedExternally
            set(value) = definedExternally
        var width: Number
        var height: Number
        var data: AlignedData?
            get() = definedExternally
            set(value) = definedExternally
        var tzDate: ((ts: Number) -> Date)?
            get() = definedExternally
            set(value) = definedExternally
        var fmtDate: ((tpl: String) -> (date: Date) -> String)?
            get() = definedExternally
            set(value) = definedExternally
        var series: Array<Series>
        var scales: Map<String, Scale>?
            get() = definedExternally
            set(value) = definedExternally
        var axes: Array<Axis>?
            get() = definedExternally
            set(value) = definedExternally
        var gutters: `T$7`?
            get() = definedExternally
            set(value) = definedExternally
        var select: Select?
            get() = definedExternally
            set(value) = definedExternally
        var legend: `T$8`?
            get() = definedExternally
            set(value) = definedExternally
        var cursor: Cursor?
            get() = definedExternally
            set(value) = definedExternally
        var focus: Focus?
            get() = definedExternally
            set(value) = definedExternally
        var hooks: Hooks?
            get() = definedExternally
            set(value) = definedExternally
        var plugins: Array<`T$9`>?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface Focus {
        var alpha: Number
    }
    interface BBox {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var left: Number
        var top: Number
        var width: Number
        var height: Number
    }
    interface Select : BBox {
        var over: Boolean?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$10` {
        var show: dynamic /* Boolean? | ((self: uPlot, seriesIdx: Number) -> HTMLElement)? */
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$11` {
        var setScale: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var x: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var y: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var dist: Number?
            get() = definedExternally
            set(value) = definedExternally
        var uni: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$12` {
        var key: String
        var setSeries: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var scales: dynamic /* JsTuple<String, String> */
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$13` {
        var prox: Number
    }
    interface Cursor {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var x: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var y: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var left: Number?
            get() = definedExternally
            set(value) = definedExternally
        var top: Number?
            get() = definedExternally
            set(value) = definedExternally
        var idx: Number?
            get() = definedExternally
            set(value) = definedExternally
        var dataIdx: ((self: uPlot, seriesIdx: Number, closestIdx: Number, xValue: Number) -> Number)?
            get() = definedExternally
            set(value) = definedExternally
        var move: ((self: uPlot, mouseLeft: Number, mouseTop: Number) -> dynamic)?
            get() = definedExternally
            set(value) = definedExternally
        var points: `T$10`?
            get() = definedExternally
            set(value) = definedExternally
        var drag: `T$11`?
            get() = definedExternally
            set(value) = definedExternally
        var sync: `T$12`?
            get() = definedExternally
            set(value) = definedExternally
        var focus: `T$13`?
            get() = definedExternally
            set(value) = definedExternally
        var lock: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var locked: Boolean?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface Scale {
        var time: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var auto: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var range: dynamic /* dynamic | ((self: uPlot, initMin: Number, initMax: Number, scaleKey: String) -> dynamic)? */
            get() = definedExternally
            set(value) = definedExternally
        var from: String?
            get() = definedExternally
            set(value) = definedExternally
        var distr: Number? /* 1 | 2 | 3 */
            get() = definedExternally
            set(value) = definedExternally
        var log: Number? /* 10 | 2 */
            get() = definedExternally
            set(value) = definedExternally
        var min: Number?
            get() = definedExternally
            set(value) = definedExternally
        var max: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$14` {
        var stroke: Path2D?
            get() = definedExternally
            set(value) = definedExternally
        var fill: Path2D?
            get() = definedExternally
            set(value) = definedExternally
        var clip: Path2D?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$15` {
        var show: dynamic /* Boolean? | ((self: uPlot, seriesIdx: Number, idx0: Number, idx1: Number) -> Boolean?)? */
            get() = definedExternally
            set(value) = definedExternally
        var size: Number?
            get() = definedExternally
            set(value) = definedExternally
        var width: Any?
            get() = definedExternally
            set(value) = definedExternally
        var stroke: Any?
            get() = definedExternally
            set(value) = definedExternally
        var fill: Any?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface Series {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var `class`: String?
            get() = definedExternally
            set(value) = definedExternally
        var scale: String?
            get() = definedExternally
            set(value) = definedExternally
        var auto: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var sorted: dynamic /* Number | Number | String */
            get() = definedExternally
            set(value) = definedExternally
        var spanGaps: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var label: String?
            get() = definedExternally
            set(value) = definedExternally
        var value: dynamic /* String? | ((self: uPlot, rawValue: Number, seriesIdx: Number, idx: Number) -> dynamic)? */
            get() = definedExternally
            set(value) = definedExternally
        var values: ((self: uPlot, seriesIdx: Number, idx: Number) -> Any?)?
            get() = definedExternally
            set(value) = definedExternally
        var paths: ((self: uPlot, seriesIdx: Number, idx0: Number, idx1: Number) -> `T$14`)?
            get() = definedExternally
            set(value) = definedExternally
        var points: `T$15`?
            get() = definedExternally
            set(value) = definedExternally
        var band: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var stroke: Any?
            get() = definedExternally
            set(value) = definedExternally
        var width: Any?
            get() = definedExternally
            set(value) = definedExternally
        var fill: Any?
            get() = definedExternally
            set(value) = definedExternally
        var fillTo: dynamic /* Number? | ((self: uPlot, seriesIdx: Number, dataMin: Number, dataMax: Number) -> Number)? */
            get() = definedExternally
            set(value) = definedExternally
        var dash: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
        var alpha: Number?
            get() = definedExternally
            set(value) = definedExternally
        var idxs: dynamic /* JsTuple<Number, Number> */
            get() = definedExternally
            set(value) = definedExternally
        var min: Number?
            get() = definedExternally
            set(value) = definedExternally
        var max: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$16` {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var filter: AxisSplitsFilter?
            get() = definedExternally
            set(value) = definedExternally
        var stroke: Any?
            get() = definedExternally
            set(value) = definedExternally
        var width: Any?
            get() = definedExternally
            set(value) = definedExternally
        var dash: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface `T$17` {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var filter: AxisSplitsFilter?
            get() = definedExternally
            set(value) = definedExternally
        var stroke: Any?
            get() = definedExternally
            set(value) = definedExternally
        var width: Any?
            get() = definedExternally
            set(value) = definedExternally
        var dash: Array<Number>?
            get() = definedExternally
            set(value) = definedExternally
        var size: Number?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface Axis {
        var show: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var scale: String?
            get() = definedExternally
            set(value) = definedExternally
        var side: Number?
            get() = definedExternally
            set(value) = definedExternally
        var size: Number?
            get() = definedExternally
            set(value) = definedExternally
        var gap: Number?
            get() = definedExternally
            set(value) = definedExternally
        var font: Any?
            get() = definedExternally
            set(value) = definedExternally
        var stroke: Any?
            get() = definedExternally
            set(value) = definedExternally
        var label: String?
            get() = definedExternally
            set(value) = definedExternally
        var labelSize: Number?
            get() = definedExternally
            set(value) = definedExternally
        var labelFont: Any?
            get() = definedExternally
            set(value) = definedExternally
        var space: dynamic /* Number? | ((self: uPlot, axisIdx: Number, scaleMin: Number, scaleMax: Number, plotDim: Number) -> Number)? */
            get() = definedExternally
            set(value) = definedExternally
        var incrs: dynamic /* Array<Number>? | ((self: uPlot, axisIdx: Number, scaleMin: Number, scaleMax: Number, fullDim: Number, minSpace: Number) -> Array<Number>)? */
            get() = definedExternally
            set(value) = definedExternally
        var splits: dynamic /* Array<Number>? | ((self: uPlot, axisIdx: Number, scaleMin: Number, scaleMax: Number, foundIncr: Number, pctSpace: Number) -> Array<Number>)? */
            get() = definedExternally
            set(value) = definedExternally
        var filter: AxisSplitsFilter?
            get() = definedExternally
            set(value) = definedExternally
        var values: dynamic /* ((self: uPlot, splits: Array<Number>, axisIdx: Number, foundSpace: Number, foundIncr: Number) -> Array<dynamic /* String? | Number? */>)? | Array<Array<dynamic /* String? | Number? */>>? */
            get() = definedExternally
            set(value) = definedExternally
        var rotate: dynamic /* Number? | ((self: uPlot, values: Array<dynamic /* String | Number */>, axisIdx: Number, foundSpace: Number) -> Number)? */
            get() = definedExternally
            set(value) = definedExternally
        var grid: `T$16`?
            get() = definedExternally
            set(value) = definedExternally
        var ticks: `T$17`?
            get() = definedExternally
            set(value) = definedExternally
    }
    interface HooksDescription {
        var init: ((self: uPlot, opts: Options, data: AlignedData) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var setScale: ((self: uPlot, scaleKey: String) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var setCursor: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var setSelect: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var setSeries: ((self: uPlot, seriesIdx: Number, opts: Series) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var setData: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var setSize: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var drawClear: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var drawAxes: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var drawSeries: ((self: uPlot, seriesKey: String) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var draw: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var ready: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
        var destroy: ((self: uPlot) -> Unit)?
            get() = definedExternally
            set(value) = definedExternally
    }

    companion object {
        fun assign(targ: Any?, vararg srcs: Any?): Any?
        fun rangeNum(min: Number, max: Number, mult: Number, extra: Boolean): dynamic /* JsTuple<Number, Number> */
        fun rangeLog(min: Number, max: Number, fullMags: Boolean): dynamic /* JsTuple<Number, Number> */
        fun fmtNum(param_val: Number): String
        fun fmtDate(tpl: String, names: DateNames = definedExternally): (date: Date) -> String
        fun tzDate(date: Date, tzName: String): Date
    }
}