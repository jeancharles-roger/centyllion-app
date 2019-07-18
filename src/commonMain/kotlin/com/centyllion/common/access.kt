package com.centyllion.common

const val centyllionHost = "centyllion.com"

const val apprenticeRole = "apprentice"
const val creatorRole = "creator"
const val masterRole = "master"
const val adminRole = "admin"

const val apprenticeIcon = "graduation-cap"
const val creatorIcon = "hammer"
const val masterIcon = "chalkboard"
const val adminIcon = "cogs"

val roleIcons = mapOf(
    apprenticeRole to apprenticeIcon,
    creatorRole to creatorIcon,
    masterRole to masterIcon,
    adminRole to adminIcon
)

enum class SubscriptionType(val role: String?, val groupId: String) {
    Apprentice(null, "632088b8-ef86-4cb9-8ad4-6c65a88c1e5b"),
    Creator(creatorRole, "822926b1-3a8e-4b8a-9531-d9492d400b09"),
    Master(masterRole, "5fecdefa-4998-4afc-835a-d796ddde5591"),
    Admin(adminRole, "be7ab8c7-1dd1-4569-ba6c-36c6449db7a3");

    companion object {
        fun parse(value: String) = when {
            value.equals("Creator", true) -> Creator
            value.equals("Master", true) -> Master
            value.equals("Admin", true) -> Admin
            else -> Apprentice
        }
    }

    fun max(other: SubscriptionType?) = if (other == null || other.ordinal <= ordinal) this else other
}

fun List<SubscriptionType>?.topGroup() = this
    ?.fold(SubscriptionType.Apprentice) { p, c -> if (c.ordinal > p.ordinal) c else p }
    ?: SubscriptionType.Apprentice
