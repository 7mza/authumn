package com.authumn.authumn.roles

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.privileges.Privilege
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

@WebMvcTest(controllers = [RoleCtrl::class])
@WithMockKustomUser
class RoleCtrlTest {
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
    fun save() {
        whenever(service.save(rolePost)).thenReturn(role)
        val result: RoleGetDto? =
            webTestClient
                .post()
                .uri("/api/role")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePost)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(RoleGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(role.toDto())
    }

    @Test
    fun findById() {
        whenever(service.findById("1")).thenReturn(role)
        val result: RoleGetDto? =
            webTestClient
                .get()
                .uri("/api/role/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(RoleGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(role.toDto())
    }

    @Test
    fun findAll() {
        val roles = listOf(role)
        whenever(service.findAll()).thenReturn(roles)
        val result: Collection<RoleGetDto>? =
            webTestClient
                .get()
                .uri("/api/role")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(RoleGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).hasSameElementsAs(roles.map { it.toDto() })
    }

    @Test
    fun update() {
        whenever(service.update("1", rolePut)).thenReturn(role)
        val result: RoleGetDto? =
            webTestClient
                .put()
                .uri("/api/role/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rolePut)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(RoleGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(role.toDto())
    }

    @Test
    fun deleteById() {
        doNothing().whenever(service).deleteById("1")
        webTestClient
            .delete()
            .uri("/api/role/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }
}
