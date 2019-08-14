package com.centyllion.backend.authorization

import com.centyllion.common.SubscriptionType
import java.util.LinkedHashMap

class MemoryAuthorizationManager(
    val groups: LinkedHashMap<String, SubscriptionType> = linkedMapOf(),
    val backend: AuthorizationManager? = null
): AuthorizationManager {

    override fun getGroup(id: String): SubscriptionType =
        groups[id] ?: backend?.getGroup(id) ?: SubscriptionType.Apprentice

    override fun joinGroup(id: String, group: SubscriptionType) {
        groups[id] = group
    }

    override fun leaveGroup(id: String, group: SubscriptionType) {
        if (groups[id] == group) {
            groups[id] == SubscriptionType.Apprentice
        }
    }

}
