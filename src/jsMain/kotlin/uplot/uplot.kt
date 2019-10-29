package uplot

import org.w3c.dom.HTMLElement
import kotlin.js.Json

class ScalesOptions(
    var x: ScaleOptions = ScaleOptions(),
    var y: ScaleOptions = ScaleOptions()
)

class ScaleOptions(
    /** Can be 't' ot 'n' */
    var type: String = "n",
    var auto: Boolean? = null,
    var range: (min: Number, max: Number) -> Array<Number> = { min, max -> arrayOf(min, max) }
)

class SeriesOptions(
    val x: SerieOptions = SerieOptions("x"),
    var y: Array<SerieOptions> = emptyArray()
)

class SerieOptions(
    var scale: String,
    var label: String? = null,
    var value: ((Number) -> String)? = null,
    var color: String? = null,
    var width: Number? = null
)

class AxesOption(
    val x: AxeOption = AxeOption("x"),
    var y: Array<AxeOption>? = emptyArray()
)

class AxeOption(
    var scale: String,
    var type: String = "n",
    var width: Number? = null,
    var label: String? = null,
    var color: String? = null
)

class UPlotOptions(
    var width: Int,
    var height: Int,
    var cursor: Boolean? = null,
    var scales: Json? = null,
    var series: SeriesOptions = SeriesOptions(),
    var axes: AxesOption = AxesOption()
)

@JsName("uPlot")
external class `uPlot`(options: UPlotOptions, data: Array<Array<Number>>) {

    val data: Array<Array<Number>>

    val root: HTMLElement

    fun getView(): Array<Number>

    fun setData(data: Array<Array<Number>>, minView: Number = definedExternally, maxView: Number = definedExternally)
}
