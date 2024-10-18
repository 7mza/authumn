package com.authumn.authumn.privileges

import com.authumn.authumn.commons.Commons
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@Table(
    name = "privileges",
    uniqueConstraints = [UniqueConstraint(columnNames = ["label"], name = "privilege_label_unique")],
)
data class Privilege(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,
    @Column(nullable = false)
    @field:NotBlank(message = "label must not be blank")
    @field:NotEmpty(message = "label must not be empty")
    var label: String,
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    val isDefault: Boolean = false,
    @Column(nullable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val updateAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
) {
    fun toDto() =
        PrivilegeGetDto(
            id = this.id,
            label = this.label,
            isDefault = this.isDefault,
            createdAt = Commons.instantToString(this.createdAt),
            updateAt = Commons.instantToString(this.createdAt),
        )
}

interface IPrivilegeRepo : JpaRepository<Privilege, String> {
    fun findAllByIsDefaultTrue(): Collection<Privilege>
}
