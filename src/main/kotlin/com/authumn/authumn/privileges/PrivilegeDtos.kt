package com.authumn.authumn.privileges

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.io.Serializable

data class PrivilegeGetDto(
    val id: String,
    val label: String,
    val isDefault: Boolean,
    val createdAt: String,
    val updateAt: String,
) : Serializable

data class PrivilegePostDto(
    @field:NotBlank(message = "label must not be blank")
    @field:NotEmpty(message = "label must not be empty")
    @Schema(
        description = "privilege label, must not be blank or empty, must be unique",
        defaultValue = "read",
    )
    val label: String,
    @Schema(
        description = "default privileges are automatically picked when creating a new role",
        defaultValue = "false",
    )
    val isDefault: Boolean? = false,
)

data class PrivilegePutDto(
    @field:NotBlank(message = "label must not be blank")
    @field:NotEmpty(message = "label must not be empty")
    @Schema(
        description = "privilege label, must not be blank or empty, must be unique",
        defaultValue = "read",
    )
    val label: String,
    @Schema(
        description = "default privileges are automatically picked when creating a new role",
        defaultValue = "false",
    )
    val isDefault: Boolean? = false,
)
