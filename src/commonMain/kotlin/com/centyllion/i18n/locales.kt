package com.centyllion.i18n

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Locales(
    val default: String = "en-US",
    val locales: List<String> = emptyList()
) {
    /** Searches [locale] in available one the or the closest */
    fun resolve(locale: String): String {
        if (locales.contains(locale)) return locale
        val prefix = locale.indexOf("-").let { if (it < 0) locale else locale.substring(0, it) }
        return locales.firstOrNull { it.startsWith(prefix) } ?: default
    }
}

@Serializable
class Locale(
    val name: String,
    private val translations: Map<String, String>
) {
    @Transient
    val parameterRegex = Regex("%([0-9]+)")

    fun i18n(key: String, vararg parameters: Any): String {
        val result = translations[key]
        if (result == null) println("Translation missing for '$key' in $name")
        return parameterRegex.replace(result ?: key) {
            it.groups[1]?.value?.toIntOrNull()?.let { parameters[it].toString() } ?: "%$it"
        }
    }

}
