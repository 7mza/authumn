package com.authumn.authumn

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.commons.ErrorDto
import com.authumn.authumn.privileges.IPrivilegeService
import com.authumn.authumn.privileges.PrivilegeCtrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient

@WebMvcTest(controllers = [PrivilegeCtrl::class])
@WithMockKustomUser
class CtrlExceptionsTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: IPrivilegeService

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()
    }

    @Test
    fun `unhandled exception`() {
        whenever(service.findById(anyString())).thenThrow(RuntimeException("toto"))
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/privilege/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-500-0")
        assertThat(result?.message).isEqualTo("Unhandled Exception")
        assertThat(result?.path).isEqualTo("/api/privilege/1")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun customConstraintViolation() {
        whenever(service.findById(anyString())).thenThrow(CustomConstraintViolationException("toto"))
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/privilege/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-500-1")
        assertThat(result?.message).isEqualTo("toto")
        assertThat(result?.path).isEqualTo("/api/privilege/1")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun customResourceNotFoundException() {
        whenever(service.findById(anyString())).thenThrow(CustomResourceNotFoundException("toto"))
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/privilege/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-404-2")
        assertThat(result?.message).isEqualTo("toto")
        assertThat(result?.path).isEqualTo("/api/privilege/1")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun `resource not found`() {
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/blabla")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-404-1")
        assertThat(result?.message).isEqualTo("Resource not found")
        assertThat(result?.path).isEqualTo("/blabla")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }
}
