package uplot

import org.w3c.dom.HTMLElement
import kotlin.js.Json
import kotlin.js.json

interface Size {
    val width: Int
    val height: Int
}

internal fun Json.add(name: String, value: Any?) = value?.let { this[name] = it}

class Series(
    val label: String,
    val time: Boolean = true,
    val scale: String? = null,
    val width: Number? = null,
    val show: Boolean = true
) {
    val json = json(
        "label" to label, "time" to time, "show" to show
    ).apply {
        add("scale", scale)
        add("width", width)
    }
}

class UPlotOptions(
    val title: String,
    val width: Int,
    val height: Int,
    val id: String? = null,
    val klass: String? = null,
    val series: List<Series>
) {
    val json = json(
        "title" to title, "width" to width, "height" to height
    ).apply {
        add("id", id)
        add("class", klass)
        this["series"] = series.map(Series::json)
    }
}

fun createUPlot(options: UPlotOptions, data: Array<Array<Number>>, parent: HTMLElement) =
    UPlot(options.json, data, parent)

/*
let opts = {
  title: "My Chart",
  id: "chart1",
  class: "my-chart",
  width: 800,
  height: 600,
  series: [
    {},
    {
      // initial toggled state (optional)
      show: true,

      spanGaps: false,

      // in-legend display
      label: "RAM",
      value: (self, rawValue) => "$" + rawValue.toFixed(2),

      // series style
      stroke: "red",
      width: 1,
      fill: "rgba(255, 0, 0, 0.3)",
      dash: [10, 5],
    }
  ],
};
let uplot = new uPlot.Line(opts, data, document.body);
 */

@JsModule("uPlot")
external class UPlot {

    constructor(options: Json, data: Array<Array<Number>>, parent: HTMLElement = definedExternally)

    /** [whenReady] must call init. */
    constructor(options: Json, data: Array<Array<Number>>, whenReady: (UPlot, init: ()-> Unit) -> Unit)

    val root: HTMLElement

    fun setData(data: Array<Array<out Number>>, autoScale: Boolean = definedExternally)

    // u.setSeries(i, {show: !u.series.y[i-1].show})
    fun setSeries(index: Int, options: Json)

    fun setSize(size: Size)

    val series : Array<Series>
}


