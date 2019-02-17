@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "NESTED_CLASS_IN_EXTERNAL_INTERFACE")

import kotlin.js.Promise

external interface `T$0` {
    var x: String
    var y: Number
}
external interface `T$1` {
    var name: String
    var data: dynamic /* Array<Number> | Array<`T$0`> */
}

@JsModule("apexcharts")
open external class ApexCharts(el: Any, options: Any) {
    open fun render(): Promise<Unit> = definedExternally
    open fun updateOptions(options: Any, redrawPaths: Boolean, animate: Boolean): Promise<Unit> = definedExternally
    open fun updateSeries(newSeries: Array<`T$1`>, animate: Boolean): Unit = definedExternally
    open fun updateSeries(newSeries: Array<Number>, animate: Boolean): Unit = definedExternally
    open fun toggleSeries(seriesName: String): Unit = definedExternally
    open fun destroy(): Unit = definedExternally
    open fun addXaxisAnnotation(options: Any, pushToMemory: Boolean? = definedExternally /* null */, context: Any? = definedExternally /* null */): Unit = definedExternally
    open fun addYaxisAnnotation(options: Any, pushToMemory: Boolean? = definedExternally /* null */, context: Any? = definedExternally /* null */): Unit = definedExternally
    open fun addPointAnnotation(options: Any, pushToMemory: Boolean? = definedExternally /* null */, context: Any? = definedExternally /* null */): Unit = definedExternally
    open fun addText(options: Any, pushToMemory: Boolean? = definedExternally /* null */, context: Any? = definedExternally /* null */): Unit = definedExternally
    open fun dataURI(): Unit = definedExternally
    companion object {
        fun exec(chartID: String, fn: () -> Unit, options: Any): Any = definedExternally
        fun initOnLoad(): Unit = definedExternally
    }
    interface `T$2` {
        var x: String
        var y: Number
    }
    interface `T$3` {
        var name: String
        var data: dynamic /* Array<Number> | Array<`T$2`> */
    }
    interface ApexOptions {
        var annotations: ApexAnnotations? get() = definedExternally; set(value) = definedExternally
        var chart: ApexChart? get() = definedExternally; set(value) = definedExternally
        var colors: Array<String>? get() = definedExternally; set(value) = definedExternally
        var dataLabels: ApexDataLabels? get() = definedExternally; set(value) = definedExternally
        var series: dynamic /* Array<`T$3`> | Array<Number> */ get() = definedExternally; set(value) = definedExternally
        var stroke: ApexStroke? get() = definedExternally; set(value) = definedExternally
        var labels: Array<String>? get() = definedExternally; set(value) = definedExternally
        var legend: ApexLegend? get() = definedExternally; set(value) = definedExternally
        var fill: ApexFill? get() = definedExternally; set(value) = definedExternally
        var tooltip: ApexTooltip? get() = definedExternally; set(value) = definedExternally
        var plotOptions: ApexPlotOptions? get() = definedExternally; set(value) = definedExternally
        var responsive: Array<ApexResponsive>? get() = definedExternally; set(value) = definedExternally
        var xaxis: ApexXAxis? get() = definedExternally; set(value) = definedExternally
        var yaxis: dynamic /* ApexYAxis | Array<ApexYAxis> */ get() = definedExternally; set(value) = definedExternally
        var grid: ApexGrid? get() = definedExternally; set(value) = definedExternally
        var states: ApexStates? get() = definedExternally; set(value) = definedExternally
        var title: ApexTitleSubtitle? get() = definedExternally; set(value) = definedExternally
        var subtitle: ApexTitleSubtitle? get() = definedExternally; set(value) = definedExternally
        var theme: ApexTheme? get() = definedExternally; set(value) = definedExternally
    }
}
external interface `T$4` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var top: Number? get() = definedExternally; set(value) = definedExternally
    var left: Number? get() = definedExternally; set(value) = definedExternally
    var blur: Number? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$5` {
    val animationEnd: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val beforeMount: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val mounted: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val updated: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val click: ((e: Any, chart: Any, options: Any) -> Unit)? get() = definedExternally
    val legendClick: ((chart: Any, seriesIndex: Number, options: Any) -> Unit)? get() = definedExternally
    val selection: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val dataPointSelection: ((e: Any, chart: Any, options: Any) -> Unit)? get() = definedExternally
    val dataPointMouseEnter: ((e: Any, chart: Any, options: Any) -> Unit)? get() = definedExternally
    val dataPointMouseLeave: ((e: Any, chart: Any, options: Any) -> Unit)? get() = definedExternally
    val beforeZoom: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val zoomed: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
    val scrolled: ((chart: Any, options: Any) -> Unit)? get() = definedExternally
}
external interface `T$6` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var autoScaleYaxis: Boolean? get() = definedExternally; set(value) = definedExternally
    var target: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$7` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$8` {
    var download: Boolean? get() = definedExternally; set(value) = definedExternally
    var selection: Boolean? get() = definedExternally; set(value) = definedExternally
    var zoom: Boolean? get() = definedExternally; set(value) = definedExternally
    var zoomin: Boolean? get() = definedExternally; set(value) = definedExternally
    var zoomout: Boolean? get() = definedExternally; set(value) = definedExternally
    var pan: Boolean? get() = definedExternally; set(value) = definedExternally
    var reset: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$9` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var tools: `T$8`? get() = definedExternally; set(value) = definedExternally
    var autoSelected: dynamic /* String /* "zoom" */ | String /* "selection" */ | String /* "pan" */ */ get() = definedExternally; set(value) = definedExternally
}
external interface `T$10` {
    var color: String? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$11` {
    var color: String? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$12` {
    var fill: `T$10`? get() = definedExternally; set(value) = definedExternally
    var stroke: `T$11`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$13` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var type: dynamic /* String /* "x" */ | String /* "y" */ | String /* "xy" */ */ get() = definedExternally; set(value) = definedExternally
    var zoomedArea: `T$12`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$14` {
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
    var dashArray: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$15` {
    var min: Number? get() = definedExternally; set(value) = definedExternally
    var max: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$16` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
    var fill: `T$10`? get() = definedExternally; set(value) = definedExternally
    var stroke: `T$14`? get() = definedExternally; set(value) = definedExternally
    var xaxis: `T$15`? get() = definedExternally; set(value) = definedExternally
    var yaxis: `T$15`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$17` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var delay: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$18` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var speed: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$19` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var easing: dynamic /* String /* "linear" */ | String /* "easein" */ | String /* "easeout" */ | String /* "easeinout" */ */ get() = definedExternally; set(value) = definedExternally
    var speed: Number? get() = definedExternally; set(value) = definedExternally
    var animateGradually: `T$17`? get() = definedExternally; set(value) = definedExternally
    var dynamicAnimation: `T$18`? get() = definedExternally; set(value) = definedExternally
}
external interface ApexChart {
    var width: dynamic /* String | Number */ get() = definedExternally; set(value) = definedExternally
    var height: dynamic /* String | Number */ get() = definedExternally; set(value) = definedExternally
    var type: dynamic /* String /* "line" */ | String /* "area" */ | String /* "bar" */ | String /* "histogram" */ | String /* "pie" */ | String /* "donut" */ | String /* "radialBar" */ | String /* "scatter" */ | String /* "bubble" */ | String /* "heatmap" */ | String /* "candlestick" */ | String /* "radar" */ */
    var foreColor: String? get() = definedExternally; set(value) = definedExternally
    var fontFamily: String
    var background: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var dropShadow: `T$4`? get() = definedExternally; set(value) = definedExternally
    var events: `T$5`? get() = definedExternally; set(value) = definedExternally
    var brush: `T$6`? get() = definedExternally; set(value) = definedExternally
    var id: String? get() = definedExternally; set(value) = definedExternally
    var locales: Array<ApexLocale>? get() = definedExternally; set(value) = definedExternally
    var defaultLocale: String? get() = definedExternally; set(value) = definedExternally
    var sparkline: `T$7`? get() = definedExternally; set(value) = definedExternally
    var stacked: Boolean? get() = definedExternally; set(value) = definedExternally
    var stackType: dynamic /* String /* "normal" */ | String /* "100%" */ */ get() = definedExternally; set(value) = definedExternally
    var toolbar: `T$9`? get() = definedExternally; set(value) = definedExternally
    var zoom: `T$13`? get() = definedExternally; set(value) = definedExternally
    var selection: `T$16`? get() = definedExternally; set(value) = definedExternally
    var animations: `T$19`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$20` {
    var type: String? get() = definedExternally; set(value) = definedExternally
    var value: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$21` {
    var filter: `T$20`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$22` {
    var allowMultipleDataPointsSelection: Boolean? get() = definedExternally; set(value) = definedExternally
    var filter: `T$20`? get() = definedExternally; set(value) = definedExternally
}
external interface ApexStates {
    var normal: `T$21`? get() = definedExternally; set(value) = definedExternally
    var hover: `T$21`? get() = definedExternally; set(value) = definedExternally
    var active: `T$22`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$23` {
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
}
external interface ApexTitleSubtitle {
    var text: String? get() = definedExternally; set(value) = definedExternally
    var align: dynamic /* String /* "left" */ | String /* "center" */ | String /* "right" */ */ get() = definedExternally; set(value) = definedExternally
    var margin: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var floating: Number? get() = definedExternally; set(value) = definedExternally
    var style: `T$23`? get() = definedExternally; set(value) = definedExternally
}
external interface ApexStroke {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var curve: dynamic /* String /* "smooth" */ | String /* "straight" */ | String /* "stepline" */ */ get() = definedExternally; set(value) = definedExternally
    var lineCap: dynamic /* String /* "butt" */ | String /* "square" */ | String /* "round" */ */ get() = definedExternally; set(value) = definedExternally
    var colors: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var dashArray: dynamic /* Number | Array<Number> */ get() = definedExternally; set(value) = definedExternally
}
external interface ApexAnnotations {
    var position: String? get() = definedExternally; set(value) = definedExternally
    var yaxis: Array<YAxisAnnotations>? get() = definedExternally; set(value) = definedExternally
    var xaxis: Array<XAxisAnnotations>? get() = definedExternally; set(value) = definedExternally
    var points: Array<PointAnnotations>? get() = definedExternally; set(value) = definedExternally
}
external interface AnnotationLabel {
    var borderColor: String? get() = definedExternally; set(value) = definedExternally
    var borderWidth: Number? get() = definedExternally; set(value) = definedExternally
    var text: String? get() = definedExternally; set(value) = definedExternally
    var textAnchor: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var style: AnnotationStyle? get() = definedExternally; set(value) = definedExternally
    var position: String? get() = definedExternally; set(value) = definedExternally
    var orientation: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$24` {
    var left: Number? get() = definedExternally; set(value) = definedExternally
    var right: Number? get() = definedExternally; set(value) = definedExternally
    var top: Number? get() = definedExternally; set(value) = definedExternally
    var bottom: Number? get() = definedExternally; set(value) = definedExternally
}
external interface AnnotationStyle {
    var background: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var cssClass: String? get() = definedExternally; set(value) = definedExternally
    var padding: `T$24`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$25` {
    var borderColor: String? get() = definedExternally; set(value) = definedExternally
    var borderWidth: Number? get() = definedExternally; set(value) = definedExternally
    var text: String? get() = definedExternally; set(value) = definedExternally
    var textAnchor: String? get() = definedExternally; set(value) = definedExternally
    var position: String? get() = definedExternally; set(value) = definedExternally
    var orientation: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var style: AnnotationStyle? get() = definedExternally; set(value) = definedExternally
}
external interface XAxisAnnotations {
    var x: Number? get() = definedExternally; set(value) = definedExternally
    var strokeDashArray: Number? get() = definedExternally; set(value) = definedExternally
    var borderColor: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var label: `T$25`? get() = definedExternally; set(value) = definedExternally
}
external interface YAxisAnnotations {
    var y: Number? get() = definedExternally; set(value) = definedExternally
    var strokeDashArray: Number? get() = definedExternally; set(value) = definedExternally
    var borderColor: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var yAxisIndex: Number? get() = definedExternally; set(value) = definedExternally
    var label: AnnotationLabel? get() = definedExternally; set(value) = definedExternally
}
external interface `T$26` {
    var size: Number? get() = definedExternally; set(value) = definedExternally
    var fillColor: String? get() = definedExternally; set(value) = definedExternally
    var strokeColor: String? get() = definedExternally; set(value) = definedExternally
    var strokeWidth: Number? get() = definedExternally; set(value) = definedExternally
    var shape: String? get() = definedExternally; set(value) = definedExternally
    var radius: Number? get() = definedExternally; set(value) = definedExternally
}
external interface PointAnnotations {
    var x: Number? get() = definedExternally; set(value) = definedExternally
    var y: Nothing? get() = definedExternally; set(value) = definedExternally
    var yAxisIndex: Number? get() = definedExternally; set(value) = definedExternally
    var seriesIndex: Number? get() = definedExternally; set(value) = definedExternally
    var marker: `T$26`? get() = definedExternally; set(value) = definedExternally
    var label: AnnotationLabel? get() = definedExternally; set(value) = definedExternally
}
external interface `T$27` {
    var download: String? get() = definedExternally; set(value) = definedExternally
    var selection: String? get() = definedExternally; set(value) = definedExternally
    var selectionZoom: String? get() = definedExternally; set(value) = definedExternally
    var zoomIn: String? get() = definedExternally; set(value) = definedExternally
    var zoomOut: String? get() = definedExternally; set(value) = definedExternally
    var pan: String? get() = definedExternally; set(value) = definedExternally
    var reset: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$28` {
    var months: Array<String>? get() = definedExternally; set(value) = definedExternally
    var shortMonths: Array<String>? get() = definedExternally; set(value) = definedExternally
    var days: Array<String>? get() = definedExternally; set(value) = definedExternally
    var shortDays: Array<String>? get() = definedExternally; set(value) = definedExternally
    var toolbar: `T$27`? get() = definedExternally; set(value) = definedExternally
}
external interface ApexLocale {
    var name: String? get() = definedExternally; set(value) = definedExternally
    var options: `T$28`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$29` {
    var from: Number? get() = definedExternally; set(value) = definedExternally
    var to: Number? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$30` {
    var ranges: Array<`T$29`>? get() = definedExternally; set(value) = definedExternally
    var backgroundBarColors: Array<String>? get() = definedExternally; set(value) = definedExternally
    var backgroundBarOpacity: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$31` {
    var position: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$32` {
    var horizontal: Boolean? get() = definedExternally; set(value) = definedExternally
    var endingShape: dynamic /* String /* "flat" */ | String /* "rounded" */ | String /* "arrow" */ */ get() = definedExternally; set(value) = definedExternally
    var columnWidth: String? get() = definedExternally; set(value) = definedExternally
    var barHeight: String? get() = definedExternally; set(value) = definedExternally
    var distributed: Boolean? get() = definedExternally; set(value) = definedExternally
    var colors: `T$30`? get() = definedExternally; set(value) = definedExternally
    var dataLabels: `T$31`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$33` {
    var upward: String? get() = definedExternally; set(value) = definedExternally
    var downward: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$34` {
    var useFillColor: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$35` {
    var colors: `T$33`? get() = definedExternally; set(value) = definedExternally
    var wick: `T$34`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$36` {
    var inverse: Boolean? get() = definedExternally; set(value) = definedExternally
    var ranges: Array<`T$29`>? get() = definedExternally; set(value) = definedExternally
    var min: Number? get() = definedExternally; set(value) = definedExternally
    var max: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$37` {
    var radius: Number? get() = definedExternally; set(value) = definedExternally
    var enableShades: Boolean? get() = definedExternally; set(value) = definedExternally
    var shadeIntensity: Number? get() = definedExternally; set(value) = definedExternally
    var distributed: Boolean? get() = definedExternally; set(value) = definedExternally
    var colorScale: `T$36`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$38` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var fontFamily: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$39` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var fontFamily: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: String) -> String)? get() = definedExternally
}
external interface `T$40` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var label: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    val formatter: ((w: Any) -> String)? get() = definedExternally
}
external interface `T$41` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var name: `T$38`? get() = definedExternally; set(value) = definedExternally
    var value: `T$39`? get() = definedExternally; set(value) = definedExternally
    var total: `T$40`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$42` {
    var size: String? get() = definedExternally; set(value) = definedExternally
    var background: String? get() = definedExternally; set(value) = definedExternally
    var labels: `T$41`
}
external interface `T$43` {
    var offset: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$44` {
    var size: Number? get() = definedExternally; set(value) = definedExternally
    var donut: `T$42`? get() = definedExternally; set(value) = definedExternally
    var customScale: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var dataLabels: `T$43`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$45` {
    var colors: Array<String>? get() = definedExternally; set(value) = definedExternally
}
external interface `T$46` {
    var strokeColor: String? get() = definedExternally; set(value) = definedExternally
    var fill: `T$45`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$47` {
    var size: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var polygons: `T$46`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$48` {
    var margin: Number? get() = definedExternally; set(value) = definedExternally
    var size: String? get() = definedExternally; set(value) = definedExternally
    var background: String? get() = definedExternally; set(value) = definedExternally
    var image: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var height: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var clipped: Boolean? get() = definedExternally; set(value) = definedExternally
    var position: dynamic /* String /* "front" */ | String /* "back" */ */ get() = definedExternally; set(value) = definedExternally
}
external interface `T$49` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var startAngle: Number? get() = definedExternally; set(value) = definedExternally
    var endAngle: Number? get() = definedExternally; set(value) = definedExternally
    var background: String? get() = definedExternally; set(value) = definedExternally
    var strokeWidth: String? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
    var margin: Number? get() = definedExternally; set(value) = definedExternally
    var dropShadow: `T$4`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$50` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$51` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: Number) -> String)? get() = definedExternally
}
external interface `T$52` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var name: `T$50`? get() = definedExternally; set(value) = definedExternally
    var value: `T$51`? get() = definedExternally; set(value) = definedExternally
    var total: `T$40`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$53` {
    var size: Number? get() = definedExternally; set(value) = definedExternally
    var inverseOrder: Boolean? get() = definedExternally; set(value) = definedExternally
    var startAngle: Number? get() = definedExternally; set(value) = definedExternally
    var endAngle: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var hollow: `T$48`? get() = definedExternally; set(value) = definedExternally
    var track: `T$49`? get() = definedExternally; set(value) = definedExternally
    var dataLabels: `T$52`? get() = definedExternally; set(value) = definedExternally
}
external interface ApexPlotOptions {
    var bar: `T$32`? get() = definedExternally; set(value) = definedExternally
    var candlestick: `T$35`? get() = definedExternally; set(value) = definedExternally
    var heatmap: `T$37`? get() = definedExternally; set(value) = definedExternally
    var pie: `T$44`? get() = definedExternally; set(value) = definedExternally
    var radar: `T$47`? get() = definedExternally; set(value) = definedExternally
    var radialBar: `T$53`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$54` {
    var shade: String? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
    var shadeIntensity: Number? get() = definedExternally; set(value) = definedExternally
    var gradientToColors: Array<String>? get() = definedExternally; set(value) = definedExternally
    var inverseColors: Boolean? get() = definedExternally; set(value) = definedExternally
    var opacityFrom: Number? get() = definedExternally; set(value) = definedExternally
    var opacityTo: Number? get() = definedExternally; set(value) = definedExternally
    var stops: Array<Number>? get() = definedExternally; set(value) = definedExternally
}
external interface `T$55` {
    var src: Array<String>? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var height: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$56` {
    var style: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var height: Number? get() = definedExternally; set(value) = definedExternally
    var strokeWidth: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexFill {
    var colors: Array<String>? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
    var gradient: `T$54`? get() = definedExternally; set(value) = definedExternally
    var image: `T$55`? get() = definedExternally; set(value) = definedExternally
    var pattern: `T$56`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$57` {
    var color: String? get() = definedExternally; set(value) = definedExternally
    var useSeriesColors: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$58` {
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var height: Number? get() = definedExternally; set(value) = definedExternally
    var strokeColor: String? get() = definedExternally; set(value) = definedExternally
    var strokeWidth: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var radius: Number? get() = definedExternally; set(value) = definedExternally
    var shape: dynamic /* String /* "square" */ | String /* "circle" */ */ get() = definedExternally; set(value) = definedExternally
    val customHTML: (() -> String)? get() = definedExternally
    val onClick: (() -> Unit)? get() = definedExternally
}
external interface `T$59` {
    var horizontal: Number? get() = definedExternally; set(value) = definedExternally
    var vertical: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$60` {
    var left: Number? get() = definedExternally; set(value) = definedExternally
    var top: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$61` {
    var toggleDataSeries: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$62` {
    var highlightDataSeries: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface ApexLegend {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var showForSingleSeries: Boolean? get() = definedExternally; set(value) = definedExternally
    var showForNullSeries: Boolean? get() = definedExternally; set(value) = definedExternally
    var showForZeroSeries: Boolean? get() = definedExternally; set(value) = definedExternally
    var floating: Boolean? get() = definedExternally; set(value) = definedExternally
    var position: dynamic /* String /* "left" */ | String /* "right" */ | String /* "top" */ | String /* "bottom" */ */ get() = definedExternally; set(value) = definedExternally
    var horizontalAlign: dynamic /* String /* "left" */ | String /* "center" */ | String /* "right" */ */ get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var fontFamily: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var height: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: String, opts: Any) -> String)? get() = definedExternally
    var textAnchor: String? get() = definedExternally; set(value) = definedExternally
    var labels: `T$57`? get() = definedExternally; set(value) = definedExternally
    var markers: `T$58`? get() = definedExternally; set(value) = definedExternally
    var itemMargin: `T$59`? get() = definedExternally; set(value) = definedExternally
    var containerMargin: `T$60`? get() = definedExternally; set(value) = definedExternally
    var onItemClick: `T$61`? get() = definedExternally; set(value) = definedExternally
    var onItemHover: `T$62`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$63` {
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var fontFamily: String? get() = definedExternally; set(value) = definedExternally
    var colors: Array<String>? get() = definedExternally; set(value) = definedExternally
}
external interface `T$64` {
    var enabled: Boolean
    var top: Number? get() = definedExternally; set(value) = definedExternally
    var left: Number? get() = definedExternally; set(value) = definedExternally
    var blur: Number? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexDataLabels {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: Number, opts: Any) -> String)? get() = definedExternally
    var textAnchor: dynamic /* String /* "start" */ | String /* "middle" */ | String /* "end" */ */ get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var style: `T$63`? get() = definedExternally; set(value) = definedExternally
    var dropShadow: `T$64`? get() = definedExternally; set(value) = definedExternally
}
external interface ApexResponsive {
    var breakpoint: Number? get() = definedExternally; set(value) = definedExternally
    var options: Any? get() = definedExternally; set(value) = definedExternally
}
external interface `T$65` {
    var highlightDAtaSeries: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$66` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var format: String? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: Number) -> String)? get() = definedExternally
}
external interface `T$67` {
    val formatter: ((seriesName: String) -> String)? get() = definedExternally
}
external interface `T$68` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: Number) -> String)? get() = definedExternally
    var title: `T$67`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$69` {
    val formatter: ((`val`: Number) -> String)? get() = definedExternally
    var title: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$70` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface `T$71` {
    var display: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$72` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var position: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexTooltip {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var shared: Boolean? get() = definedExternally; set(value) = definedExternally
    var followCursor: Boolean? get() = definedExternally; set(value) = definedExternally
    var intersect: Boolean? get() = definedExternally; set(value) = definedExternally
    var inverseOrder: Boolean? get() = definedExternally; set(value) = definedExternally
    val custom: ((options: Any) -> Unit)? get() = definedExternally
    var theme: String? get() = definedExternally; set(value) = definedExternally
    var fillSeriesColor: Boolean? get() = definedExternally; set(value) = definedExternally
    var onDatasetHover: `T$65`? get() = definedExternally; set(value) = definedExternally
    var x: `T$66`? get() = definedExternally; set(value) = definedExternally
    var y: `T$68`? get() = definedExternally; set(value) = definedExternally
    var z: `T$69`? get() = definedExternally; set(value) = definedExternally
    var marker: `T$70`? get() = definedExternally; set(value) = definedExternally
    var items: `T$71`? get() = definedExternally; set(value) = definedExternally
    var fixed: `T$72`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$73` {
    var colors: Array<String>? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var fontFamily: String? get() = definedExternally; set(value) = definedExternally
    var cssClass: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$74` {
    var year: String? get() = definedExternally; set(value) = definedExternally
    var month: String? get() = definedExternally; set(value) = definedExternally
    var day: String? get() = definedExternally; set(value) = definedExternally
    var hour: String? get() = definedExternally; set(value) = definedExternally
    var minute: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$75` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var rotate: Number? get() = definedExternally; set(value) = definedExternally
    var rotateAlways: Boolean? get() = definedExternally; set(value) = definedExternally
    var hideOverlappingLabels: Boolean? get() = definedExternally; set(value) = definedExternally
    var showDuplicates: Boolean? get() = definedExternally; set(value) = definedExternally
    var trim: Boolean? get() = definedExternally; set(value) = definedExternally
    var minHeight: Number? get() = definedExternally; set(value) = definedExternally
    var maxHeight: Number? get() = definedExternally; set(value) = definedExternally
    var style: `T$73`? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var format: String? get() = definedExternally; set(value) = definedExternally
    val formatter: ((value: String, timestamp: Number) -> String)? get() = definedExternally
    var datetimeFormatter: `T$74`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$76` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var strokeWidth: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$77` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var borderType: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var height: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$78` {
    var color: String? get() = definedExternally; set(value) = definedExternally
    var fontSize: String? get() = definedExternally; set(value) = definedExternally
    var cssClass: String? get() = definedExternally; set(value) = definedExternally
}
external interface `T$79` {
    var text: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var style: `T$78`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$80` {
    var color: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var dashArray: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$81` {
    var colorFrom: String? get() = definedExternally; set(value) = definedExternally
    var colorTo: String? get() = definedExternally; set(value) = definedExternally
    var stops: Array<Number>? get() = definedExternally; set(value) = definedExternally
    var opacityFrom: Number? get() = definedExternally; set(value) = definedExternally
    var opacityTo: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$82` {
    var type: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var gradient: `T$81`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$83` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var width: dynamic /* String | Number */ get() = definedExternally; set(value) = definedExternally
    var position: String? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
    var stroke: `T$80`? get() = definedExternally; set(value) = definedExternally
    var fill: `T$82`? get() = definedExternally; set(value) = definedExternally
    var dropShadow: `T$4`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$84` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexXAxis {
    var type: dynamic /* String /* "categories" */ | String /* "datetime" */ | String /* "numeric" */ */ get() = definedExternally; set(value) = definedExternally
    var categories: dynamic /* Array<Number> | Array<String> */ get() = definedExternally; set(value) = definedExternally
    var labels: `T$75`? get() = definedExternally; set(value) = definedExternally
    var axisBorder: `T$76`? get() = definedExternally; set(value) = definedExternally
    var axisTicks: `T$77`? get() = definedExternally; set(value) = definedExternally
    var tickAmount: Number? get() = definedExternally; set(value) = definedExternally
    var min: Number? get() = definedExternally; set(value) = definedExternally
    var max: Number? get() = definedExternally; set(value) = definedExternally
    var range: Number? get() = definedExternally; set(value) = definedExternally
    var floating: Boolean? get() = definedExternally; set(value) = definedExternally
    var position: String? get() = definedExternally; set(value) = definedExternally
    var title: `T$79`? get() = definedExternally; set(value) = definedExternally
    var crosshairs: `T$83`? get() = definedExternally; set(value) = definedExternally
    var tooltip: `T$84`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$85` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var maxWidth: Number? get() = definedExternally; set(value) = definedExternally
    var style: `T$78`? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    val formatter: ((`val`: Number) -> String)? get() = definedExternally
}
external interface `T$86` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$87` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var borderType: String? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$88` {
    var text: String? get() = definedExternally; set(value) = definedExternally
    var rotate: Number? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
    var style: `T$78`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$89` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var position: String? get() = definedExternally; set(value) = definedExternally
    var stroke: `T$80`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$90` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexYAxis {
    var seriesName: String? get() = definedExternally; set(value) = definedExternally
    var opposite: Boolean? get() = definedExternally; set(value) = definedExternally
    var tickAmount: Number? get() = definedExternally; set(value) = definedExternally
    var min: Number? get() = definedExternally; set(value) = definedExternally
    var max: Number? get() = definedExternally; set(value) = definedExternally
    var floating: Boolean? get() = definedExternally; set(value) = definedExternally
    var decimalsInFloat: Number? get() = definedExternally; set(value) = definedExternally
    var labels: `T$85`? get() = definedExternally; set(value) = definedExternally
    var axisBorder: `T$86`? get() = definedExternally; set(value) = definedExternally
    var axisTicks: `T$87`? get() = definedExternally; set(value) = definedExternally
    var title: `T$88`? get() = definedExternally; set(value) = definedExternally
    var crosshairs: `T$89`? get() = definedExternally; set(value) = definedExternally
    var tooltip: `T$90`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$91` {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var offsetX: Number? get() = definedExternally; set(value) = definedExternally
    var offsetY: Number? get() = definedExternally; set(value) = definedExternally
}
external interface `T$92` {
    var lines: `T$91`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$93` {
    var colors: Array<String>? get() = definedExternally; set(value) = definedExternally
    var opacity: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexGrid {
    var show: Boolean? get() = definedExternally; set(value) = definedExternally
    var borderColor: String? get() = definedExternally; set(value) = definedExternally
    var strokeDashArray: Number? get() = definedExternally; set(value) = definedExternally
    var position: dynamic /* String /* "front" */ | String /* "back" */ */ get() = definedExternally; set(value) = definedExternally
    var xaxis: `T$92`? get() = definedExternally; set(value) = definedExternally
    var yaxis: `T$92`? get() = definedExternally; set(value) = definedExternally
    var row: `T$93`? get() = definedExternally; set(value) = definedExternally
    var column: `T$93`? get() = definedExternally; set(value) = definedExternally
    var padding: `T$24`? get() = definedExternally; set(value) = definedExternally
}
external interface `T$94` {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var color: String? get() = definedExternally; set(value) = definedExternally
    var shadeTo: dynamic /* String /* "light" */ | String /* "dark" */ */ get() = definedExternally; set(value) = definedExternally
    var shadeIntensity: Number? get() = definedExternally; set(value) = definedExternally
}
external interface ApexTheme {
    var palette: String? get() = definedExternally; set(value) = definedExternally
    var monochrome: `T$94`? get() = definedExternally; set(value) = definedExternally
}
