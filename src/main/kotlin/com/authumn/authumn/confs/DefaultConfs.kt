package com.authumn.authumn.confs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor

@Configuration
class DefaultConfs {
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper =
        ObjectMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            // disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("authumn-api")
                .description("TODO")
                .version("0.0.1")
                .contact(Contact().name("TODO").email("TODO")),
        )

    @Bean
    fun textEncryptor(
        @Value("\${enc.password}") password: String,
        @Value("\${enc.salt}") salt: String,
    ): TextEncryptor = Encryptors.text(password, salt)
}
