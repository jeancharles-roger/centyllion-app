package com.centyllion.client.controller.utils

fun <T> Array<T>.push(e: T): Int = asDynamic().push(e) as Int
fun <T> Array<T>.pop(): T = asDynamic().pop() as T
