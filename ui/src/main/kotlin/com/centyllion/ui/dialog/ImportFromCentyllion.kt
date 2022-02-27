package com.centyllion.ui.dialog

import androidx.compose.animation.core.*
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ModelResultPage
import com.centyllion.model.SimulationDescription
import com.centyllion.model.SimulationResultPage
import com.centyllion.ui.AppContext
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.FileImport
import compose.icons.fontawesomeicons.solid.RedoAlt
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImportFromCentyllion: Dialog {

    override val titleKey: String = "Import from Centyllion\u2026"

    data class SimulationItem(
        val model: GrainModelDescription,
        val simulation: SimulationDescription,
    ) {
        val isNameNotBlank get() = model.name.isNotBlank() || simulation.name.isNotBlank()
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    override fun content(appContext: AppContext) {
        Box {
            val searching = remember { mutableStateOf(false) }
            val offset = remember { mutableStateOf<Long>(0) }
            val searchText = remember { mutableStateOf("") }
            val foundSimulations = remember {
                mutableStateOf(listOf<SimulationItem>())
            }
            val listState = rememberLazyListState()

            LazyColumn(state = listState) {

                item {
                    Row(Modifier.padding(vertical = 4.dp, horizontal = 18.dp)) {
                        TextField(
                            label = { Text(appContext.locale.i18n("Search")) },
                            value = searchText.value,
                            onValueChange = {
                                newTextSearch -> searchText.value = newTextSearch
                                foundSimulations.value = emptyList()
                                searching.value = true
                                appContext.scope.launch(Dispatchers.IO) {
                                    searchSimulations(searchText.value, offset.value, 20).content
                                        .forEach { simulation ->
                                            val item = SimulationItem(getModel(simulation.modelId), simulation)
                                            synchronized(foundSimulations) { foundSimulations.value += item }
                                        }
                                    searching.value = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
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
                            }
                        )
                    }
                }

                items(foundSimulations.value) { item ->
                    Row(Modifier.fillParentMaxWidth().padding(vertical = 8.dp, horizontal = 18.dp)) {

                        Text(
                            "${item.model.name} - ${item.simulation.name} by ${item.model.info.user?.name}",
                            Modifier.weight(1f).align(Alignment.CenterVertically)
                        )

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

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )


        }
    }

    val hostBase = "https://app.centyllion.com/api"

    val client = HttpClient {
        //expectSuccess = true
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    suspend fun searchSimulations(query: String, offset: Long, limit: Long): SimulationResultPage =
        client.get("$hostBase/simulation/search?q=$query&offset=$offset&limit=$limit")

    suspend fun searchSimulationsForModel(model: GrainModelDescription, offset: Long, limit: Long): SimulationResultPage =
        client.get("$hostBase/simulation/search?model=${model.id}&offset=$offset&limit=$limit")

    suspend fun searchModels(query: String, offset: Long, limit: Long): ModelResultPage =
        client.get("$hostBase/model/search?q=$query&offset=$offset&limit=$limit")

    suspend fun getSimulation(id: String): SimulationDescription =
        client.get("$hostBase/simulation/$id")

    suspend fun getModel(id: String): GrainModelDescription =
        client.get("$hostBase/model/$id")

}
