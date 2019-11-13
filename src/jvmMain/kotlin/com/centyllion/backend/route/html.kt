package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.index
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.withCharset
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.bufferedWriter
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.html.HTML
import kotlinx.html.stream.appendHTML
import kotlinx.html.visitAndFinalize

/**
 * Represents an [OutgoingContent] using `kotlinx.html` builder.
 */
class HtmlContent(
    override val status: HttpStatusCode? = null, private val builder: HTML.() -> Unit
) : OutgoingContent.WriteChannelContent() {

    override val contentType: ContentType
        get() = ContentType.Text.Html.withCharset(Charsets.UTF_8)

    @KtorExperimentalAPI
    override suspend fun writeTo(channel: ByteWriteChannel) {
        channel.bufferedWriter().use {
            it.append("<!DOCTYPE html>\n")
            val consumer = it.appendHTML()
            val html = HTML(mapOf("prefix" to "og:http://ogp.me/ns#"), consumer)
            html.visitAndFinalize(consumer, builder)
        }
    }
}


/**
 * Responds to a client with a HTML response, using specified [block] to build an HTML page
 */
suspend fun ApplicationCall.respondHtml(status: HttpStatusCode = HttpStatusCode.OK, block: HTML.() -> Unit) {
    respond(HtmlContent(status, block))
}

fun Route.html(data: Data) {
    get("/show") {
        val path = call.request.uri
        val modelId = call.parameters["model"]
        val simulationId = call.parameters["simulation"]
        when {
            simulationId != null -> {
                val simulation = data.getSimulation(simulationId)
                val name = simulation?.name ?: ""
                val description = simulation?.simulation?.description ?: ""
                val image = "https://app.centyllion.com/api/simulation/${simulationId}/thumbnail"
                context.respondHtml { index("Show $name", description, path, image, true) }
            }
            modelId != null -> {
                val model = data.getGrainModel(modelId)
                val name = model?.name ?: ""
                val description = model?.model?.description ?: ""
                context.respondHtml { index("Show $name", description, path) }
            }
            else -> context.respondHtml { index(path = path) }
        }
    }

    get("/{page?}") { context.respondHtml { index() } }
}