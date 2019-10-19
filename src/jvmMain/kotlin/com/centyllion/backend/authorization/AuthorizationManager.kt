package com.centyllion.backend.authorization

import com.centyllion.backend.authBase
import com.centyllion.backend.authRealm
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder

const val masterRealm = "master"
const val masterLogin = "automation"
const val masterClient = "admin-cli"

const val apprenticeGroupId = "632088b8-ef86-4cb9-8ad4-6c65a88c1e5b"
const val creatorGroupId = "822926b1-3a8e-4b8a-9531-d9492d400b09"
const val masterGroupId = "5fecdefa-4998-4afc-835a-d796ddde5591"
const val adminGroupId = "be7ab8c7-1dd1-4569-ba6c-36c6449db7a3"

interface AuthorizationManager {
    fun getGroups(id: String): List<String>
    fun joinGroup(id: String, groupId: String)
    fun leaveGroup(id: String, groupId: String)
}

class KeycloakAuthorizationManager(
    val keycloakPassword: String
) : AuthorizationManager {

    private fun client() = KeycloakBuilder.builder()
        .serverUrl(authBase).realm(masterRealm).clientId(masterClient)
        .username(masterLogin).password(keycloakPassword).build()

    fun <T> useClient(block: (Keycloak) -> T): T {
        val client = client()
        val result = block(client)
        client.close()
        return result
    }

    override fun getGroups(id: String) = useClient { client ->
        val centyllionRealm = client.realm(authRealm)
        val user = centyllionRealm.users().get(id)
        user?.groups()?.map { it.id } ?: emptyList()
    }

    override fun joinGroup(id: String, groupId: String) = useClient { client ->
        val centyllionRealm = client.realm(authRealm)
        val user = centyllionRealm.users().get(id)
        user?.joinGroup(groupId)
        Unit
    }

    override fun leaveGroup(id: String, groupId: String) = useClient { client ->
        val centyllionRealm = client.realm(authRealm)
        val user = centyllionRealm.users().get(id)
        user?.leaveGroup(groupId)
        Unit
    }
}
