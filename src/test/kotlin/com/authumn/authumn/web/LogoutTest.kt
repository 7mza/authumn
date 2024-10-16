package com.authumn.authumn.web

import com.authumn.authumn.TEST_EMAIL
import com.authumn.authumn.TEST_PWD
import com.authumn.authumn.commons.Commons
import com.authumn.authumn.mockUserDetailsService
import com.authumn.authumn.signIn
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.User
import com.fasterxml.jackson.databind.ObjectMapper
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlElement
import com.gargoylesoftware.htmlunit.html.HtmlForm
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class LogoutTest {
    @Autowired
    private lateinit var htmlClient: WebClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userDetailsService: UserDetailsService

    @MockBean
    private lateinit var passwordEncoder: PasswordEncoder

    @MockBean
    private lateinit var userService: IUserService

    private val user = User(id = "1", email = TEST_EMAIL, password = TEST_PWD, roles = emptyList())

    @BeforeEach
    fun beforeEach() {
        whenever(passwordEncoder.matches(anyString(), anyString())).thenReturn(true)

        mockUserDetailsService(userDetailsService)

        whenever(userDetailsService.loadUserByUsername("wrong@email.com")).thenThrow(UsernameNotFoundException(""))

        whenever(userService.findAll()).thenReturn(listOf(user))

        htmlClient.cookieManager.clearCookies()
        htmlClient.options.isThrowExceptionOnScriptError = false
        htmlClient.options.isThrowExceptionOnFailingStatusCode = false
        htmlClient.options.isRedirectEnabled = true

        signIn<Page>(htmlClient.getPage("/login"), TEST_EMAIL, TEST_PWD)

        val response = htmlClient.getPage<Page>("/api/user").webResponse
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(response.contentType).isEqualTo(MediaType.APPLICATION_JSON_VALUE)
        assertThat(response.contentAsString).isEqualTo(Commons.writeJson(listOf(user.toDto()), objectMapper))
    }

    @Test
    fun `test logout`() {
        val logoutPage: HtmlPage = htmlClient.getPage("/logout")

        assertThat(logoutPage.webResponse.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(logoutPage.webResponse.contentType).isEqualTo(MediaType.TEXT_HTML_VALUE)

        val form: HtmlElement = logoutPage.querySelector("form")
        assertThat(form).isInstanceOf(HtmlForm::class.java)
        assertThat((form as HtmlForm).methodAttribute).isEqualTo("post")
        assertThat(form.actionAttribute).isEqualTo("/logout")

        val button: HtmlElement = form.querySelector("button")
        assertThat(button).isInstanceOf(HtmlButton::class.java)

        val loginPage: HtmlPage = (button as HtmlButton).click()
        assertThat(loginPage.webResponse.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(loginPage.webResponse.contentType).isEqualTo(MediaType.TEXT_HTML_VALUE)
        assertThat(loginPage.url.toString()).endsWith("/login?logout")

        val alert: HtmlElement = loginPage.querySelector("div[role=\"alert\"]")
        assertThat(alert).isNotNull()
        assertThat(alert.textContent.trimIndent()).isEqualTo("You have been signed out")

        val apiResponse: HtmlPage = htmlClient.getPage("/api/user")
        assertThat(apiResponse.webResponse.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(apiResponse.webResponse.contentType).isEqualTo(MediaType.TEXT_HTML_VALUE)
        assertThat(apiResponse.url.path).isEqualTo("/login")
    }
}
