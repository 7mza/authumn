package com.authumn.authumn

import com.authumn.authumn.commons.Commons
import com.authumn.authumn.users.KustomUser
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.assertj.core.api.Assertions.assertThat
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.util.UriComponentsBuilder
import java.lang.annotation.Inherited
import java.util.Base64

const val TEST_ID = "a1889fed-e681-43ea-a3d5-5a56c40c4437"
const val TEST_EMAIL = "test@gmail.com"
const val TEST_PWD = "pwd123"
const val TEST_ROLE = "admin"
const val TEST_PRIV = "privilege1"

/*
* csrf workaround for WebTestClient + Servlet
* https://github.com/spring-projects/spring-security/issues/10841#issuecomment-1050659319
*/
fun WebTestClient.applyCsrfFix(webApplicationContext: WebApplicationContext): WebTestClient =
    MockMvcWebTestClient
        .bindToApplicationContext(webApplicationContext)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .defaultRequest(MockMvcRequestBuilders.get("/").with(SecurityMockMvcRequestPostProcessors.csrf()))
        .configureClient()
        .build()

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.authorizationserver.client.client-authorization.registration")
data class ClientAuthorizationProperties(
    var clientId: String?,
    var clientName: String?,
    var redirectUris: List<String>?,
    var postLogoutRedirectUris: List<String>?,
    var scopes: List<String>?,
)

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.authorizationserver.client.client-credentials.registration")
data class ClientCredentialsProperties(
    var clientId: String?,
    var clientName: String?,
    var scopes: List<String>?,
)

data class AccessTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("id_token")
    val idToken: String?,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    val scope: String,
    @JsonProperty("token_type")
    val tokenType: String,
) {
    fun decodeToken(
        token: String,
        objectMapper: ObjectMapper,
    ): JwtToken? {
        if (token.split(".").size == 3) {
            objectMapper.apply {
                enable(SerializationFeature.INDENT_OUTPUT)
                writerWithDefaultPrettyPrinter()
            }
            val chunks = token.split(".")
            val decoder = Base64.getUrlDecoder()
            val headerStr = String(decoder.decode(chunks[0]))
            val payloadStr = String(decoder.decode(chunks[1]))
            val header: Header = Commons.parseJson(headerStr, objectMapper)
            val payload: Payload = Commons.parseJson(payloadStr, objectMapper)
            return JwtToken(header = header, payload = payload)
        }
        return null
    }
}

data class Header(
    val alg: String,
    val kid: String,
)

data class Payload(
    val aud: String?,
    @JsonProperty("auth_time")
    val authTime: Long?,
    val azp: String?,
    val exp: String?,
    val iat: String?,
    val iss: String?,
    val jti: String?,
    val nbf: String?,
    val roles: List<String>?,
    val scope: List<String>?,
    val sid: String?,
    val sub: String?,
)

data class JwtToken(
    val header: Header,
    val payload: Payload,
)

data class Introspect(
    val active: Boolean,
)

inline fun <reified P : Page> signIn(
    page: HtmlPage,
    username: String,
    password: String,
): P {
    val usernameInput: HtmlInput = page.querySelector("input[name=\"username\"]")
    val passwordInput: HtmlInput = page.querySelector("input[name=\"password\"]")
    val signInButton: HtmlButton = page.querySelector("button")

    usernameInput.type(username)
    passwordInput.type(password)
    return signInButton.click()
}

fun assertLoginPage(page: HtmlPage) {
    assertThat(page.getUrl().toString()).endsWith("/login")

    val usernameInput: HtmlInput = page.querySelector("input[name=\"username\"]")
    val passwordInput: HtmlInput = page.querySelector("input[name=\"password\"]")
    val signInButton: HtmlButton = page.querySelector("button")

    assertThat(usernameInput).isNotNull()
    assertThat(passwordInput).isNotNull()
    assertThat(signInButton.textContent).isEqualTo("Sign in")
}

fun loginAndGetAuthorizationCode(
    htmlClient: WebClient,
    clientProperties: ClientAuthorizationProperties,
): String {
    val authRequest =
        UriComponentsBuilder
            .fromPath("/oauth2/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", clientProperties.clientId)
            .queryParam("scope", clientProperties.scopes!!.filter { it != "profile" }.joinToString(" "))
            .queryParam("redirect_uri", clientProperties.redirectUris!!.first())
            .toUriString()

    htmlClient.options.isThrowExceptionOnScriptError = false
    htmlClient.options.isThrowExceptionOnFailingStatusCode = false
    htmlClient.options.isRedirectEnabled = false
    htmlClient.cookieManager.clearCookies()

    signIn<Page>(htmlClient.getPage("/login"), TEST_EMAIL, TEST_PWD)
    val webResponse = htmlClient.getPage<Page>(authRequest).webResponse
    assertThat(webResponse.statusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value())

    val location = webResponse.getResponseHeaderValue("location")
    assertThat(location).startsWith(clientProperties.redirectUris!!.first())
    assertThat(location).contains("code=")

    val code =
        UriComponentsBuilder
            .fromUriString(location)
            .build()
            .queryParams["code"]!!
            .first()

    return code
}

fun mockUserDetailsService(userDetailsService: UserDetailsService) {
    whenever(userDetailsService.loadUserByUsername(anyString())).thenReturn(
        KustomUser(
            id = TEST_ID,
            username = TEST_EMAIL,
            password = TEST_PWD,
            authorities =
                listOf(
                    SimpleGrantedAuthority(TEST_ROLE),
                    SimpleGrantedAuthority(TEST_PRIV),
                ),
        ),
    )
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithMockKustomUserSecurityContextFactory::class)
annotation class WithMockKustomUser(
    val value: String = "user",
    val id: String = "",
    val username: String = "",
    val roles: Array<String> = ["USER"],
    val authorities: Array<String> = [],
    val password: String = "password",
    @get:AliasFor(annotation = WithSecurityContext::class) val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD,
)

internal class WithMockKustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockKustomUser> {
    private var securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()

    override fun createSecurityContext(withUser: WithMockKustomUser): SecurityContext {
        val username = if (StringUtils.hasLength(withUser.username)) withUser.username else withUser.value
        Assert.notNull(
            username,
        ) { "$withUser cannot have null username on both username and value properties" }
        val grantedAuthorities: MutableList<GrantedAuthority> = ArrayList()
        for (authority in withUser.authorities) {
            grantedAuthorities.add(SimpleGrantedAuthority(authority))
        }
        if (grantedAuthorities.isEmpty()) {
            for (role in withUser.roles) {
                Assert.isTrue(
                    !role.startsWith("ROLE_"),
                ) { "roles cannot start with ROLE_ Got $role" }
                grantedAuthorities.add(SimpleGrantedAuthority("ROLE_$role"))
            }
        } else {
            check(withUser.roles.size == 1 && "USER" == withUser.roles[0]) {
                (
                    "You cannot define roles attribute " + listOf(*withUser.roles) + " with authorities attribute " +
                        listOf(
                            *withUser.authorities,
                        )
                )
            }
        }
        val principal =
            KustomUser(
                withUser.id,
                username,
                withUser.password,
                enabled = true,
                accountNonExpired = true,
                credentialsNonExpired = true,
                accountNonLocked = true,
                authorities = grantedAuthorities,
            )
        val authentication: Authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                principal,
                principal.password,
                principal.authorities,
            )
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = authentication
        return context
    }

    @Autowired(required = false)
    fun setSecurityContextHolderStrategy(securityContextHolderStrategy: SecurityContextHolderStrategy) {
        this.securityContextHolderStrategy = securityContextHolderStrategy
    }
}
