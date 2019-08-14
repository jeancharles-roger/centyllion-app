package com.centyllion.backend

import com.auth0.jwt.JWTVerifier
import com.centyllion.backend.authorization.AuthorizationManager
import com.centyllion.backend.authorization.KeycloakAuthorizationManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.data.MemoryData
import com.centyllion.backend.data.SqlData
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import io.ktor.network.tls.certificates.generateCertificate
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore

private fun certificateKeystore(): KeyStore {
    return if (Files.exists(certificatePath)) Files.newInputStream(certificatePath).use {
        val keyStore = KeyStore.getInstance("JKS")!!
        keyStore.load(it, certificatePassword.toCharArray())
        keyStore
    } else {
        // TODO use here a proper certificate, this is only for debug
        generateCertificate(
            certificatePath.toFile(),
            keyAlias = certificateKeyAlias,
            keyPassword = certificatePassword,
            jksPassword = certificatePassword
        )
    }
}

interface ServerConfig {

    val debug: Boolean
    val dry: Boolean
    val verifier: JWTVerifier?

    val authorization: AuthorizationManager
    val payment: PaymentManager
    val data: Data

    val webroot: String
}

data class CliServerConfig(
    override val debug: Boolean, override val dry: Boolean,
    val host: String, val port: Int,
    val dbType: String, val dbHost: String, val dbPort: Int,
    val dbName: String, val dbUser: String, val dbPassword: String,
    val stripeKey: String, val keycloakPassword: String,
    override val webroot: String = "webroot"

): ServerConfig {

    override val authorization: AuthorizationManager =
        KeycloakAuthorizationManager(keycloakPassword)

    override val payment: PaymentManager = StripePaymentManager(stripeKey)

    override val data: Data = SqlData(dbType, dbHost, dbPort, dbName, dbUser, dbPassword).let {
        if (dry) MemoryData(backend = it) else it
    }

    override val verifier: JWTVerifier? = null

}

class ServerCommand : CliktCommand("Start the server") {

    val ssl by option(help = "Uses a ssl connector").flag(default = false)

    val debug by option(help = "Activate debug parameters").flag(default = false)

    val host by option(help = "Host to listen").default("localhost")
    val port by option(help = "Port to listen").int().default(0)

    val dry by option(help = "The modification are only done in memory").flag(default = false)

    val dbType by option("--db-type", help = "Database type")
        .default("postgresql")

    val dbHost by option("--db-host", help = "Database host")
        .default("localhost")

    val dbPort by option("--db-port", help = "Database port").int().default(5432)
    val dbName by option("--db-name", help = "Database name").default("centyllion")
    val dbUser by option("--db-user", help = "Database user name").default("centyllion")
    val dbPassword by option("--db-password", help = "Database password")

    val stripeKey by option("--stripe-key", help = "Stripe key")

    val keycloakPassword by option("--keycloak-password", help = "Keycloak password")

    val keystore by option(help = "Keystore for passwords and keys")
        .path(exists = true, readable = true)
        .default(Paths.get("centyllion.jks"))

    val password by option(help = "Keystore password", envvar = "KEYSTORE_PASSWORD")
        .prompt("Keystore password", hideInput = true)

    val dbPasswordAlias by option(
        "--db-password-alias", help = "Database password alias for keystore"
    ).default("dbPassword")

    val stripeKeyAlias by option(
        "--stripe-key-alias", help = "Stripe key alias for keystore"
    ).default("stripeKey")

    val keycloakPasswordKeyAlias by option(
        "--keycloak-password-alias", help = "Keycloak key alias for keystore"
    ).default("keycloak")

    val webroot by option("--webroot", help = "Directory of static resources").default("webroot")

    fun extractPassword(keystore: KeyStore, alias: String, pwd: CharArray): String {
        if (keystore.containsAlias(alias)) {
            val stripeKeyEntry = keystore.getEntry(alias, KeyStore.PasswordProtection(pwd))
            if (stripeKeyEntry is KeyStore.SecretKeyEntry) return String(stripeKeyEntry.secretKey.encoded)
            else throw UsageError("Alias $alias in keystore isn't a secret key")
        } else throw UsageError("Alias $alias in keystore doesn't exist")
    }

    fun config(): ServerConfig {
        // loads keystore from
        val pwd = password.toCharArray()
        val loadedKeystore = Files.newInputStream(keystore).use {
            val keyStore = KeyStore.getInstance("JKS")!!
            keyStore.load(it, pwd)
            keyStore
        }

        val actualDbPassword = dbPassword ?: extractPassword(loadedKeystore, dbPasswordAlias, pwd)
        val actualStripeKey = stripeKey ?: extractPassword(loadedKeystore, stripeKeyAlias, pwd)
        val actualKeycloakPassword = keycloakPassword ?: extractPassword(loadedKeystore, keycloakPasswordKeyAlias, pwd)
        return CliServerConfig(
            debug, dry,
            host, port,
            dbType, dbHost, dbPort, dbName, dbUser, actualDbPassword,
            actualStripeKey, actualKeycloakPassword,
            webroot
        )
    }


    @KtorExperimentalAPI
    override fun run() {
        val effectivePort = if (port <= 0) if (ssl) 8443 else 8080 else port

        val env = applicationEngineEnvironment {
            if (ssl) {
                sslConnector(
                    certificateKeystore(),
                    certificateKeyAlias,
                    passwordProvider,
                    passwordProvider
                ) {
                    host = this@ServerCommand.host
                    port = effectivePort
                }
            } else {
                connector {
                    host = this@ServerCommand.host
                    port = effectivePort
                }
            }

            module { centyllion(config()) }

        }

        embeddedServer(Netty, env).start(wait = true)
    }
}
