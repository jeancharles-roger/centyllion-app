package com.centyllion.client.controller

import kotlinx.html.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.browser.document

data class ColumnSize(
    val desktop: Int, val tablet: Int = desktop, val mobile: Int? = null, val centered: Boolean = false
) {
    val classes = "column is-$desktop-desktop is-$tablet-tablet " +
            if (mobile != null) "is-$mobile-mobile" else "" +
                    if (centered) "is-centered" else ""
}

fun size(desktop: Int, tablet: Int = desktop, mobile: Int? = null, centered: Boolean = false) =
    ColumnSize(desktop, tablet, mobile, centered)

fun column(content: HTMLElement, size: ColumnSize = size(4)): HTMLDivElement {
    val result = document.createElement("div") as HTMLDivElement
    result.className = "column ${size.classes}"
    result.appendChild(content)
    return result
}

@HtmlTagMarker
fun <T, C : TagConsumer<T>> C.columns(classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "columns $classes"), this).visitAndFinalize(this, block)

@HtmlTagMarker
fun FlowContent.columns(classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "columns $classes"), consumer).visit(block)

@HtmlTagMarker
fun FlowContent.column(size: ColumnSize = ColumnSize(4), classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "${size.classes} $classes"), consumer).visit(block)

@HtmlTagMarker
fun <T, C : TagConsumer<T>> C.column(size: ColumnSize = ColumnSize(4), classes: String = "", block: DIV.() -> Unit = {}) =
    DIV(attributesMapOf("class", "columns ${size.classes} $classes"), this).visitAndFinalize(this, block)


fun <T> Array<T>.push(e: T): Int = asDynamic().push(e) as Int
fun <T> Array<T>.pop(): T = asDynamic().pop() as T
