package com.centyllion.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.centyllion.model.User
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

/** Create a private and public key pair for API tests with credentials */
private val jwtAlgorithm =  KeyPairGenerator.getInstance("RSA").let { generator ->
    generator.initialize(512)
    val pair = generator.generateKeyPair()
    val publicKey = pair.public as RSAPublicKey
    val privateKey = pair.private as RSAPrivateKey
    Algorithm.RSA256(publicKey, privateKey)
}

/** Creates a JWT token for test API */
fun createTextJwtToken(user: User) =
    JWT.create()
        .withAudience(authClient).withClaim("sub", user.keycloakId)
        .withClaim("name", user.name).withClaim("email", user.email)
        .withIssuer(authBase).sign(jwtAlgorithm)!!

/** Execute tests with the Centyllion API testing rig */
@KtorExperimentalAPI
fun <R> withCentyllion(test: TestApplicationEngine.() -> R): R =
    withTestApplication(
        {
            val verifier = JWT.require(jwtAlgorithm).withIssuer(authBase).build()
            centyllion(false, MemoryData(), verifier)
        }, test
    )

fun TestApplicationEngine.handleLoggedInRequest(
    method: HttpMethod, uri: String, user: User,
    setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall = handleRequest {
    this.uri = uri
    this.method = method
    this.addHeader("Authorization", "Bearer ${createTextJwtToken(user)}")
    setup()
}
