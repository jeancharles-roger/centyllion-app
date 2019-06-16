package com.centyllion.backend

import com.centyllion.common.SubscriptionType
import com.centyllion.common.topGroup
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder

const val masterRealm = "master"
const val masterLogin = "automation"
const val masterClient = "admin-cli"

interface AuthorizationManager {

    fun getGroup(id: String): SubscriptionType

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

    override fun getGroup(id: String): SubscriptionType = useClient { client ->
        val centyllionRealm = client.realm(authRealm)
        val user = centyllionRealm.users().get(id)
        user?.groups()?.map { SubscriptionType.parse(it.name) }.topGroup()
    }

    fun joinGroup(id: String, groupId: String) = useClient { client ->
        val centyllionRealm = client.realm(authRealm)
        val user = centyllionRealm.users().get(id)
        user?.joinGroup(groupId)
        Unit
    }

    fun leaveGroup(id: String, groupId: String) = useClient { client ->
        val centyllionRealm = client.realm(authRealm)
        val user = centyllionRealm.users().get(id)
        user?.leaveGroup(groupId)
        Unit
    }
}
