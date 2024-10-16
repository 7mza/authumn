package com.authumn.authumn.tokens

import com.authumn.authumn.AccessTokenResponse
import com.authumn.authumn.ClientCredentialsProperties
import com.authumn.authumn.commons.Commons
import com.authumn.authumn.keypairs.IKeyPairService
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class ClientCredentialsTokenTest {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var keyPairService: IKeyPairService

    @Autowired
    private lateinit var clientProperties: ClientCredentialsProperties

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var body: String

    @BeforeAll
    fun beforeAll() {
        keyPairService.save(keyPairService.generateJWKSet())
        keyPairService.save(keyPairService.generateJWKSet())
        keyPairService.save(keyPairService.generateJWKSet())

        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()

        body =
            "grant_type=client_credentials" +
            "&client_id=${clientProperties.clientId}" +
            "&client_secret=secret2&scope=${clientProperties.scopes!!.joinToString(" ")}"
    }

    @AfterAll
    fun afterAll() {
        keyPairService.deleteAll()
    }

    @Test
    fun `get access token using client credentials flow`() {
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
        assertThat(response.idToken).isNull()
        assertThat(response.refreshToken).isNull()
        assertThat(response.scope.split(" ")).hasSameElementsAs(clientProperties.scopes)
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
        assertThat(jwtAccessToken.payload.roles).hasSameElementsAs(clientProperties.scopes)
        assertThat(jwtAccessToken.payload.scope).hasSameElementsAs(clientProperties.scopes)
        assertThat(jwtAccessToken.payload.sid).isNull()
        assertThat(jwtAccessToken.payload.sub).isEqualTo(clientProperties.clientId)
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
}
