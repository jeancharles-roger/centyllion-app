@file:Suppress("UNCHECKED_CAST")
@file:UseExperimental(UnstableDefault::class)
package com.centyllion.backend

import com.centyllion.model.Asset
import com.centyllion.model.CollectionInfo
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Info
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.User
import com.centyllion.model.UserOptions
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.ContentConverter
import io.ktor.features.suitableCharset
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.request.contentCharset
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Companion.stringify
import kotlinx.serialization.serializer

class JsonConverter : ContentConverter {
    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any
    ) = TextContent(convertForSend(value), contentType.withCharset(context.call.suitableCharset()))

    private fun convertForSend(value: Any?): String = when (value) {
        is Info -> stringify(Info.serializer(), value)
        is User -> stringify(User.serializer(), value)
        is Asset -> stringify(Asset.serializer(), value)
        is GrainModel -> stringify(GrainModel.serializer(), value)
        is GrainModelDescription -> stringify(GrainModelDescription.serializer(), value)
        is Simulation -> stringify(Simulation.serializer(), value)
        is SimulationDescription -> stringify(SimulationDescription.serializer(), value)
        is FeaturedDescription -> stringify(FeaturedDescription.serializer(), value)
        is CollectionInfo -> stringify(CollectionInfo.serializer(), value)
        is ResultPage<*> ->
            when (value.content.firstOrNull()) {
                is SimulationDescription -> stringify(
                    ResultPage.serializer(SimulationDescription.serializer()),
                    value as ResultPage<SimulationDescription>
                )
                is GrainModelDescription -> stringify(
                    ResultPage.serializer(GrainModelDescription.serializer()),
                    value as ResultPage<GrainModelDescription>
                )
                is FeaturedDescription -> stringify(
                    ResultPage.serializer(FeaturedDescription.serializer()),
                    value as ResultPage<FeaturedDescription>
                )
                is User -> stringify(
                    ResultPage.serializer(User.serializer()),
                    value as ResultPage<User>
                )
                is Asset -> stringify(
                    ResultPage.serializer(Asset.serializer()),
                    value as ResultPage<Asset>
                )
                is String -> stringify(
                    ResultPage.serializer(String.serializer()),
                    value as ResultPage<String>
                )
                else -> // the page is empty send it using any serializer
                    stringify(
                        ResultPage.serializer(String.serializer()),
                        value as ResultPage<String>
                    )

            }
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
            UserOptions::class -> Json.parse(UserOptions.serializer(), text)
            else -> throw Exception("Can't transform ${request.type.simpleName} from Json")
        }
    }
}
