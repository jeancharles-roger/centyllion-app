package com.centyllion.i18n

import kotlinx.serialization.Serializable

@Serializable
data class Locales(
    val default: String = "en_US",
    val locales: List<String> = emptyList()
) {
    /** Searches [locale] in available one the or the closest */
    fun resolve(locale: String): String {
        if (locales.contains(locale)) return locale
        val prefix = locale.substring(0, locale.indexOf("-") + 1)
        return locales.firstOrNull { it.startsWith(prefix) } ?: default
    }
}

@Serializable
class Locale(
    val name: String,
    private val translations: Map<String, String>
) {

    fun localize(key: String, vararg parameters: String) = translations[key] ?: key

}
