package com.centyllion.ui.dialog

import androidx.compose.animation.core.*
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ModelResultPage
import com.centyllion.model.SimulationDescription
import com.centyllion.model.SimulationResultPage
import com.centyllion.ui.AppContext
import com.centyllion.ui.SimpleTextField
import com.centyllion.ui.row
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.FileImport
import compose.icons.fontawesomeicons.solid.RedoAlt
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImportFromCentyllion: Dialog {

    override val titleKey: String = "Import from Centyllion\u2026"

    sealed interface ResultItem {
        val label: String
    }

    data class ErrorItem(
        val message: String
    ): ResultItem {
        override val label: String get() = message
    }

    data class SimulationItem(
        val model: GrainModelDescription,
        val simulation: SimulationDescription,
    ): ResultItem {
        override val label: String get() = "${model.name} - ${simulation.name} by ${model.info.user?.name}"

        val isNameNotBlank get() = model.name.isNotBlank() || simulation.name.isNotBlank()
    }

    @Composable
    override fun content(appContext: AppContext) {
        Box(Modifier.fillMaxWidth()) {
            val searching = remember { mutableStateOf(false) }
            val offset = remember { mutableStateOf<Long>(0) }
            val searchText = remember { mutableStateOf("") }
            val foundSimulations = remember {
                mutableStateOf(listOf<ResultItem>())
            }
            val listState = rememberLazyListState()

            LazyColumn(state = listState) {

                item {
                    row(
                        trailingContent = {
                            if (searching.value) {
                                val infiniteTransition = rememberInfiniteTransition()
                                val angle = infiniteTransition.animateFloat(
                                    initialValue = 0F,
                                    targetValue = 360F,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = LinearEasing)
                                    )
                                )
                                Icon(
                                    FontAwesomeIcons.Solid.RedoAlt,
                                    null,
                                    modifier = Modifier.size(24.dp).rotate(angle.value)
                                )
                            }
                        },
                        trailingRatio = .2f
                    ) {
                        Text(appContext.locale.i18n("Search"))
                        SimpleTextField(appContext, searchText.value) { newTextSearch ->
                            searchText.value = newTextSearch
                            foundSimulations.value = emptyList()
                            searching.value = true
                            appContext.scope.launch(Dispatchers.IO) {
                                try {
                                    searchSimulations(searchText.value, offset.value, 20).content
                                        .forEach { simulation ->
                                            val item = SimulationItem(getModel(simulation.modelId), simulation)
                                            synchronized(foundSimulations) {
                                                foundSimulations.value += item
                                            }
                                        }
                                } catch (e: Throwable) {
                                    synchronized(foundSimulations) {
                                        foundSimulations.value += listOf(ErrorItem(e.localizedMessage))
                                    }
                                }
                                searching.value = false
                            }
                        }
                    }

                    Spacer(Modifier
                        .padding(horizontal = 4.dp)
                        .background(Color.Gray.copy(alpha = .7f))
                        .fillMaxWidth()
                        .height(2.dp))
                }

                items(foundSimulations.value) { item ->
                    Row(Modifier.fillParentMaxWidth().padding(vertical = 8.dp, horizontal = 18.dp)) {
                        Text(
                            item.label,
                            Modifier.weight(1f).align(Alignment.CenterVertically)
                        )

                        if (item is SimulationItem) {
                            IconButton(
                                onClick = {
                                    appContext.importModelAndSimulation(item.model.model, item.simulation.simulation)
                                    appContext.currentDialog = null
                                },
                                modifier = appContext.theme.buttonIconModifier.align(Alignment.CenterVertically),
                            ) {
                                Icon(imageVector = FontAwesomeIcons.Solid.FileImport, contentDescription = null)
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )


        }
    }

    val hostBase = "https://app.centyllion.com/api"

    val client = HttpClient {
        //expectSuccess = true
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun searchSimulations(query: String, offset: Long, limit: Long): SimulationResultPage =
        client.get("$hostBase/simulation/search?q=$query&offset=$offset&limit=$limit").body()

    suspend fun searchSimulationsForModel(model: GrainModelDescription, offset: Long, limit: Long): SimulationResultPage =
        client.get("$hostBase/simulation/search?model=${model.id}&offset=$offset&limit=$limit").body()

    suspend fun searchModels(query: String, offset: Long, limit: Long): ModelResultPage =
        client.get("$hostBase/model/search?q=$query&offset=$offset&limit=$limit").body()

    suspend fun getSimulation(id: String): SimulationDescription =
        client.get("$hostBase/simulation/$id").body()

    suspend fun getModel(id: String): GrainModelDescription =
        client.get("$hostBase/model/$id").body()

}
