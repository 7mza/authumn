package com.authumn.authumn

import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.User
import com.authumn.authumn.users.UserGetDto
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AllowedSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var userService: IUserService

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build()
    }

    @Test
    fun `post user don't require auth`() {
        val user = User(id = "1", email = "aa@aa.aa", password = "pwd", roles = emptyList())
        whenever(userService.save(any())).thenReturn(user)
        val response: UserGetDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    {
                      "email": "user@email.com",
                      "password": "password",
                      "roles": []
                    }
                    """.trimIndent(),
                ).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(UserGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(response).isEqualTo(user.toDto())
    }

    @Test
    fun `actuator health don't require auth`() {
        val result: String? =
            webTestClient
                .get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(String::class.java)
                .returnResult()
                .responseBody
        assertThat(JsonPath.parse(result).read<String>("$.status")).isEqualTo("UP")
    }

    @Test
    fun `swagger-ui don't require auth`() {
        webTestClient
            .get()
            .uri("/swagger-ui/index.html")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `non allowed endpoints require auth`() {
        webTestClient
            .get()
            .uri("/other")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }
}
