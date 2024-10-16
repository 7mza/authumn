package com.authumn.authumn.privileges

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.commons.ErrorDto
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
class PrivilegeCtrlSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: IPrivilegeService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val privilegePost = PrivilegePostDto(label = "priv", isDefault = false)

    private val privilegePut = PrivilegePutDto(label = "priv", isDefault = false)

    private val privilege = Privilege(id = "3", label = "priv", isDefault = false)

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build().applyCsrfFix(webApplicationContext)

        whenever(service.save(any())).thenReturn(privilege)
        whenever(service.findById(anyString())).thenReturn(privilege)
        whenever(service.findAll()).thenReturn(listOf(privilege))
        whenever(service.update(anyString(), any())).thenReturn(privilege)
        doNothing().whenever(service).deleteById(anyString())
    }

    @Test
    @WithAnonymousUser
    fun `save require auth`() {
        webTestClient
            .post()
            .uri("/api/privilege")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(privilegePost)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call save`() {
        webTestClient
            .post()
            .uri("/api/privilege")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(privilegePost)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call save`() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/privilege")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePost)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isForbidden
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-403-1")
        assertThat(result?.message).isEqualTo("Forbidden")
        assertThat(result?.path).isEqualTo("/api/privilege")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    @WithAnonymousUser
    fun `findById require auth`() {
        webTestClient
            .get()
            .uri("/api/privilege/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call findById`() {
        webTestClient
            .get()
            .uri("/api/privilege/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call findById`() {
        webTestClient
            .get()
            .uri("/api/privilege/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun `findAll require auth`() {
        webTestClient
            .get()
            .uri("/api/privilege")
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
            .uri("/api/privilege")
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
            .uri("/api/privilege")
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
            .uri("/api/privilege/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(privilegePut)
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
            .uri("/api/privilege/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(privilegePut)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call update`() {
        webTestClient
            .put()
            .uri("/api/privilege/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(privilegePut)
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
            .uri("/api/privilege/1")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call any deleteById`() {
        webTestClient
            .delete()
            .uri("/api/privilege/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call deleteById`() {
        webTestClient
            .delete()
            .uri("/api/privilege/1")
            .exchange()
            .expectStatus()
            .isForbidden
    }
}
