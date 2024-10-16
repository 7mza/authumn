package com.authumn.authumn.privileges

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
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

@WebMvcTest(controllers = [PrivilegeCtrl::class])
@WithMockKustomUser
class PrivilegeCtrlTest {
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
    fun save() {
        whenever(service.save(privilegePost)).thenReturn(privilege)
        val result: PrivilegeGetDto? =
            webTestClient
                .post()
                .uri("/api/privilege")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePost)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PrivilegeGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(privilege.toDto())
    }

    @Test
    fun findById() {
        whenever(service.findById("1")).thenReturn(privilege)
        val result: PrivilegeGetDto? =
            webTestClient
                .get()
                .uri("/api/privilege/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PrivilegeGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(privilege.toDto())
    }

    @Test
    fun findAll() {
        val privileges = listOf(privilege)
        whenever(service.findAll()).thenReturn(privileges)
        val result: Collection<PrivilegeGetDto>? =
            webTestClient
                .get()
                .uri("/api/privilege")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(PrivilegeGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).hasSameElementsAs(privileges.map { it.toDto() })
    }

    @Test
    fun update() {
        whenever(service.update("1", privilegePut)).thenReturn(privilege)
        val result: PrivilegeGetDto? =
            webTestClient
                .put()
                .uri("/api/privilege/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(privilegePut)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(PrivilegeGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(privilege.toDto())
    }

    @Test
    fun deleteById() {
        doNothing().whenever(service).deleteById("1")
        webTestClient
            .delete()
            .uri("/api/privilege/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }
}
