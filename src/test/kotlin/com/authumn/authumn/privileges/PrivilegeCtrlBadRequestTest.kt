package com.authumn.authumn.privileges

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.commons.ErrorDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [PrivilegeCtrl::class])
@WithMockKustomUser
class PrivilegeCtrlBadRequestTest {
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
    }

    @Test
    fun saveWithEmptyLabel() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/privilege")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePost.copy(label = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(result?.path).isEqualTo("/api/privilege")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithBlankLabel() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/privilege")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePost.copy(label = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("label must not be blank")
        assertThat(result?.path).isEqualTo("/api/privilege")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithWrongJson() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/privilege")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-3")
        assertThat(result?.message).startsWith("Bad request:")
        assertThat(result?.path).isEqualTo("/api/privilege")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithEmptyLabel() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/privilege/${privilege.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePut.copy(label = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(result?.path).isEqualTo("/api/privilege/${privilege.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithBlankLabel() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/privilege/${privilege.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePut.copy(label = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("label must not be blank")
        assertThat(result?.path).isEqualTo("/api/privilege/${privilege.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithWrongJson() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/privilege/${privilege.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-3")
        assertThat(result?.message).startsWith("Bad request:")
        assertThat(result?.path).isEqualTo("/api/privilege/${privilege.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/privilege/ ")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePut)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/privilege/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun findByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/privilege/ ")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/privilege/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun deleteByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .delete()
                .uri("/api/privilege/ ")
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/privilege/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }
}
