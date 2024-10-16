package com.authumn.authumn.keypairs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@SecurityScheme(
    name = "security_auth",
    type = SecuritySchemeType.OAUTH2,
    flows =
        OAuthFlows(
            clientCredentials =
                OAuthFlow(
                    tokenUrl = "http://127.0.0.1:9000/oauth2/token", // FIXME: should point to a gateway
                    scopes = [
                        OAuthScope(
                            name = "admin",
                            description = "admin scope",
                        ),
                    ],
                ),
        ),
)
@SecurityRequirement(
    name = "security_auth",
    scopes = ["admin"],
)
@Tag(name = "keypairs", description = "auth-server keys operations")
@RequestMapping(value = ["/api/keypair"], produces = [MediaType.APPLICATION_JSON_VALUE])
interface IKeyPairApi {
    @PostMapping
    @Operation(
        summary = "generate a new keypair",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = KeyPairGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                "id": "5fc51811-34ce-4f65-b0d6-4f5e83f794ec",
                                "publicKey": "-----BEGIN PUBLIC KEY-----MIIBIjAN...-----END PUBLIC KEY-----",
                                "privateKey": "-----BEGIN PRIVATE KEY-----MIIEvA...-----END PRIVATE KEY-----",
                                "createdAt": "2024-10-04T06:02:50.705215Z"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun generate(): KeyPairGetDto

    @GetMapping("/{id}")
    @Operation(
        summary = "find a keypair by id",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = KeyPairGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                "id": "5fc51811-34ce-4f65-b0d6-4f5e83f794ec",
                                "publicKey": "-----BEGIN PUBLIC KEY-----MIIBIjAN...-----END PUBLIC KEY-----",
                                "privateKey": "-----BEGIN PRIVATE KEY-----MIIEvA...-----END PRIVATE KEY-----",
                                "createdAt": "2024-10-04T06:02:50.705215Z"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun findById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
    ): KeyPairGetDto

    @GetMapping
    @Operation(
        summary = "find all keypairs",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = ArraySchema(items = Schema(implementation = KeyPairGetDto::class)),
                        schema = Schema(implementation = KeyPairGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                [
                                  {
                                    "id": "5fc51811-34ce-4f65-b0d6-4f5e83f794ec",
                                    "publicKey": "-----BEGIN PUBLIC KEY-----MIIBIjAN...-----END PUBLIC KEY-----",
                                    "privateKey": "-----BEGIN PRIVATE KEY-----MIIEvA...-----END PRIVATE KEY-----",
                                    "createdAt": "2024-10-04T06:02:50.705215Z"
                                  }
                                ]                                 
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun findAll(): Collection<KeyPairGetDto>

    @DeleteMapping("/{id}")
    @Operation(
        summary = "delete a keypair by id",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "No Content",
            ),
        ],
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
    )

    @DeleteMapping
    @Operation(
        summary = "delete all but the newest keypair",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "No Content",
            ),
        ],
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteAllButNewest()
}
