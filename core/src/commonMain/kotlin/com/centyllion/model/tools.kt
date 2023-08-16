package com.centyllion.model


enum class EditTool(val icon: String) {
    Move("arrows-alt"), Pen("pen"),
    Line("pencil-ruler"), Spray("spray-can"),
    Eraser("eraser");

    fun actualSize(size: ToolSize): Int = when (this) {
        Spray -> when (size) {
            ToolSize.Fine -> 10
            else -> size.size * 4
        }
        else -> size.size
    }
}

@Suppress("unused")
enum class ToolSize(val size: Int) {
    Fine(1), Small(5), Medium(10), Large(20)
}
