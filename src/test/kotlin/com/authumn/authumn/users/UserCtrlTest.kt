package com.authumn.authumn.users

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.privileges.Privilege
import com.authumn.authumn.roles.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
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
class UserCtrlTest {
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
    }

    @Test
    fun save() {
        whenever(service.save(userPost)).thenReturn(user)
        val result: UserGetDto? =
            webTestClient
                .post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPost)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(UserGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(user.toDto())
    }

    @Test
    fun findById() {
        whenever(service.findById("1")).thenReturn(user)
        val result: UserGetDto? =
            webTestClient
                .get()
                .uri("/api/user/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(UserGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(user.toDto())
    }

    @Test
    fun findAll() {
        val users = listOf(user)
        whenever(service.findAll()).thenReturn(users)
        val result: Collection<UserGetDto>? =
            webTestClient
                .get()
                .uri("/api/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(UserGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).hasSameElementsAs(users.map { it.toDto() })
    }

    @Test
    fun update() {
        whenever(service.update("1", userPut)).thenReturn(user)
        val result: UserGetDto? =
            webTestClient
                .put()
                .uri("/api/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userPut)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(UserGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(user.toDto())
    }

    @Test
    fun deleteById() {
        doNothing().whenever(service).deleteById("1")
        webTestClient
            .delete()
            .uri("/api/user/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }
}
