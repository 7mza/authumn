package com.authumn.authumn.keypairs

import com.nimbusds.jose.jwk.RSAKey
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.transaction.Transactional
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.security.crypto.encrypt.TextEncryptor
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity(name = "key_pairs")
data class KeyPair(
    // @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    val id: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val publicKey: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    val privateKey: String,
    @Column(nullable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val updateAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
) {
    fun toRSAKey(
        publicKeyConverter: PublicKeyConverter,
        privateKeyConverter: PrivateKeyConverter,
    ): RSAKey {
        val publicKey = publicKeyConverter.deserializeFromByteArray(this.publicKey.toByteArray())
        val privateKey = privateKeyConverter.deserializeFromByteArray(this.privateKey.toByteArray())
        return RSAKey
            .Builder(publicKey)
            .privateKey(privateKey)
            .keyID(this.id)
            .build()
    }

    fun toDto(textEncryptor: TextEncryptor) =
        KeyPairGetDto(
            id = this.id,
            publicKey = textEncryptor.decrypt(this.publicKey),
            privateKey = textEncryptor.decrypt(this.privateKey),
            createdAt = this.createdAt.toString(),
        )
}

interface IKeyPairRepository : JpaRepository<KeyPair, String> {
    fun findAllByOrderByCreatedAtDesc(): Collection<KeyPair>

    @Query("SELECT e FROM key_pairs e ORDER BY e.createdAt DESC LIMIT 1")
    fun findNewest(): KeyPair

    @Transactional
    @Modifying
    @Query("DELETE FROM key_pairs e1 WHERE e1.id <> (SELECT e2.id FROM key_pairs e2 ORDER BY e2.createdAt DESC LIMIT 1)")
    fun deleteAllButNewest()
}
