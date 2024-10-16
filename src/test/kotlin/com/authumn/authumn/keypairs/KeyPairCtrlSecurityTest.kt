package com.authumn.authumn.keypairs

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class KeyPairCtrlSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: IKeyPairService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @MockBean
    private lateinit var textEncryptor: TextEncryptor

    private lateinit var keypair: KeyPair

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build().applyCsrfFix(webApplicationContext)

        whenever(textEncryptor.encrypt("pub")).thenReturn("bup")
        whenever(textEncryptor.decrypt("bup")).thenReturn("pub")
        whenever(textEncryptor.encrypt("pem")).thenReturn("mep")
        whenever(textEncryptor.decrypt("mep")).thenReturn("pem")

        keypair = KeyPair(id = "1", publicKey = textEncryptor.encrypt("pub"), privateKey = textEncryptor.encrypt("pem"))

        whenever(service.save(anyOrNull())).thenReturn(keypair)
        whenever(service.findById(anyString())).thenReturn(keypair)
        whenever(service.findAllByOrderByCreatedAtDesc()).thenReturn(listOf(keypair, keypair))
        doNothing().whenever(service).deleteById(anyString())
        doNothing().whenever(service).deleteAllButNewest()
    }

    @Test
    @WithAnonymousUser
    fun `generate require auth`() {
        webTestClient
            .post()
            .uri("/api/keypair")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call generate`() {
        webTestClient
            .post()
            .uri("/api/keypair")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call generate`() {
        webTestClient
            .post()
            .uri("/api/keypair")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun `findById require auth`() {
        webTestClient
            .get()
            .uri("/api/keypair/1")
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
            .uri("/api/keypair/1")
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
            .uri("/api/keypair/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun `findAllByOrderByCreatedAtDesc require auth`() {
        webTestClient
            .get()
            .uri("/api/keypair")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call findAllByOrderByCreatedAtDesc`() {
        webTestClient
            .get()
            .uri("/api/keypair")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call findAllByOrderByCreatedAtDesc`() {
        webTestClient
            .get()
            .uri("/api/keypair")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun deleteById() {
        webTestClient
            .delete()
            .uri("/api/keypair/1")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call deleteById`() {
        webTestClient
            .delete()
            .uri("/api/keypair/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call deleteById`() {
        webTestClient
            .delete()
            .uri("/api/keypair/1")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithAnonymousUser
    fun `deleteAllButNewest require auth`() {
        webTestClient
            .delete()
            .uri("/api/keypair")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithMockKustomUser(authorities = ["admin"])
    fun `authority admin can call deleteAllButNewest`() {
        webTestClient
            .delete()
            .uri("/api/keypair")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    @WithMockKustomUser(authorities = ["user"])
    fun `authority user can't call deleteAllButNewest`() {
        webTestClient
            .delete()
            .uri("/api/keypair")
            .exchange()
            .expectStatus()
            .isForbidden
    }
}
