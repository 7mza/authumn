package com.authumn.authumn.commons

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val PATTERN_FORMAT = "dd/MM/yyyy HH:mm:ss"
const val LABEL_MUST_BE_UNIQUE = "label must be unique"
const val EMAIL_MUST_BE_UNIQUE = "email must be unique"

class Commons {
    companion object {
        inline fun <reified T> parseJson(
            json: String,
            objectMapper: ObjectMapper,
        ): T = objectMapper.readValue(json.trimIndent())

        inline fun <reified T> writeJson(
            t: T,
            objectMapper: ObjectMapper,
        ): String = objectMapper.writeValueAsString(t)

        fun instantToString(instant: Instant): String =
            DateTimeFormatter
                .ofPattern(PATTERN_FORMAT)
                .withZone(ZoneId.of("UTC"))
                .format(instant)
    }
}
