@file:JsModule("uPlot")
package uplot

import org.w3c.dom.HTMLElement
import kotlin.js.Json

external interface Serie {
    val label: String
    val time: Boolean
    val scale: String
    val width: Number
    val show: Boolean
}

external interface Series {
    val x: Serie
    val y: Array<Serie>
}

external class Line(options: Json, data: Array<Array<Number>>) {

    val root: HTMLElement

    fun setData(data: Array<Array<out Number>>, minView: Number = definedExternally, maxView: Number = definedExternally)

    fun toggle(idxs: Array<Int>, onOff: Boolean = definedExternally, pub: Boolean = definedExternally)

    val series : Series
}


