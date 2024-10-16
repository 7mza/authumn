package com.authumn.authumn.keypairs

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [KeyPairPairCtrl::class])
@WithMockKustomUser
class KeyPairCtrlTest {
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
    }

    @Test
    fun generate() {
        whenever(service.save(anyOrNull())).thenReturn(keypair)
        val result: KeyPairGetDto? =
            webTestClient
                .post()
                .uri("/api/keypair")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(KeyPairGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(keypair.toDto(textEncryptor))
    }

    @Test
    fun findById() {
        whenever(service.findById("1")).thenReturn(keypair)
        val result: KeyPairGetDto? =
            webTestClient
                .get()
                .uri("/api/keypair/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody(KeyPairGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(keypair.toDto(textEncryptor))
    }

    @Test
    fun findAllByOrderByCreatedAtDesc() {
        whenever(service.findAllByOrderByCreatedAtDesc()).thenReturn(listOf(keypair, keypair))
        val result: Collection<KeyPairGetDto>? =
            webTestClient
                .get()
                .uri("/api/keypair")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectBodyList(KeyPairGetDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result).isEqualTo(listOf(keypair, keypair).map { it.toDto(textEncryptor) })
    }

    @Test
    fun deleteById() {
        doNothing().whenever(service).deleteById("1")
        webTestClient
            .delete()
            .uri("/api/keypair/1")
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun deleteAllButNewest() {
        doNothing().whenever(service).deleteAllButNewest()
        webTestClient
            .delete()
            .uri("/api/keypair")
            .exchange()
            .expectStatus()
            .isNoContent
    }
}
