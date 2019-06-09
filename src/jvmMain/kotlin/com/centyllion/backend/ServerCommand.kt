package com.centyllion.backend

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
import kotlinx.serialization.Serializable
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

@Serializable
data class ServerConfig(
    val host: String, val port: Int,
    val dbType: String, val dbHost: String, val dbPort: Int,
    val dbName: String, val dbUser: String, val dbPassword: String,
    val stripeKey: String, val keycloakPassword: String
) {
    fun data() = SqlData(dbType, dbHost, dbPort, dbName, dbUser, dbPassword)

    fun subscription() = KeycloakStripeSubscriptionManager(stripeKey, keycloakPassword)
}

class ServerCommand : CliktCommand("Start the server") {

    val ssl by option(help = "Uses a ssl connector").flag(default = false)

    val debug by option(help = "Activate debug parameters").flag(default = false)

    val host by option(help = "Host to listen").default("localhost")
    val port by option(help = "Port to listen").int().default(0)


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
        return ServerConfig(
            host,
            port,
            dbType,
            dbHost,
            dbPort,
            dbName,
            dbUser,
            actualDbPassword,
            actualStripeKey,
            actualKeycloakPassword
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

            val config = config()
            module { centyllion(debug, config.data(), config.subscription()) }

        }

        embeddedServer(Netty, env).start(wait = true)
    }
}
