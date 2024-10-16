package com.authumn.authumn.web

import com.authumn.authumn.AccessTokenResponse
import com.authumn.authumn.ClientAuthorizationProperties
import com.authumn.authumn.TEST_EMAIL
import com.authumn.authumn.TEST_PWD
import com.authumn.authumn.commons.Commons
import com.authumn.authumn.keypairs.IKeyPairService
import com.authumn.authumn.loginAndGetAuthorizationCode
import com.authumn.authumn.mockUserDetailsService
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.util.UriComponentsBuilder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RpInitiatedLogoutTest {
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

    @MockBean
    private lateinit var userService: IUserService

    private lateinit var body: String

    private val user = User(id = "1", email = TEST_EMAIL, password = TEST_PWD, roles = emptyList())

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()

        whenever(passwordEncoder.matches(anyString(), anyString())).thenReturn(true)

        mockUserDetailsService(userDetailsService)

        whenever(userService.findAll()).thenReturn(listOf(user))

        keyPairService.save(keyPairService.generateJWKSet())

        body =
            "grant_type=authorization_code" +
            "&redirect_uri=${clientProperties.redirectUris!!.first()}" +
            "&client_id=${clientProperties.clientId}" +
            "&client_secret=secret" +
            "&scope=${clientProperties.scopes}" +
            "&code=${loginAndGetAuthorizationCode(htmlClient, clientProperties)}"

        val webResponse = htmlClient.getPage<Page>("/api/user").webResponse
        assertThat(webResponse.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(webResponse.contentType).isEqualTo(MediaType.APPLICATION_JSON_VALUE)
        assertThat(webResponse.contentAsString).isEqualTo(Commons.writeJson(listOf(user.toDto()), objectMapper))
    }

    @AfterEach
    fun afterEach() {
        keyPairService.deleteAll()
    }

    @Test
    fun `GET rp initiated logout`() {
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

        val logoutQuery =
            UriComponentsBuilder
                .fromPath("/connect/logout")
                .queryParam("id_token_hint", response!!.idToken)
                .queryParam("logout_hint", TEST_EMAIL)
                .queryParam("client_id", clientProperties.clientId)
                .queryParam("post_logout_redirect_uri", clientProperties.postLogoutRedirectUris!!.first())
                .queryParam("state", "blabla")
                .queryParam("ui_locales", "EN")
                .toUriString()

        var webResponse = htmlClient.getPage<Page>(logoutQuery).webResponse
        assertThat(webResponse.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())
        var location = webResponse.getResponseHeaderValue("location")
        assertThat(location).startsWith(clientProperties.postLogoutRedirectUris!!.first())
        assertThat(location).contains("state=blabla")

        webResponse = htmlClient.getPage<Page>("/api/user").webResponse
        assertThat(webResponse.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())
        location = webResponse.getResponseHeaderValue("location")
        assertThat(location).endsWith("/login")
    }

    // FIXME
    @Disabled
    @Test
    fun `POST rp initiated logout`() {
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

        val logoutBody =
            "id_token_hint=${response!!.idToken}" +
                "&logout_hint=$TEST_EMAIL" +
                "&client_id=${clientProperties.clientId}" +
                "&post_logout_redirect_uri=${clientProperties.postLogoutRedirectUris!!.first()}" +
                "&state=blabla" +
                "&ui_locales=EN"

        webTestClient
            .post()
            .uri("/connect/logout")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(logoutBody)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isFound
            .expectHeader()
            .location("${clientProperties.postLogoutRedirectUris!!.first()}?state=blabla")

        val webResponse = htmlClient.getPage<Page>("/api/user").webResponse
        assertThat(webResponse.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())
        val location = webResponse.getResponseHeaderValue("location")
        assertThat(location).endsWith("/login")

        webTestClient
            .get()
            .uri("/api/user")
            .headers { it.setBearerAuth(response.accessToken) }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }
}
