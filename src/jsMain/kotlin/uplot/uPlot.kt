package uplot

import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.json

class Size(val width: Int, val height: Int)

internal fun Json.add(name: String, value: Any?) = value?.let { this[name] = it}

class Scale(
    val time: Boolean = true
) {
    val json = json("time" to time)
}

class Series(
    val label: String,
    val time: Boolean = true,
    val scale: String? = null,
    val stroke: String? = null,
    val width: Number? = null,
    val dash: List<Int>? = null,
    val fill: String? = null,
    // spanGaps
    val show: Boolean = true,
    val value: ((UPlot, Number) -> String)? = null
) {
    val json = json(
        "label" to label,  "time" to time, "show" to show
    ).apply {
        add("scale", scale)
        add("stroke", stroke)
        add("width", width)
        add("dash", dash)
        add("fill", fill)
        add("value", value)
    }
}

class UPlotOptions(
    val width: Int,
    val height: Int,
    val title: String? = null,
    val id: String? = null,
    val klass: String? = null,
    val cursor: Boolean? = null,
    val legend: Boolean? = null,
    val scales: Map<String, Scale>? = null,
    val series: List<Series>? = null
) {
    val json = json(
        "width" to width, "height" to height
    ).apply {
        add("title", title)
        add("id", id)
        add("class", klass)
        add("cursor", cursor)
        add("legend", legend)
        add("scales", scales?.let { scales ->
            val result = json()
            scales.forEach { result.add(it.key, it.value.json) }
            result
        })
        add("series", series?.map(Series::json)?.toTypedArray())
    }
}

fun createUPlot(options: UPlotOptions, data: Array<Array<Number>>) = UPlot(options.json, data)

@JsModule("uPlot")
external class UPlot {

    constructor(options: Json, data: Array<Array<Number>>, parent: HTMLElement = definedExternally)

    /** [whenReady] must call init. */
    constructor(options: Json, data: Array<Array<Number>>, whenReady: (UPlot, init: ()-> Unit) -> Unit)

    val root: HTMLElement

    fun setData(data: Array<Array<out Number>>, autoScale: Boolean = definedExternally)

    fun setSeries(index: Int, options: Json)

    fun setSize(size: Size)

    val series : Array<Series>
}


