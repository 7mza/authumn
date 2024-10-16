package com.authumn.authumn.keypairs

import com.authumn.authumn.WithMockKustomUser
import com.authumn.authumn.applyCsrfFix
import com.authumn.authumn.commons.ErrorDto
import com.authumn.authumn.confs.DefaultConfs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [KeyPairPairCtrl::class])
@WithMockKustomUser
@Import(DefaultConfs::class)
class KeyPairCtrlBadRequestTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var service: IKeyPairService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun beforeEach() {
        this.webTestClient = MockMvcWebTestClient.bindTo(mockMvc).build().applyCsrfFix(webApplicationContext)
    }

    @Test
    fun findByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .get()
                .uri("/api/keypair/ ")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/keypair/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }

    @Test
    fun deleteByBlankId() {
        val result: ErrorDto? =
            webTestClient
                .delete()
                .uri("/api/keypair/ ")
                .exchange()
                .expectStatus()
                .isBadRequest
                .expectBody(ErrorDto::class.java)
                .returnResult()
                .responseBody
        assertThat(result?.code).isEqualTo("as-400-2")
        assertThat(result?.message).isEqualTo("id must not be blank")
        assertThat(result?.path).isEqualTo("/api/keypair/%20")
        assertThat(result?.timestamp).isNotNull()
        assertThat(result?.requestId).isEqualTo("N/A")
    }
}
