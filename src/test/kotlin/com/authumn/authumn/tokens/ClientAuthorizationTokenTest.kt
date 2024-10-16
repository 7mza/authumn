package com.authumn.authumn.tokens

import com.authumn.authumn.AccessTokenResponse
import com.authumn.authumn.ClientAuthorizationProperties
import com.authumn.authumn.Introspect
import com.authumn.authumn.TEST_EMAIL
import com.authumn.authumn.TEST_PRIV
import com.authumn.authumn.TEST_ROLE
import com.authumn.authumn.commons.Commons
import com.authumn.authumn.keypairs.IKeyPairService
import com.authumn.authumn.loginAndGetAuthorizationCode
import com.authumn.authumn.mockUserDetailsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.gargoylesoftware.htmlunit.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ClientAuthorizationTokenTest {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var htmlClient: WebClient

    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var keyPairService: IKeyPairService

    @Autowired
    private lateinit var clientProperties: ClientAuthorizationProperties

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userDetailsService: UserDetailsService

    @MockBean
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var body: String

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()

        whenever(passwordEncoder.matches(anyString(), anyString())).thenReturn(true)

        mockUserDetailsService(userDetailsService)

        // FIXME: test with no keys
        keyPairService.save(keyPairService.generateJWKSet())
        keyPairService.save(keyPairService.generateJWKSet())
        keyPairService.save(keyPairService.generateJWKSet())

        body =
            "grant_type=authorization_code" +
            "&redirect_uri=${clientProperties.redirectUris!!.first()}" +
            "&client_id=${clientProperties.clientId}" +
            "&client_secret=secret" +
            "&scope=${clientProperties.scopes}" +
            "&code=${loginAndGetAuthorizationCode(htmlClient, clientProperties)}"
    }

    @AfterEach
    fun afterEach() {
        keyPairService.deleteAll()
    }

    @Test
    fun `get access token using client authorization flow`() {
        val response: AccessTokenResponse? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(AccessTokenResponse::class.java)
                .returnResult()
                .responseBody

        assertThat(response).isNotNull
        assertThat(response!!.accessToken).isInstanceOf(String::class.java)
        assertThat(response.expiresIn).isEqualTo(299)
        assertThat(response.idToken).isInstanceOf(String::class.java)
        assertThat(response.refreshToken).isInstanceOf(String::class.java)
        assertThat(response.scope.split(" ")).hasSameElementsAs(clientProperties.scopes!!.filter { it != "profile" })
        assertThat(response.tokenType).isEqualTo("Bearer")

        val jwtAccessToken = response.decodeToken(response.accessToken, objectMapper)
        logger.debug(
            "access_token:\n{}",
            """
${Commons.writeJson(jwtAccessToken!!.header, objectMapper)}
${Commons.writeJson(jwtAccessToken.payload, objectMapper)}
""",
        )

        assertThat(jwtAccessToken.header.alg).isEqualTo("RS256")
        assertThat(jwtAccessToken.header.kid).isEqualTo(keyPairService.findNewest().id)

        assertThat(jwtAccessToken.payload.aud).isEqualTo(clientProperties.clientId)
        assertThat(jwtAccessToken.payload.authTime).isNull()
        assertThat(jwtAccessToken.payload.azp).isNull()
        assertThat(jwtAccessToken.payload.exp).isInstanceOf(String::class.java)
        assertThat(jwtAccessToken.payload.iat).isInstanceOf(String::class.java)
        assertThat(jwtAccessToken.payload.iss).isEqualTo("http://localhost:0")
        assertThat(jwtAccessToken.payload.jti).isInstanceOf(String::class.java)
        assertThat(jwtAccessToken.payload.nbf).isInstanceOf(String::class.java)
        assertThat(jwtAccessToken.payload.roles).hasSameElementsAs(listOf(TEST_ROLE, TEST_PRIV))
        assertThat(jwtAccessToken.payload.scope).hasSameElementsAs(
            clientProperties.scopes!!.filter { it != "profile" }.plus(
                listOf(TEST_ROLE, TEST_PRIV),
            ),
        )
        assertThat(jwtAccessToken.payload.sid).isNull()
        assertThat(jwtAccessToken.payload.sub).isEqualTo(TEST_EMAIL)

        val jwtIdToken = response.decodeToken(response.idToken!!, objectMapper)
        logger.debug(
            "id_token:\n{}",
            """
${Commons.writeJson(jwtIdToken!!.header, objectMapper)}
${Commons.writeJson(jwtIdToken.payload, objectMapper)}
""",
        )

        assertThat(jwtIdToken.header.alg).isEqualTo("RS256")
        assertThat(jwtIdToken.header.kid).isEqualTo(keyPairService.findNewest().id)

        assertThat(jwtIdToken.payload.aud).isEqualTo(clientProperties.clientId)
        assertThat(jwtIdToken.payload.authTime).isInstanceOf(java.lang.Long::class.java)
        assertThat(jwtIdToken.payload.azp).isEqualTo(clientProperties.clientId)
        assertThat(jwtIdToken.payload.exp).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.iat).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.iss).isEqualTo("http://localhost:0")
        assertThat(jwtIdToken.payload.jti).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.nbf).isNull()
        assertThat(jwtIdToken.payload.roles).isNull()
        assertThat(jwtIdToken.payload.scope).isNull()
        assertThat(jwtIdToken.payload.sid).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.sub).isEqualTo(TEST_EMAIL)
    }

    @Test
    fun `call secured api using retrieved access token`() {
        val response: AccessTokenResponse? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(AccessTokenResponse::class.java)
                .returnResult()
                .responseBody

        webTestClient
            .get()
            .uri("/api/user")
            .headers { it.setBearerAuth(response!!.accessToken) }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    // FIXME https://github.com/spring-projects/spring-authorization-server/issues/1359
    @Disabled
    @Test
    fun `refresh token & use it to call secured api`() {
        val accessResponse: AccessTokenResponse? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(AccessTokenResponse::class.java)
                .returnResult()
                .responseBody

        val refreshBody =
            "grant_type=refresh_token" +
                "&refresh_token=${accessResponse!!.refreshToken}" +
                "&client_id=${clientProperties.clientId}" +
                "&client_secret=secret"

        val refreshResponse: AccessTokenResponse? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(refreshBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(AccessTokenResponse::class.java)
                .returnResult()
                .responseBody

        assertThat(refreshResponse).usingRecursiveComparison().isNotEqualTo(accessResponse)

        assertThat(refreshResponse).isNotNull
        assertThat(refreshResponse!!.accessToken).isInstanceOf(String::class.java)
        assertThat(refreshResponse.expiresIn).isEqualTo(299)
        assertThat(refreshResponse.idToken).isInstanceOf(String::class.java)
        assertThat(refreshResponse.refreshToken).isInstanceOf(String::class.java)
        assertThat(refreshResponse.scope.split(" ")).hasSameElementsAs(clientProperties.scopes!!.filter { it != "profile" })
        assertThat(refreshResponse.tokenType).isEqualTo("Bearer")

        val jwtRefreshedAccessToken = refreshResponse.decodeToken(refreshResponse.accessToken, objectMapper)
        logger.debug(
            "access_token:\n{}",
            """
${Commons.writeJson(jwtRefreshedAccessToken!!.header, objectMapper)}
${Commons.writeJson(jwtRefreshedAccessToken.payload, objectMapper)}
""",
        )

        assertThat(jwtRefreshedAccessToken.header.alg).isEqualTo("RS256")
        assertThat(jwtRefreshedAccessToken.header.kid).isEqualTo(keyPairService.findNewest().id)

        assertThat(jwtRefreshedAccessToken.payload.aud).isEqualTo(clientProperties.clientId)
        assertThat(jwtRefreshedAccessToken.payload.authTime).isNull()
        assertThat(jwtRefreshedAccessToken.payload.azp).isNull()
        assertThat(jwtRefreshedAccessToken.payload.exp).isInstanceOf(String::class.java)
        assertThat(jwtRefreshedAccessToken.payload.iat).isInstanceOf(String::class.java)
        assertThat(jwtRefreshedAccessToken.payload.iss).isEqualTo("http://localhost:0")
        assertThat(jwtRefreshedAccessToken.payload.jti).isInstanceOf(String::class.java)
        assertThat(jwtRefreshedAccessToken.payload.nbf).isInstanceOf(String::class.java)
        assertThat(jwtRefreshedAccessToken.payload.roles).hasSameElementsAs(listOf(TEST_ROLE, TEST_PRIV))
        assertThat(jwtRefreshedAccessToken.payload.scope).hasSameElementsAs(
            clientProperties.scopes!!.filter { it != "profile" }.plus(
                listOf(TEST_ROLE, TEST_PRIV),
            ),
        )
        assertThat(jwtRefreshedAccessToken.payload.sid).isNull()
        assertThat(jwtRefreshedAccessToken.payload.sub).isEqualTo(TEST_EMAIL)

        val jwtIdToken = refreshResponse.decodeToken(refreshResponse.idToken!!, objectMapper)
        logger.debug(
            "id_token:\n{}",
            """
${Commons.writeJson(jwtIdToken!!.header, objectMapper)}
${Commons.writeJson(jwtIdToken.payload, objectMapper)}
""",
        )

        assertThat(jwtIdToken.header.alg).isEqualTo("RS256")
        assertThat(jwtIdToken.header.kid).isEqualTo(keyPairService.findNewest().id)

        assertThat(jwtIdToken.payload.aud).isEqualTo(clientProperties.clientId)
        assertThat(jwtIdToken.payload.authTime).isInstanceOf(java.lang.Long::class.java)
        assertThat(jwtIdToken.payload.azp).isEqualTo(clientProperties.clientId)
        assertThat(jwtIdToken.payload.exp).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.iat).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.iss).isEqualTo("http://localhost:0")
        assertThat(jwtIdToken.payload.jti).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.nbf).isNull()
        assertThat(jwtIdToken.payload.roles).isNull()
        assertThat(jwtIdToken.payload.scope).isNull()
        assertThat(jwtIdToken.payload.sid).isInstanceOf(String::class.java)
        assertThat(jwtIdToken.payload.sub).isEqualTo(TEST_EMAIL)

        webTestClient
            .get()
            .uri("/api/user")
            .headers { it.setBearerAuth(refreshResponse.accessToken) }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    // FIXME: https://stackoverflow.com/questions/75684139/spring-authorization-server-logout-via-postman/75689698#75689698
    @Disabled
    @Test
    fun `revoke an access token and test it's no longer valid`() {
        val accessResponse: AccessTokenResponse? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(AccessTokenResponse::class.java)
                .returnResult()
                .responseBody

        val introspectBody =
            "token=${accessResponse!!.accessToken}" +
                "&client_id=${clientProperties.clientId}" +
                "&client_secret=secret"

        var introspect: Introspect? =
            webTestClient
                .post()
                .uri("/oauth2/introspect")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(introspectBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Introspect::class.java)
                .returnResult()
                .responseBody

        assertThat(introspect).isNotNull
        assertThat(introspect!!.active).isTrue

        val revokeBody =
            "token=${accessResponse.accessToken}" +
                "&token_type_hint=access_token" +
                "&client_id=${clientProperties.clientId}" +
                "&client_secret=secret"

        webTestClient
            .post()
            .uri("/oauth2/revoke")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(revokeBody)
            .exchange()
            .expectStatus()
            .isOk

        introspect =
            webTestClient
                .post()
                .uri("/oauth2/introspect")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(introspectBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Introspect::class.java)
                .returnResult()
                .responseBody

        assertThat(introspect).isNotNull
        assertThat(introspect!!.active).isFalse

        webTestClient
            .get()
            .uri("/api/user")
            .headers { it.setBearerAuth(accessResponse.accessToken) }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `revoke a refresh token and test it's no longer valid`() {
        val accessResponse: AccessTokenResponse? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(AccessTokenResponse::class.java)
                .returnResult()
                .responseBody

        val introspectBody =
            "token=${accessResponse!!.refreshToken}" +
                "&client_id=${clientProperties.clientId}" +
                "&client_secret=secret"

        var introspect: Introspect? =
            webTestClient
                .post()
                .uri("/oauth2/introspect")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(introspectBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Introspect::class.java)
                .returnResult()
                .responseBody

        assertThat(introspect).isNotNull
        assertThat(introspect!!.active).isTrue

        val revokeBody =
            "token=${accessResponse.refreshToken}" +
                "&token_type_hint=refresh_token" +
                "&client_id=${clientProperties.clientId}" +
                "&client_secret=secret"

        webTestClient
            .post()
            .uri("/oauth2/revoke")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(revokeBody)
            .exchange()
            .expectStatus()
            .isOk

        introspect =
            webTestClient
                .post()
                .uri("/oauth2/introspect")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(introspectBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(Introspect::class.java)
                .returnResult()
                .responseBody

        assertThat(introspect).isNotNull
        assertThat(introspect!!.active).isFalse

        val refreshBody =
            "grant_type=refresh_token" +
                "&refresh_token=${accessResponse.refreshToken}" +
                "&client_id=${clientProperties.clientId}" +
                "&client_secret=secret"

        val refreshResponse: String? =
            webTestClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(refreshBody)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(String::class.java)
                .returnResult()
                .responseBody

        assertThat(refreshResponse).isEqualTo("{\"error\":\"invalid_grant\"}")
    }
}
