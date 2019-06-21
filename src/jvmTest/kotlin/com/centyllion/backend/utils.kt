package com.centyllion.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.centyllion.backend.data.Data
import com.centyllion.common.SubscriptionType
import com.centyllion.model.User
import com.centyllion.model.UserDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

val testUserDetails = UserDetails("1234", "test@centyllion.com", null, SubscriptionType.Apprentice, null)
val testUser = User("1", "Test", "tester", testUserDetails)

/** Create a private and public key pair for API tests with credentials */
private val jwtAlgorithm =  KeyPairGenerator.getInstance("RSA").let { generator ->
    generator.initialize(512)
    val pair = generator.generateKeyPair()
    val publicKey = pair.public as RSAPublicKey
    val privateKey = pair.private as RSAPrivateKey
    Algorithm.RSA256(publicKey, privateKey)
}

/** Creates a JWT token for test API */
fun createTextJwtToken(id: String, name: String, email: String, vararg role: String) =
    JWT.create()
        .withAudience(authClient).withClaim("sub", id)
        .withClaim("name", name).withClaim("email", email)
        .withArrayClaim("roles", role)
        .withIssuer(authBase).sign(jwtAlgorithm)!!

class TestConfig: ServerConfig {
    override val debug: Boolean = false

    override val verifier: JWTVerifier? = JWT.require(jwtAlgorithm).withIssuer(authBase).build()
    override val authorization: AuthorizationManager = MemoryAuthorizationManager()
    override val payment: PaymentManager = MemoryPaymentManager()
    override val data: Data = MemoryData()
}

/** Execute tests with the Centyllion API testing rig */
@KtorExperimentalAPI
fun <R> withCentyllion(test: TestApplicationEngine.() -> R): R =
    withTestApplication({ centyllion(TestConfig()) }, test)

fun TestApplicationEngine.request(
    method: HttpMethod, uri: String, content: String?, user: User?, vararg role: String,
    setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall = handleRequest {
    this.uri = uri
    this.method = method
    user?.details?.let{
        val token = createTextJwtToken(it.keycloakId, user.name, it.email, *role)
        this.addHeader("Authorization", "Bearer $token")
    }
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
