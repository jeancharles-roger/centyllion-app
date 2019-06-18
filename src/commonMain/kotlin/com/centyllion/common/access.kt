package com.centyllion.common

const val centyllionHost = "centyllion.com"

const val adminRole = "admin"
const val creatorRole = "creator"
const val masterRole = "master"

enum class SubscriptionType(val groupPath: String) {
    Free("/Free"),
    Creator("/Creator"),
    Master("/Master"),
    Admin("/Admin");

    companion object {
        fun parse(value: String) = when {
            value.equals("Creator", true) -> Creator
            value.equals("Master", true) -> Master
            value.equals("Admin", true) -> Admin
            else -> Free
        }
    }

    fun max(other: SubscriptionType?) = if (other == null || other.ordinal <= ordinal) this else other
}

fun List<SubscriptionType>?.topGroup() =
    this?.fold(SubscriptionType.Free) { p, c -> if (c.ordinal > p.ordinal) c else p } ?: SubscriptionType.Free
