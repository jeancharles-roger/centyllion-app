package com.centyllion.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.centyllion.backend.authorization.AuthorizationManager
import com.centyllion.backend.authorization.MemoryAuthorizationManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.data.MemoryData
import com.centyllion.model.ResultPage
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

val user1Details = UserDetails("1234", "test@centyllion.com")
val user1 = User("1", "Apprentice", "app", user1Details)

val user2Details = UserDetails("1235", "test@centyllion.com")
val user2 = User("2", "Creator", "cre", user2Details)

val adminUserDetails = UserDetails("1236", "test@centyllion.com")
val adminUser = User("4", "Admin", "adm", adminUserDetails)

/** Create a private and public key pair for API tests with credentials */
private val jwtAlgorithm =  KeyPairGenerator.getInstance("RSA").let { generator ->
    generator.initialize(512)
    val pair = generator.generateKeyPair()
    val publicKey = pair.public as RSAPublicKey
    val privateKey = pair.private as RSAPrivateKey
    Algorithm.RSA256(publicKey, privateKey)
}

/** Creates a JWT token for test API */
fun createTextJwtToken(id: String, name: String, email: String, roles: Array<out String>) =
    JWT.create()
        .withAudience(authClient).withClaim("sub", id)
        .withClaim("name", name).withClaim("email", email)
        .withArrayClaim("roles", roles)
        .withIssuer(authBase).sign(jwtAlgorithm)!!

class TestConfig: ServerConfig {
    override val debug: Boolean = false
    override val dry: Boolean = true

    override val verifier: JWTVerifier? = JWT.require(jwtAlgorithm).withIssuer(authBase).build()
    override val authorization: AuthorizationManager = MemoryAuthorizationManager()
    override val data: Data = MemoryData()

    override val webroot = "webroot"

    override var rootJs: String = "/js/centyllion/centyllion.js"
}

/** Execute tests with the Centyllion API testing rig */
fun <R> withCentyllion(test: TestApplicationEngine.() -> R): R =
    withTestApplication({ centyllion(TestConfig()) }, test)

fun TestApplicationEngine.request(
    method: HttpMethod, uri: String, content: String?, user: User?, setup: TestApplicationRequest.() -> Unit = {}
): TestApplicationCall = handleRequest {
    this.uri = uri
    this.method = method
    user?.details?.let{
        val token = createTextJwtToken(it.keycloakId, user.name, it.email, emptyArray())
        this.addHeader("Authorization", "Bearer $token")
    }
    if (content != null) {
        this.addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        this.setBody(content)
    }
    setup()
}

fun TestApplicationEngine.handleGet(uri: String, user: User?) =
    request(HttpMethod.Get, uri, null, user)


fun TestApplicationEngine.handlePost(uri: String, content: String, user: User?) =
    request(HttpMethod.Post, uri, content, user)

fun TestApplicationEngine.handlePatch(uri: String, content: String, user: User?) =
    request(HttpMethod.Patch, uri, content, user)

fun TestApplicationEngine.handleDelete(uri: String, user: User?) =
    request(HttpMethod.Delete, uri, null, user)

fun TestApplicationEngine.testUnauthorized(
    uri: String, method: HttpMethod = HttpMethod.Get, user: User? = null
) {
    val request = request(method, uri, "", user)
    val status = request.response.status()
    if (status != null) assertEquals(HttpStatusCode.Unauthorized, status)
}

fun <T> TestApplicationEngine.testGet(uri: String, expected: T, serializer: KSerializer<T>, user: User? = null) {
    val request = handleGet(uri, user)
    checkResult(request, serializer, expected)
}

fun <T> TestApplicationEngine.testGetPage(uri: String, expected: List<T>, total: Long, serializer: KSerializer<T>, user: User? = null) {
    val request = handleGet(uri, user)
    checkResult(request, ResultPage.serializer(serializer), ResultPage(expected, 0, total))
}


fun <T> TestApplicationEngine.get(uri: String, serializer: KSerializer<T>, user: User? = null): T {
    val request = handleGet(uri, user)
    return checkResult(request, serializer, null)
}

fun <T, R> TestApplicationEngine.testPost(
    uri: String, value: T, inputSerializer: KSerializer<T>,
    outputKSerializer: KSerializer<R>, user: User? = null
): R {
    val content = Json.encodeToString(inputSerializer, value)
    val request = handlePost(uri, content, user)
    return checkResult(request, outputKSerializer, null)
}

private fun <T> checkResult(request: TestApplicationCall, serializer: KSerializer<T>, expected: T?): T {
    assertEquals(HttpStatusCode.OK, request.response.status())

    val result = request.response.content
    assertNotNull(result)

    val retrieved = Json.decodeFromString(serializer, result)
    if (expected != null) assertEquals(expected, retrieved)
    return retrieved
}
