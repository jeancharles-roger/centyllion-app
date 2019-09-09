package markdownit

class MarkdownItOptions(
    /** false, Set true to enable HTML tags in source. Be careful! That's not safe! You may need external sanitizer to protect output from XSS. It's better to extend features via plugins, instead of enabling HTML. */
    val html: Boolean? = null,
    /** false, Set true to add '/' when closing single tags (<br />). This is needed only for full CommonMark compatibility. In real world you will need HTML output. */
    val xhtmlOut: Boolean? = null,
    /** false, Set true to convert \n in paragraphs into <br>. */
    val breaks: Boolean? = null,
    /**  language-. CSS language class prefix for fenced blocks. Can be useful for external highlighters. */
    val langPrefix: String? = null,
    /** - false. Set true to autoconvert URL-like text to links. */
    val linkify: Boolean? = null,
    /**  - false. Set true to enable some language-neutral replacement + quotes beautification (smartquotes). */
    val typographer: Boolean? = null,
    /** - “”‘’, String or Array. Double + single quotes replacement pairs, when typographer enabled and smartquotes on. For example, you can use '«»„“' for Russian, '„“‚‘' for German, and ['«\xA0', '\xA0»', '‹\xA0', '\xA0›'] for French (including nbsp). */
    val quotes: String? = null,
    /** null. Highlighter function for fenced code blocks. Highlighter function (str, lang) should return escaped HTML. It can also return empty string if the source was not changed and should be escaped externaly. If result starts with <pre... internal wrapper is skipped. */
    val highlight: ((string: String, lang: String) -> String)? = null
)

external interface MarkdownIt {

    fun enable(option: String, ignoreInvalid: Boolean = definedExternally)
    fun enable(options: Array<String>, ignoreInvalid: Boolean = definedExternally)
    fun disable(option: String, ignoreInvalid: Boolean = definedExternally)
    fun disable(options: Array<String>, ignoreInvalid: Boolean = definedExternally)

    /**
     * Render markdown string into html. It does all magic for you :).
     * env can be used to inject additional metadata ({} by default). But you will not need it with high probability. See also comment in MarkdownIt.parse.
     */
    fun render(src: String, env: Any? = definedExternally): String

    /**
     * Similar to [render] but for single paragraph content. Result will NOT be wrapped into <p> tags.
     */
    fun renderInline(src: String, env: Any? = definedExternally): String
}
