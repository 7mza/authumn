package com.authumn.authumn.keypairs

import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.nimbusds.jose.jwk.RSAKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.encrypt.TextEncryptor
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeyPairServiceTest {
    @Autowired
    private lateinit var service: IKeyPairService

    @Autowired
    private lateinit var repository: IKeyPairRepository

    @Autowired
    private lateinit var textEncryptor: TextEncryptor

    @Autowired
    private lateinit var publicKeyConverter: PublicKeyConverter

    @Autowired
    private lateinit var privateKeyConverter: PrivateKeyConverter

    @AfterEach
    fun afterEach() {
        repository.deleteAll()
    }

    @Test
    fun save() {
        val pair = service.generateJWKSet()
        val saved = service.save(pair)
        assertThat(
            saved.toRSAKey(
                publicKeyConverter = publicKeyConverter,
                privateKeyConverter = privateKeyConverter,
            ),
        ).isEqualTo(pair)
        val encPair = repository.findById(pair.keyID).getOrNull()
        assertThat(encPair).isNotNull
        assertThat(encPair!!.id).isEqualTo(pair.keyID)
        assertThat(encPair.publicKey).isInstanceOf(String::class.java)
        val pub = textEncryptor.decrypt(encPair.publicKey)
        assertThat(pub).startsWith("-----BEGIN PUBLIC KEY-----")
        assertThat(pub).endsWith("-----END PUBLIC KEY-----")
        assertThat(encPair.privateKey).isInstanceOf(String::class.java)
        val pem = textEncryptor.decrypt(encPair.privateKey)
        assertThat(pem).startsWith("-----BEGIN PRIVATE KEY-----")
        assertThat(pem).endsWith("-----END PRIVATE KEY-----")
        assertThat(encPair.createdAt).isBefore(Instant.now())
        assertThat(encPair.updateAt).isBefore(Instant.now())
    }

    @Test
    fun findNewest() {
        val pairs = (1..3).map { service.generateJWKSet() }
        val saved = pairs.map { service.save(it) }.sortedByDescending { it.createdAt }
        val found = service.findNewest()
        assertThat(found).isEqualTo(saved.first())
    }

    @Test
    fun findById() {
        val pair = service.generateJWKSet()
        val saved = service.save(pair)
        val found = service.findById(pair.keyID)
        assertThat(found).isEqualTo(saved)
    }

    @Test
    fun `find by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.findById("1")
            }
        assertThat(ex.message).isEqualTo("KeyPair with id: 1 not found")
    }

    @Test
    fun findAllByOrderByCreatedAtDesc() {
        val pairs = (1..3).map { service.generateJWKSet() }
        val saved = pairs.map { service.save(it) }.sortedByDescending { it.createdAt }
        val found = service.findAllByOrderByCreatedAtDesc()
        assertThat(found).isEqualTo(saved)
    }

    @Test
    fun count() {
        (1..3).map { service.generateJWKSet() }.map { service.save(it) }
        assertThat(service.count()).isEqualTo(3)
    }

    @Test
    fun generateJWKSet() {
        try {
            val pair = service.generateJWKSet()
            assertThat(pair).isInstanceOf(RSAKey::class.java)
            assertThat(pair.keyID).isInstanceOf(String::class.java)
            assertThat(pair.toRSAPublicKey()).isInstanceOf(RSAPublicKey::class.java)
            assertThat(pair.toRSAPrivateKey()).isInstanceOf(RSAPrivateKey::class.java)
        } catch (_: Throwable) {
            fail("error in generateJWKSet")
        }
    }

    @Test
    fun deleteById() {
        val pair = service.generateJWKSet()
        service.save(pair)
        service.deleteById(pair.keyID)
        assertThat(repository.count()).isZero
    }

    @Test
    fun `delete by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.deleteById("1")
            }
        assertThat(ex.message).isEqualTo("KeyPair with id: 1 not found")
    }

    @Test
    fun deleteAllButNewest() {
        val saved = (1..3).map { service.generateJWKSet() }.map { service.save(it) }.sortedBy { it.createdAt }
        assertThat(repository.count()).isEqualTo(3)
        service.deleteAllButNewest()
        assertThat(repository.count()).isEqualTo(1)
        assertThat(repository.findAll().first()).isEqualTo(saved.last())
    }
}
