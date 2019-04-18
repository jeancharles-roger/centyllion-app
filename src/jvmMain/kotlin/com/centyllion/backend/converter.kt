package com.centyllion.backend

import com.centyllion.model.*
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.ContentConverter
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.readRemaining
import kotlinx.io.core.readText
import kotlinx.serialization.json.Json

@KtorExperimentalAPI
class JsonConverter : ContentConverter {
    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any
    ) = TextContent(convertForSend(value), contentType.withCharset(context.call.suitableCharset()))

    private fun convertForSend(value: Any?): String = when (value) {
        is User -> Json.stringify(User.serializer(), value)
        is GrainModel -> Json.stringify(GrainModel.serializer(), value)
        is GrainModelDescription -> Json.stringify(GrainModelDescription.serializer(), value)
        is Simulation -> Json.stringify(Simulation.serializer(), value)
        is SimulationDescription -> Json.stringify(SimulationDescription.serializer(), value)
        is FeaturedDescription -> Json.stringify(FeaturedDescription.serializer(), value)
        is Event -> Json.stringify(Event.serializer(), value)
        is List<*> -> "[${if (value.isNotEmpty()) value.joinToString(",") { convertForSend(it) } else ""}]"
        else -> throw Exception("Can't transform ${value?.javaClass?.simpleName} to Json")
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        val request = context.subject
        val channel = request.value as? ByteReadChannel ?: return null
        val charset = context.call.request.contentCharset() ?: Charsets.UTF_8
        val text = channel.readRemaining().readText(charset)

        return when (request.type) {
            User::class -> Json.parse(User.serializer(), text)
            GrainModel::class -> Json.parse(GrainModel.serializer(), text)
            GrainModelDescription::class -> Json.parse(GrainModelDescription.serializer(), text)
            Simulation::class -> Json.parse(Simulation.serializer(), text)
            SimulationDescription::class -> Json.parse(SimulationDescription.serializer(), text)
            FeaturedDescription::class -> Json.parse(FeaturedDescription.serializer(), text)
            else -> throw Exception("Can't transform ${request.type.simpleName} from Json")
        }
    }
}
