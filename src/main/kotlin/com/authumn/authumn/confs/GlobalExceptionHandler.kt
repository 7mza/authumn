package com.authumn.authumn.confs

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.commons.ErrorDto
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

private const val X_REQUEST_ID_HEADER = "X-Request-ID"

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "internal error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: Throwable,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("Unhandled Exception: ${ex.javaClass}")
        logger.error("$ex")
        val dto =
            ErrorDto(
                code = "as-500-0",
                message = "Unhandled Exception",
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto)
    }

    @ExceptionHandler(CustomConstraintViolationException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "internal error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: CustomConstraintViolationException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        val dto =
            ErrorDto(
                code = "as-500-1",
                message = ex.message,
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "404",
                description = "NOT FOUND",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "not found error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: NoResourceFoundException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorDto(
                code = "as-404-1",
                message = "Resource not found",
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            ),
        )
    }

    @ExceptionHandler(CustomResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "404",
                description = "NOT FOUND",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "not found error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: CustomResourceNotFoundException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        val dto =
            ErrorDto(
                code = "as-404-2",
                message = ex.message,
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "400",
                description = "BAD REQUEST",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "bad request error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: MethodArgumentNotValidException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        val message =
            ex.fieldErrors
                .map { it.defaultMessage }
                .sortedBy { it }
                .joinToString(". ")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorDto(
                code = "as-400-1",
                message = message,
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            ),
        )
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "400",
                description = "BAD REQUEST",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "bad request error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: HandlerMethodValidationException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        val message =
            ex.allErrors
                .map { it.defaultMessage }
                .sortedBy { it }
                .joinToString(". ")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorDto(
                code = "as-400-2",
                message = message,
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            ),
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "400",
                description = "BAD REQUEST",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "bad request error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: HttpMessageNotReadableException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorDto(
                code = "as-400-3",
                message = "Bad request: ${ex.mostSpecificCause.javaClass.toString().split(".").last()}",
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            ),
        )
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                    "code": "code-XXX",
                                    "message": "forbidden error message",
                                    "path": "/path",
                                    "timestamp": "2024-08-08T17:50:39.463730661Z",
                                    "requestId": "9b5e1741-5"
                                }
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun handleException(
        ex: AuthorizationDeniedException,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<ErrorDto> {
        logger.error("$ex")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorDto(
                code = "as-403-1",
                message = "Forbidden",
                path = servletRequest.requestURI,
                timestamp = Instant.now().toString(),
                requestId = servletRequest.getHeader(X_REQUEST_ID_HEADER) ?: "N/A",
            ),
        )
    }
}
