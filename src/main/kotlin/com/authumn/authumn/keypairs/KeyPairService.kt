package com.authumn.authumn.keypairs

import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.nimbusds.jose.jwk.RSAKey
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.serializer.Deserializer
import org.springframework.core.serializer.Serializer
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID

@Transactional
interface IKeyPairService {
    fun save(rsaKey: RSAKey): KeyPair

    fun findNewest(): KeyPair

    fun findById(id: String): KeyPair

    fun findAllByOrderByCreatedAtDesc(): Collection<KeyPair>

    fun count(): Long

    fun generateJWKSet(): RSAKey

    fun deleteById(id: String)

    fun deleteAllButNewest()

    fun deleteAll()

    fun existsById(id: String): Boolean
}

@Component
class KeyPairService
    @Autowired
    constructor(
        private val repository: IKeyPairRepository,
        private val publicKeyConverter: PublicKeyConverter,
        private val privateKeyConverter: PrivateKeyConverter,
    ) : IKeyPairService {
        override fun save(rsaKey: RSAKey): KeyPair {
            var publicBaos: ByteArrayOutputStream? = null
            var privateBaos: ByteArrayOutputStream? = null
            try {
                publicBaos = ByteArrayOutputStream()
                privateBaos = ByteArrayOutputStream()
                publicKeyConverter.serialize(rsaKey.toRSAPublicKey(), publicBaos)
                privateKeyConverter.serialize(rsaKey.toRSAPrivateKey(), privateBaos)
                val pair =
                    KeyPair(id = rsaKey.keyID, publicKey = publicBaos.toString(), privateKey = privateBaos.toString())
                return repository.save(pair)
            } finally {
                publicBaos?.close()
                privateBaos?.close()
            }
        }

        override fun findNewest(): KeyPair = repository.findNewest()

        override fun findById(id: String): KeyPair =
            repository
                .findById(id)
                .orElseThrow {
                    CustomResourceNotFoundException("KeyPair with id: $id not found")
                }

        override fun findAllByOrderByCreatedAtDesc(): Collection<KeyPair> = repository.findAllByOrderByCreatedAtDesc()

        override fun count() = repository.count()

        override fun deleteById(id: String) {
            if (existsById(id).not()) throw CustomResourceNotFoundException("KeyPair with id: $id not found")
            return repository.deleteById(id)
        }

        override fun deleteAllButNewest() = repository.deleteAllButNewest()

        override fun deleteAll() = repository.deleteAll()

        override fun generateJWKSet(): RSAKey {
            val keyPair = generateRsaKey()
            val publicKey = keyPair.public as RSAPublicKey
            val privateKey = keyPair.private as RSAPrivateKey
            return RSAKey
                .Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build()
        }

        override fun existsById(id: String): Boolean = repository.existsById(id)

        private fun generateRsaKey(): java.security.KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            return keyPairGenerator.generateKeyPair()
        }
    }

@Component
class PrivateKeyConverter
    @Autowired
    constructor(
        private val textEncryptor: TextEncryptor,
    ) : Serializer<RSAPrivateKey>,
        Deserializer<RSAPrivateKey> {
        override fun serialize(
            privateKey: RSAPrivateKey,
            outputStream: OutputStream,
        ) {
            val pkcs8EncodedKeySpec = PKCS8EncodedKeySpec(privateKey.encoded)
            val pem =
                "-----BEGIN PRIVATE KEY-----" +
                    Base64.getMimeEncoder().encodeToString(pkcs8EncodedKeySpec.encoded) +
                    "-----END PRIVATE KEY-----"
            outputStream.write(this.textEncryptor.encrypt(pem).toByteArray())
        }

        override fun deserialize(inputStream: InputStream): RSAPrivateKey {
            var isr: InputStreamReader? = null
            try {
                isr = InputStreamReader(inputStream)
                val pem =
                    this.textEncryptor.decrypt(
                        FileCopyUtils.copyToString(isr),
                    )
                val privateKeyPEM = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
                val encoded = Base64.getMimeDecoder().decode(privateKeyPEM)
                val keyFactory = KeyFactory.getInstance("RSA")
                val keySpec = PKCS8EncodedKeySpec(encoded)
                return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
            } finally {
                isr?.close()
            }
        }
    }

@Component
class PublicKeyConverter
    @Autowired
    constructor(
        private val textEncryptor: TextEncryptor,
    ) : Serializer<RSAPublicKey>,
        Deserializer<RSAPublicKey> {
        override fun serialize(
            publicKey: RSAPublicKey,
            outputStream: OutputStream,
        ) {
            val x509EncodedKeySpec = X509EncodedKeySpec(publicKey.encoded)
            val pub =
                "-----BEGIN PUBLIC KEY-----" +
                    Base64.getMimeEncoder().encodeToString(x509EncodedKeySpec.encoded) +
                    "-----END PUBLIC KEY-----"
            outputStream.write(this.textEncryptor.encrypt(pub).toByteArray())
        }

        override fun deserialize(inputStream: InputStream): RSAPublicKey {
            var isr: InputStreamReader? = null
            try {
                isr = InputStreamReader(inputStream)
                val pub = textEncryptor.decrypt(FileCopyUtils.copyToString(isr))
                val publicKeyPEM = pub.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
                val encoded = Base64.getMimeDecoder().decode(publicKeyPEM)
                val keyFactory = KeyFactory.getInstance("RSA")
                val keySpec = X509EncodedKeySpec(encoded)
                return keyFactory.generatePublic(keySpec) as RSAPublicKey
            } finally {
                isr?.close()
            }
        }
    }
