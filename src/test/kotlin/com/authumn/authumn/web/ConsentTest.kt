package com.authumn.authumn.web

import com.authumn.authumn.ClientAuthorizationProperties
import com.authumn.authumn.WithMockKustomUser
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.DomElement
import com.gargoylesoftware.htmlunit.html.DomNode
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.mockito.Mockito.anyString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.web.util.UriComponentsBuilder
import java.util.function.Consumer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockKustomUser
@TestInstance(Lifecycle.PER_CLASS)
class ConsentTest {
    @Autowired
    private lateinit var htmlClient: WebClient

    @Autowired
    private lateinit var clientProperties: ClientAuthorizationProperties

    @MockBean
    private lateinit var authorizationConsentService: OAuth2AuthorizationConsentService

    private lateinit var authorizationRequestUri: String

    @BeforeAll
    fun beforeAll() {
        authorizationRequestUri =
            UriComponentsBuilder
                .fromPath("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientProperties.clientId)
                .queryParam("scope", clientProperties.scopes!!.joinToString(" "))
                .queryParam("redirect_uri", clientProperties.redirectUris!!.first())
                .toUriString()
    }

    @BeforeEach
    fun beforeEach() {
        whenever(authorizationConsentService.findById(anyString(), anyString())).thenReturn(null)

        htmlClient.options.isThrowExceptionOnFailingStatusCode = false
        htmlClient.options.isRedirectEnabled = true
        htmlClient.cookieManager.clearCookies()
    }

    @Test
    fun whenUserConsentsToAllScopesThenReturnAuthorizationCode() {
        val consentPage = htmlClient.getPage<HtmlPage>(authorizationRequestUri)
        assertThat(consentPage.titleText).isEqualTo("Consent required")

        val scopes: MutableList<HtmlCheckBoxInput> = ArrayList()
        consentPage
            .querySelectorAll("input[name='scope']")
            .forEach(Consumer { scope: DomNode -> scopes.add(scope as HtmlCheckBoxInput) })
        for (scope in scopes) {
            scope.click<Page>()
        }

        val scopeIds: MutableList<String> = ArrayList()
        scopes.forEach(
            Consumer { scope: HtmlCheckBoxInput ->
                assertThat(scope.isChecked).isTrue()
                scopeIds.add(scope.id)
            },
        )
        assertThat(scopeIds).containsExactlyInAnyOrder("profile")

        val submitConsentButton = consentPage.querySelector<DomElement>("button[id='submit-consent']")
        htmlClient.options.isRedirectEnabled = false

        val approveConsentResponse = submitConsentButton.click<Page>().webResponse
        assertThat(approveConsentResponse.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())
        val location = approveConsentResponse.getResponseHeaderValue("location")
        assertThat(location).startsWith(clientProperties.redirectUris!!.first())
        assertThat(location).contains("code=")
    }

    @Test
    fun whenUserCancelsConsentThenReturnAccessDeniedError() {
        val consentPage = htmlClient.getPage<HtmlPage>(authorizationRequestUri)
        assertThat(consentPage.titleText).isEqualTo("Consent required")

        val cancelConsentButton = consentPage.querySelector<DomElement>("button[id='cancel-consent']")
        htmlClient.options.isRedirectEnabled = false

        val cancelConsentResponse = cancelConsentButton.click<Page>().webResponse
        assertThat(cancelConsentResponse.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())
        val location = cancelConsentResponse.getResponseHeaderValue("location")
        assertThat(location).startsWith(clientProperties.redirectUris!!.first())
        assertThat(location).contains("error=access_denied")
    }
}
