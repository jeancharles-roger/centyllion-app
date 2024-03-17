package com.centyllion.model

import kotlinx.serialization.json.Json
import kotlin.random.Random

fun dendriteModel(): GrainModel {
    val ms = Grain(0, "ms", "blue", description = "Manganèse soluble")
    val mc = Grain(1, "mc", "red", description = "Manganèse cristallisé", movementProbability = 0.0)

    val r1 = Behaviour(
        "cristalisation", "Cristalisation du Manganèse", 1.0,
        mainReactiveId = mc.id, mainProductId = mc.id, reaction = listOf(Reaction(ms.id, mc.id))
    )

    return GrainModel("Dendrite", "test model", listOf(ms, mc), listOf(r1))
}

fun dendriteSimulation(size: Int = 100) = createSimulation(size = 100) {
    when (it) {
        size/2 -> 1
        else -> if (Random.nextDouble() < 0.15) 0 else -1
    }
}

fun antsModel()= Json.decodeFromString(GrainModel.serializer(), """
    {
        "name": "Fourmis Chaotiques",
        "description": "Tableau tout le temps changeant, pour les longs voyages, spatiaux ou temporels.",
        "grains": [{
            "id": 0,
            "name": "Bleu",
            "color": "Blue",
            "icon": "square-full",
            "invisible": false,
            "size": 1,
            "description": "",
            "halfLife": 0,
            "movementProbability": 1,
            "allowedDirection": ["Right", "RightUp", "RightDown"],
            "fieldProductions": {
                "0": 1
            },
            "fieldInfluences": {
                "1": -1,
                "0": 1,
                "2": 1
            },
            "fieldPermeable": {}
        }, {
            "id": 1,
            "name": "Rouge",
            "color": "Red",
            "icon": "square-full",
            "invisible": false,
            "size": 1,
            "description": "",
            "halfLife": 0,
            "movementProbability": 1,
            "allowedDirection": ["Left", "LeftUp", "LeftDown"],
            "fieldProductions": {
                "1": 1
            },
            "fieldInfluences": {
                "0": -1,
                "1": 1,
                "2": 1
            },
            "fieldPermeable": {}
        }, {
            "id": 2,
            "name": "Cryptonite",
            "color": "Chartreuse",
            "icon": "square-full",
            "invisible": false,
            "size": 1,
            "description": "",
            "halfLife": 0,
            "movementProbability": 1,
            "allowedDirection": ["Down", "Up", "Left", "Right"],
            "fieldProductions": {
                "2": 1
            },
            "fieldInfluences": {
                "1": 1,
                "2": 0.01,
                "0": 1
            },
            "fieldPermeable": {}
        }, {
            "id": 3,
            "name": "Crystal",
            "color": "Chartreuse",
            "icon": "square-full",
            "invisible": false,
            "size": 1,
            "description": "",
            "halfLife": 10,
            "movementProbability": 1,
            "allowedDirection": ["Down", "Up", "Left", "Right"],
            "fieldProductions": {
                "2": 1
            },
            "fieldInfluences": {
                "0": 1,
                "1": 1,
                "2": 0.01
            },
            "fieldPermeable": {}
        }, {
            "id": 4,
            "name": "Mur",
            "color": "Tan",
            "icon": "square-full",
            "invisible": false,
            "size": 1,
            "description": "",
            "halfLife": 0,
            "movementProbability": 0,
            "allowedDirection": ["Left", "Up", "Right", "Down"],
            "fieldProductions": {},
            "fieldInfluences": {},
            "fieldPermeable": {}
        }],
        "behaviours": [{
            "name": "R=>B",
            "description": "",
            "probability": 1,
            "agePredicate": {
                "op": "GreaterThanOrEquals",
                "constant": 50
            },
            "fieldPredicates": [{
                "first": 2,
                "second": {
                    "op": "GreaterThan",
                    "constant": 0.00001
                }
            }],
            "mainReactiveId": 0,
            "mainProductId": 1,
            "sourceReactive": -1,
            "fieldInfluences": {},
            "reaction": []
        }, {
            "name": "B=>R",
            "description": "",
            "probability": 1,
            "agePredicate": {
                "op": "GreaterThanOrEquals",
                "constant": 50
            },
            "fieldPredicates": [{
                "first": 2,
                "second": {
                    "op": "GreaterThan",
                    "constant": 0.00001
                }
            }],
            "mainReactiveId": 1,
            "mainProductId": 0,
            "sourceReactive": -1,
            "fieldInfluences": {},
            "reaction": []
        }, {
            "name": "Perturber",
            "description": "",
            "probability": 1,
            "agePredicate": {
                "op": "GreaterThanOrEquals",
                "constant": 0
            },
            "fieldPredicates": [],
            "mainReactiveId": 2,
            "mainProductId": 2,
            "sourceReactive": 0,
            "fieldInfluences": {
                "0": 0,
                "1": 0
            },
            "reaction": [{
                "reactiveId": -1,
                "productId": 3,
                "sourceReactive": -1,
                "allowedDirection": ["Left", "Right", "RightUp", "RightDown", "LeftDown", "LeftUp"]
            }]
        }],
        "fields": [{
            "id": 0,
            "name": "F-Bleu",
            "color": "Blue",
            "invisible": false,
            "description": "",
            "speed": 0.8,
            "halfLife": 3,
            "allowedDirection": ["Left", "Up", "Right", "Down"]
        }, {
            "id": 1,
            "name": "F-Rouge",
            "color": "Red",
            "invisible": false,
            "description": "",
            "speed": 0.8,
            "halfLife": 3,
            "allowedDirection": ["Left", "Up", "Right", "Down"]
        }, {
            "id": 2,
            "name": "field",
            "color": "Lime",
            "invisible": false,
            "description": "",
            "speed": 0.1,
            "halfLife": 3,
            "allowedDirection": ["Left", "Up", "Right", "Down"]
        }]
    }
""".trimIndent())

fun antsSimulation(size: Int = 100): Simulation {
    val third = size / 3
    return createSimulation("Chaos") {
        when (it) {
            in 0..third -> if (Random.nextInt(10) == 0) 0 else -1
            5000 -> 2
            in (size-third)..size -> if (Random.nextInt(10) == 0) 1 else -1
            else -> -1
        }
    }
}

fun formulaModel(): GrainModel = Json.decodeFromString(GrainModel.serializer(), """
{
  "fields": [
    {
      "name": "Champ",
      "color": "OrangeRed",
      "formula": "(1+sin(step/25))/2"
    },
    {
      "id": 1,
      "name": "Champ 1",
      "color": "Blue",
      "formula": "abs((step * x * y)/1000000)"
    },
    {
      "id": 2,
      "name": "Champ 2",
      "color": "LimeGreen",
      "formula": "avg(field0(x, y), field0(x-1, y), field0(x+1, y))"
    }
  ]
}
""")