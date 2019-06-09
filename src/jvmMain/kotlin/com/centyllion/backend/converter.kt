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
import kotlinx.serialization.json.Json.Companion.stringify
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@KtorExperimentalAPI
class JsonConverter : ContentConverter {
    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>, contentType: ContentType, value: Any
    ) = TextContent(convertForSend(value), contentType.withCharset(context.call.suitableCharset()))

    private fun convertForSend(value: Any?): String = when (value) {
        is User -> stringify(User.serializer(), value)
        is GrainModel -> stringify(GrainModel.serializer(), value)
        is GrainModelDescription -> stringify(GrainModelDescription.serializer(), value)
        is Simulation -> stringify(Simulation.serializer(), value)
        is SimulationDescription -> stringify(SimulationDescription.serializer(), value)
        is FeaturedDescription -> stringify(FeaturedDescription.serializer(), value)
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
                else -> stringify(
                    ResultPage.serializer(SimulationDescription.serializer()),
                    value as ResultPage<SimulationDescription>
                )
            }
        is Event -> stringify(Event.serializer(), value)
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

val fontAwesome = Font.createFont(Font.TRUETYPE_FONT, File("webroot/font/fa-solid-900.ttf"))

fun createThumbnail(model: GrainModel, simulation: Simulation): ByteArray {
    val canvasWidth = simulation.width * 5
    val canvasHeight = simulation.height * 5

    val buffer = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_BYTE_INDEXED)
    val context = buffer.createGraphics()

    val xStep = canvasWidth / simulation.width
    val yStep = canvasHeight / simulation.height
    val xMax = simulation.width * xStep

    val colors = model.indexedGrains.map {
        val triple = colorNames[it.value.color] ?: Triple(255, 0, 0)
        it.value to Color(triple.first, triple.second, triple.third)
    }.toMap()

    context.font = fontAwesome.deriveFont(xStep.toFloat())

    context.color = Color.WHITE
    context.fillRect(0, 0, canvasWidth, canvasHeight)
    var currentX = 0
    var currentY = 0
    simulation.agents.forEach {
        val grain = model.indexedGrains[it]

        if (grain != null) {
            context.color = colors[grain]
            //context.fillStyle = grain.color
            if (grain.iconString != null) {
                context.drawString(grain.iconString, currentX - xStep, currentY - yStep)
            } else {
                context.fillRect(currentX - xStep, currentY - yStep, xStep, yStep)
            }
        }

        currentX += xStep
        if (currentX >= xMax) {
            currentX = 0
            currentY += yStep
        }
    }

    val stream = ByteArrayOutputStream(simulation.dataSize)
    ImageIO.write(buffer, "png", stream)
    stream.close()

    return stream.toByteArray()
}

