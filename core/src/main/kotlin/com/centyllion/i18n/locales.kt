package com.centyllion.i18n

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

fun loadLocales(): Locales {
    val stream = Locales::class.java.getResourceAsStream("locales.json")
    val source = stream.readBytes().toString(Charsets.UTF_8)
    return Json.decodeFromString(Locales.serializer(), source)
}

@Serializable
data class Locales(
    val default: String = "en-US",
    val locales: List<String> = emptyList(),
    val labels: List<String> = emptyList(),
) {
    val system: String get() = System.getProperty("user.language")

    /** Searches [locale] in available one the or the closest */
    fun resolve(locale: String): String {
        if (locales.contains(locale)) return locale
        val prefix = locale.indexOf("-").let { if (it < 0) locale else locale.substring(0, it) }
        return locales.firstOrNull { it.startsWith(prefix) } ?: default
    }

    fun locale(locale: String): Locale {
        val id = resolve(locale)
        val stream = Locales::class.java.getResourceAsStream("$id.json")
        val source = stream.readBytes().toString(Charsets.UTF_8)
        return Json.decodeFromString(LoadedLocale.serializer(), source)
    }
}

interface Locale {
    val name: String
    fun i18n(key: String, vararg parameters: Any): String
}

@Serializable
class LoadedLocale(
    override val name: String,
    val label: String,
    private val translations: Map<String, String>
): Locale {
    // TODO create a log to clear unused logs

    @Transient
    val parameterRegex = Regex("%([0-9]+)")

    override fun i18n(key: String, vararg parameters: Any): String {
        val result = translations[key]
        if (result == null) println("Translation missing for '$key' in $name")
        return parameterRegex.replace(result ?: key) {
            it.groups[1]?.value?.toIntOrNull()?.let { parameters.getOrNull(it)?.toString() } ?: "%$it"
        }
    }
}

val emptyLocale = object : Locale {
    override val name: String = "empty"
    override fun i18n(key: String, vararg parameters: Any): String = key
}