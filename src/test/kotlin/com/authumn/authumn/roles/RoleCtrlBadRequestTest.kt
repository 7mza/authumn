package com.authumn.authumn.roles

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.commons.ErrorDto
import com.authumn.authumn.privileges.Privilege
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

@WebMvcTest(controllers = [RoleCtrl::class])
@WithMockKustomUser
class RoleCtrlBadRequestTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: IRoleService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private val rolePost = RolePostDto(label = "role", isDefault = false, privileges = listOf("1"))

    private val rolePut = RolePutDto(label = "role", isDefault = false, privileges = listOf("1"))

    private val role =
        Role(
            id = "2",
            label = "role",
            isDefault = true,
            privileges = listOf(Privilege(id = "3", label = "priv", isDefault = false)),
        )

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build().applyCsrfFix(webApplicationContext)
    }

    @Test
    fun saveWithEmptyLabel() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/role")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePost.copy(label = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(result?.path).isEqualTo("/api/role")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithBlankLabel() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/role")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePost.copy(label = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("label must not be blank")
        assertThat(result?.path).isEqualTo("/api/role")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithWrongJson() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/role")
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
        assertThat(result?.path).isEqualTo("/api/role")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithEmptyLabel() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/role/${role.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePut.copy(label = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(result?.path).isEqualTo("/api/role/${role.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithBlankLabel() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/role/${role.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePut.copy(label = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("label must not be blank")
        assertThat(result?.path).isEqualTo("/api/role/${role.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithWrongJson() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/role/${role.id}")
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
        assertThat(result?.path).isEqualTo("/api/role/${role.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/role/ ")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePut)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/role/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun findByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/role/ ")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/role/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun deleteByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .delete()
                .uri("/api/role/ ")
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/role/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }
}
