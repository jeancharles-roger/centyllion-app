@file:JsModule("uplot")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package uplot

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLElement
import org.w3c.dom.Path2D
import kotlin.js.Date

@JsName("default")
external open class uPlot {
    constructor(opts: Options, data: AlignedData, targ: HTMLElement = definedExternally)
    //constructor(opts: Options, data: AlignedData, targ: (self: uPlot, init: Function<*>) -> Unit = definedExternally)
    open var root: HTMLElement
    open var width: Number
    open var height: Number
    open var ctx: CanvasRenderingContext2D
    open var bbox: BBox
    open var select: BBox
    open var cursor: Cursor
    open var series: Array<Series>
    open var scales: Map<String, Scale>
    open var axes: Array<Axis>
    open var hooks: Hooks
    open var data: AlignedData
    open fun redraw(rebuildPaths: Boolean = definedExternally)
    open fun batch(txn: Function<*>)
    open fun destroy()
    open fun setData(data: AlignedData, resetScales: Boolean = definedExternally)
    open fun setScale(scaleKey: String, limits: ScaleLimit)
    open fun setCursor(opts: Position)
    open fun setSeries(seriesIdx: Number, opts: SerieOptions)
    open fun addSeries(opts: Series, seriesIdx: Number = definedExternally)
    open fun delSeries(seriesIdx: Number)
    open fun setSelect(opts: SelectOptions, fireHook: Boolean = definedExternally)
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