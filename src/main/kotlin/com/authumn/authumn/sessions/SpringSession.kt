package com.authumn.authumn.sessions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

@Entity(name = "spring_session")
data class SpringSession(
    @Id
    @Column(name = "primary_id")
    val primaryId: String,
    @Column(name = "session_id")
    val sessionId: String,
    @Column(name = "creation_time")
    val creationTime: Long,
    @Column(name = "last_access_time")
    val lastAccessTime: Long,
    @Column(name = "max_inactive_interval")
    val maxInactiveInterval: Int,
    @Column(name = "expiry_time")
    val expiryTime: Long,
    @Column(name = "principal_name")
    val principalName: String,
)

@Transactional(readOnly = true)
interface ISpringSessionRepo : JpaRepository<SpringSession, String>
