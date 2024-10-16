package com.authumn.authumn.web

import com.authumn.authumn.ClientAuthorizationProperties
import com.authumn.authumn.TEST_EMAIL
import com.authumn.authumn.TEST_PWD
import com.authumn.authumn.assertLoginPage
import com.authumn.authumn.commons.Commons
import com.authumn.authumn.mockUserDetailsService
import com.authumn.authumn.signIn
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlElement
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.util.UriComponentsBuilder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class LoginTest {
    @Autowired
    private lateinit var htmlClient: WebClient

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

    private lateinit var authRequest: String

    private val user = User(id = "1", email = TEST_EMAIL, password = TEST_PWD, roles = emptyList())

    @BeforeAll
    fun beforeAll() {
        authRequest =
            UriComponentsBuilder
                .fromPath("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientProperties.clientId)
                .queryParam("scope", clientProperties.scopes!!.filter { it != "profile" }.joinToString(" "))
                .queryParam("redirect_uri", clientProperties.redirectUris!!.first())
                .toUriString()
    }

    @BeforeEach
    fun beforeEach() {
        whenever(passwordEncoder.matches(anyString(), anyString())).thenReturn(true)

        mockUserDetailsService(userDetailsService)

        whenever(userDetailsService.loadUserByUsername("wrong@email.com")).thenThrow(UsernameNotFoundException(""))

        whenever(userService.findAll()).thenReturn(listOf(user))

        htmlClient.options.isThrowExceptionOnScriptError = false
        htmlClient.options.isThrowExceptionOnFailingStatusCode = true
        htmlClient.options.isRedirectEnabled = true
        htmlClient.cookieManager.clearCookies()
    }

    @Test
    fun whenLoginSuccessfulThenDisplayOk() {
        val page: HtmlPage = htmlClient.getPage("/")
        assertLoginPage(page)
        htmlClient.options.isThrowExceptionOnFailingStatusCode = false
        val signInResponse = signIn<Page>(page, TEST_EMAIL, TEST_PWD).webResponse
        assertThat(signInResponse.statusCode).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun whenLoginFailsThenDisplayBadCredentials() {
        val page: HtmlPage = htmlClient.getPage("/")
        val loginErrorPage: HtmlPage = signIn(page, "wrong@email.com", "wrong-password")
        val alert: HtmlElement = loginErrorPage.querySelector("div[role=\"alert\"]")
        assertThat(alert).isNotNull()
        assertThat(alert.textContent.trimIndent()).isEqualTo("Bad credentials")
    }

    @Test
    fun whenNotLoggedInAndRequestingTokenThenRedirectsToLogin() {
        val page: HtmlPage = htmlClient.getPage(authRequest)
        assertLoginPage(page)
    }

    @Test
    fun whenLoggingInAndRequestingTokenThenRedirectsToClientApplication() {
        htmlClient.options.isThrowExceptionOnFailingStatusCode = false
        htmlClient.options.isRedirectEnabled = false
        signIn<Page>(htmlClient.getPage("/login"), TEST_EMAIL, TEST_PWD)

        val response = htmlClient.getPage<Page>(authRequest).webResponse

        assertThat(response.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())
        val location = response.getResponseHeaderValue("location")
        assertThat(location).startsWith(clientProperties.redirectUris!!.first())
        assertThat(location).contains("code=")
    }

    @Test
    fun `when authorized using client auth flow then can call secured api`() {
        htmlClient.options.isThrowExceptionOnFailingStatusCode = false
        htmlClient.options.isRedirectEnabled = false
        signIn<Page>(htmlClient.getPage("/login"), TEST_EMAIL, TEST_PWD)

        val response = htmlClient.getPage<Page>("/api/user").webResponse
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(response.contentType).isEqualTo(MediaType.APPLICATION_JSON_VALUE)
        assertThat(response.contentAsString).isEqualTo(Commons.writeJson(listOf(user.toDto()), objectMapper))
    }
}
