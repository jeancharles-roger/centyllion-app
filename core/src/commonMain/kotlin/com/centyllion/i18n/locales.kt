package com.centyllion.i18n

object Locales {

    val default: Locale = English
    val locales: List<Locale> = listOf(English, French)

    fun resolve(locale: String) =
        locales.find { it.name == locale } ?: default
}


private val parameterRegex = Regex("%([0-9]+)")

interface Locale {
    val name: String
    val label: String

    fun value(key: String): String?

    fun i18n(key: String, vararg parameters: Any): String {
        val result = value(key)
        if (key.isNotBlank() && result == null) println("Translation missing for '$key' in $name")
        return parameterRegex.replace(result ?: key) {
            it.groups[1]?.value?.toIntOrNull()?.let { parameters.getOrNull(it)?.toString() } ?: "%$it"
        }
    }
}
