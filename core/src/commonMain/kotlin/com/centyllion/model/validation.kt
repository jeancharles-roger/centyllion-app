package com.centyllion.model

import com.centyllion.i18n.Locale

fun GrainModel.diagnose(locale: Locale): List<Problem> = buildList {
    grains.forEach { it.diagnose(this@diagnose, locale, this) }
    behaviours.forEach { it.diagnose(this@diagnose, locale, this) }
    fields.forEach { it.diagnose(this@diagnose, locale, this) }
}

fun Grain.hasNoError(model: GrainModel, locale: Locale) =
    buildList { diagnose(model,locale, this) }.isEmpty()

fun Grain.diagnose(model: GrainModel, locale: Locale, result: MutableList<Problem>) {
    if (halfLife < 0) {
        result.add(Problem(this, "Half-life", locale.i18n("Half-life must be positive or zero")))
    }

    if (movementProbability < 0.0 || movementProbability > 1.0) {
        result.add(Problem(this, "Speed", locale.i18n("Speed must be between 0 and 1")))
    }

    fieldProductions.forEach { entry ->
        if (entry.value < -1 || entry.value > 1) {
            val field = model.fieldForId(entry.key)
            result.add(Problem(this, "Field production", locale.i18n("Field production for %0 must be between -1 and 1", field?.name ?: "")))
        }

        if ((fieldPermeable[entry.key] ?: 1f) <= 0f && entry.value != 0f) {
            val field = model.fieldForId(entry.key)
            result.add(Problem(this, "Field permeability", locale.i18n("Field permeability will prevent production for %0", field?.name ?: "")))
        }
    }

    fieldInfluences.forEach { entry ->
        if (entry.value < -1 || entry.value > 1) {
            val field = model.fieldForId(entry.key)
            result.add(Problem(this, "Field influence", locale.i18n("Field influence for %0 must be between -1 and 1", field?.name ?: "")))
        }
    }

    fieldPermeable.forEach {entry ->
        if (entry.value < 0 || entry.value > 1) {
            val field = model.fieldForId(entry.key)
            Problem(this, "Field permeability", locale.i18n("Field permeability for %0 must be between 0 and 1", field?.name ?: ""))
        }
    }
}

fun Behaviour.hasNoError(model: GrainModel, locale: Locale) =
    buildList { diagnose(model,locale, this) }.isEmpty()

fun Behaviour.diagnose(model: GrainModel, locale: Locale, result: MutableList<Problem>) {
    if (mainReactiveId < 0) {
        result.add(Problem(this, "Main Reactive", locale.i18n("Behaviour must have a main reactive")))
    }

    if (probability < 0.0 || probability > 1.0) {
        result.add(Problem(this, "Speed", locale.i18n("Speed must be between 0 and 1")))
    }

    if (agePredicate.constant < 0) {
        result.add(Problem(this, "Age Predicate", locale.i18n("Age predicate value must be positive or zero")))
    }

    fieldPredicates.forEach { predicate ->
        if (predicate.second.constant < 0f || predicate.second.constant > 1f) {
            val field = model.fieldForId(predicate.first)
            result.add(Problem(this, "Threshold", locale.i18n("Field threshold value for %0 must be between 0 and 1", field?.name ?: "")))
        }
    }

    reaction.forEachIndexed { index, reaction ->
        reaction.diagnose(model, this, index+1, locale, result)
    }
}

fun Reaction.diagnose(model: GrainModel, behaviour: Behaviour, index: Int, locale: Locale, result: MutableList<Problem>) {
    if (reactiveId >= 0 && model.grainForId(reactiveId) == null) {
        result.add(Problem(behaviour, "Reactive Id", locale.i18n("Grain with id %0 doesn't exist for reactive %1", reactiveId, index)))
    }
    if (productId >= 0 && model.grainForId(productId) == null) {
        result.add(Problem(behaviour, "Product Id", locale.i18n("Grain with id %0 doesn't exist for reactive %1", productId, index)))
    }
    if (allowedDirection.isEmpty()) {
        result.add(Problem(behaviour, "Direction", locale.i18n("No direction allowed for reactive %0", index)))
    }
}

fun Field.diagnose(model: GrainModel, locale: Locale, result: MutableList<Problem>) {
    if (halfLife < 0) {
        result.add(Problem(this, "Half-life", locale.i18n("Half-life must be positive or zero")))
    }

    if (speed < 0.0 || speed > 1.0) {
        result.add(Problem(this, "Speed", locale.i18n("Speed must be between 0 and 1")))
    }
}


