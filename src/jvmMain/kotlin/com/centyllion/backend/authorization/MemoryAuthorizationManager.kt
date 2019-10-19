package com.centyllion.backend.authorization

import java.util.LinkedHashMap

class MemoryAuthorizationManager(
    val groups: LinkedHashMap<String, Set<String>> = linkedMapOf(),
    val backend: AuthorizationManager? = null
): AuthorizationManager {

    override fun getGroups(id: String) =
        (groups[id] ?: backend?.getGroups(id))?.toList() ?: emptyList()

    override fun joinGroup(id: String, groupId: String) {
        val existing = groups[id] ?: emptySet()
        groups[id] = existing + groupId
    }

    override fun leaveGroup(id: String, groupId: String) {
        val existing = groups[id] ?: emptySet()
        groups[id] = existing - groupId
    }

}
