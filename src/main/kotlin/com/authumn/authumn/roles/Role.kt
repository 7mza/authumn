package com.authumn.authumn.roles

import com.authumn.authumn.privileges.Privilege
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
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
    name = "roles",
    uniqueConstraints = [UniqueConstraint(columnNames = ["label"], name = "role_label_unique")],
)
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,
    @Column(nullable = false)
    @field:NotBlank(message = "label must not be blank")
    @field:NotEmpty(message = "label must not be empty")
    var label: String,
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    val isDefault: Boolean = false,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "roles_privileges",
        joinColumns = [JoinColumn(name = "roleId", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "privilegeId", referencedColumnName = "id")],
    )
    @field:NotEmpty(message = "privileges must not be empty")
    var privileges: Collection<Privilege>,
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
        RoleGetDto(
            id = this.id,
            label = this.label,
            isDefault = this.isDefault,
            privileges = this.privileges.map { it.toDto() },
        )
}

interface IRoleRepo : JpaRepository<Role, String> {
    fun findAllByIsDefaultTrue(): Collection<Role>
}
