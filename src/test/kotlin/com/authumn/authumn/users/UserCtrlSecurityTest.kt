package com.authumn.authumn.users

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.commons.ErrorDto
import com.authumn.authumn.privileges.Privilege
import com.authumn.authumn.roles.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserCtrlSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: IUserService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val userPost = UserPostDto(email = "aa@aa.com", password = "pwd", roles = listOf("1"))

    private val userPut = UserPutDto(email = "aa@aa.com", password = "pwd", roles = listOf("1"))

    private val user =
        User(
            id = "1",
            email = "aa@aa.com",
            password = "pwd",
            roles =
                listOf(
                    Role(
                        id = "2",
                        label = "role",
                        isDefault = true,
                        privileges = listOf(Privilege(id = "3", label = "priv", isDefault = false)),
                    ),
                ),
        )

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build().applyCsrfFix(webApplicationContext)

        whenever(service.save(any())).thenReturn(user)
        whenever(service.findById(anyString())).thenReturn(user)
        whenever(service.findAll()).thenReturn(listOf(user))
        whenever(service.update(anyString(), any())).thenReturn(user)
        doNothing().whenever(service).deleteById(anyString())
    }

    @Test
    @WithAnonymousUser
    fun `save don't require auth or csrf`() {
        MockMvcWebTestClient
            .bindTo(mockMvc)
            .build()
            .post()
            .uri("/api/user")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userPost)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithAnonymousUser
    fun `findById require auth`() {
        webTestClient
            .get()
            .uri("/api/user/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(id = "-1", username = "bb@bb.com", authorities = ["admin"])
    fun `authority admin can call any findById`() {
        webTestClient
            .get()
            .uri("/api/user/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(id = "1", username = "aa@aa.com", authorities = ["user"])
    fun `authority user can call findById on it self`() {
        webTestClient
            .get()
            .uri("/api/user/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(id = "-1", username = "bb@bb.com", authorities = ["user"])
    fun `authority user can't call findById on other users`() {
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/user/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-403-1")
        assertThat(result?.message).isEqualTo("Forbidden")
        assertThat(result?.path).isEqualTo("/api/user/1")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    @WithAnonymousUser
    fun `findAll require auth`() {
        webTestClient
            .get()
            .uri("/api/user")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call findAll`() {
        webTestClient
            .get()
            .uri("/api/user")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call findAll`() {
        webTestClient
            .get()
            .uri("/api/user")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun `update require auth`() {
        webTestClient
            .put()
            .uri("/api/user/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userPut)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call update`() {
        webTestClient
            .put()
            .uri("/api/user/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userPut)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(id = "1", username = "aa@aa.com", authorities = ["user"])
    fun `authority user can call update on itself`() {
        webTestClient
            .put()
            .uri("/api/user/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userPut)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(id = "-1", username = "bb@bb.com", authorities = ["user"])
    fun `authority user can't call update on others`() {
        webTestClient
            .put()
            .uri("/api/user/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userPut)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun `deleteById require auth`() {
        webTestClient
            .delete()
            .uri("/api/user/1")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(id = "-1", username = "bb@bb.com", authorities = ["admin"])
    fun `authority admin can call any deleteById`() {
        webTestClient
            .delete()
            .uri("/api/user/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    @WithMockKustomUser(id = "1", username = "aa@aa.com", authorities = ["user"])
    fun `authority user can call deleteById on it self`() {
        webTestClient
            .delete()
            .uri("/api/user/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    @WithMockKustomUser(id = "-1", username = "bb@bb.com", authorities = ["user"])
    fun `authority user can't call deleteById on other users`() {
        val result: ErrorDto? =
            webTestClient
                .delete()
                .uri("/api/user/1")
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-403-1")
        assertThat(result?.message).isEqualTo("Forbidden")
        assertThat(result?.path).isEqualTo("/api/user/1")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }
}
