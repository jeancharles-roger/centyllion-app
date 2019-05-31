package com.centyllion.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.centyllion.model.User
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

val testUser = User("1", "1234", "Test", "test@centyllion.com")

/** Create a private and public key pair for API tests with credentials */
private val jwtAlgorithm =  KeyPairGenerator.getInstance("RSA").let { generator ->
    generator.initialize(512)
    val pair = generator.generateKeyPair()
    val publicKey = pair.public as RSAPublicKey
    val privateKey = pair.private as RSAPrivateKey
    Algorithm.RSA256(publicKey, privateKey)
}

/** Creates a JWT token for test API */
fun createTextJwtToken(user: User, vararg role: String) =
    JWT.create()
        .withAudience(authClient).withClaim("sub", user.keycloakId)
        .withClaim("name", user.name).withClaim("email", user.email)
        .withArrayClaim("roles", role)
        .withIssuer(authBase).sign(jwtAlgorithm)!!

/** Execute tests with the Centyllion API testing rig */
@KtorExperimentalAPI
fun <R> withCentyllion(test: TestApplicationEngine.() -> R): R =
    withTestApplication(
        {
            val verifier = JWT.require(jwtAlgorithm).withIssuer(authBase).build()
            centyllion(false, MemoryData(), MemorySubscriptionManager(), verifier)
        }, test
    )

fun TestApplicationEngine.request(
    method: HttpMethod, uri: String, content: String?, user: User?, vararg role: String,
    setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall = handleRequest {
    this.uri = uri
    this.method = method
    if (user != null) this.addHeader("Authorization", "Bearer ${createTextJwtToken(user, *role)}")
    if (content != null) {
        this.addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        this.setBody(content)
    }
    setup()
}

fun TestApplicationEngine.handleLoggedInGet(uri: String, user: User?, vararg role: String) =
    request(HttpMethod.Get, uri, null, user, *role)

fun TestApplicationEngine.handleLoggedInPost(uri: String, content: String, user: User?, vararg role: String) =
    request(HttpMethod.Post, uri, content, user, *role)

fun TestApplicationEngine.handleLoggedInPatch(uri: String, content: String, user: User?, vararg role: String) =
    request(HttpMethod.Patch, uri, content, user, *role)

fun TestApplicationEngine.handleLoggedInDelete(uri: String, user: User?, vararg role: String) =
    request(HttpMethod.Delete, uri, null, user, *role)

fun TestApplicationEngine.testUnauthorized(
    uri: String, method: HttpMethod = HttpMethod.Get, user: User? = null, vararg role: String
) {
    val request = request(method, uri, "", user, *role)
    val status = request.response.status()
    if (status != null) assertEquals(HttpStatusCode.Unauthorized, status)
}

fun <T> TestApplicationEngine.testGet(
    uri: String, expected: T, serializer: KSerializer<T>,
    user: User? = null, vararg role: String
) {
    val request = handleLoggedInGet(uri, user, *role)
    checkResult(request, serializer, expected)
}

private fun <T> checkResult(request: TestApplicationCall, serializer: KSerializer<T>, expected: T) {
    assertEquals(HttpStatusCode.OK, request.response.status())

    val result = request.response.content
    assertNotNull(result)

    val retrieved = Json.parse(serializer, result)
    assertEquals(expected, retrieved)
}
