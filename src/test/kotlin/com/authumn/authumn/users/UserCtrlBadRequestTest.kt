package com.authumn.authumn.users

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.commons.ErrorDto
import com.authumn.authumn.privileges.Privilege
import com.authumn.authumn.roles.Role
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

@WebMvcTest(controllers = [UserCtrl::class])
@WithMockKustomUser
class UserCtrlBadRequestTest {
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
                        label = "user",
                        isDefault = true,
                        privileges = listOf(Privilege(id = "3", label = "priv", isDefault = false)),
                    ),
                ),
        )

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build().applyCsrfFix(webApplicationContext)
    }

    @Test
    fun saveWithInvalidEmail() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPost.copy(email = "toto"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("email not valid")
        assertThat(result?.path).isEqualTo("/api/user")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithEmptyEmail() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPost.copy(email = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("email must not be blank. email must not be empty")
        assertThat(result?.path).isEqualTo("/api/user")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithBlankEmail() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPost.copy(email = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("email must not be blank. email not valid")
        assertThat(result?.path).isEqualTo("/api/user")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithEmptyPassword() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPost.copy(password = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("password must not be blank. password must not be empty")
        assertThat(result?.path).isEqualTo("/api/user")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithBlankPassword() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPost.copy(password = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-1")
        assertThat(result?.message).isEqualTo("password must not be blank")
        assertThat(result?.path).isEqualTo("/api/user")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun saveWithWrongJson() {
        val result: ErrorDto? =
            webTestClient
                .post()
                .uri("/api/user")
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
        assertThat(result?.path).isEqualTo("/api/user")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithEmptyPassword() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPut.copy(password = ""))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("password must not be blank. password must not be empty")
        assertThat(result?.path).isEqualTo("/api/user/${user.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithBlankPassword() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPut.copy(password = "   "))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("password must not be blank")
        assertThat(result?.path).isEqualTo("/api/user/${user.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateWithWrongJson() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/user/${user.id}")
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
        assertThat(result?.path).isEqualTo("/api/user/${user.id}")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun updateByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .put()
                .uri("/api/user/ ")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPut)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/user/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun findByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/user/ ")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/user/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun deleteByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .delete()
                .uri("/api/user/ ")
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/user/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }
}
