package com.authumn.authumn.confs

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Profile("dev")
@Configuration
class DevConfs {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Profile("dev")
    @Component
    class Events
        @Autowired
        constructor(
            private val jdbcTemplate: JdbcTemplate,
        ) {
            @EventListener(ContextClosedEvent::class)
            fun onShutdown() {
                logger.debug("ContextClosedEvent: cleaning db")
                jdbcTemplate.execute(
                    """
                    TRUNCATE oauth2_registered_client,
                        oauth2_authorization_consent,
                        oauth2_authorization,
                        spring_session_attributes,
                        spring_session,
                        key_pairs,
                        roles_privileges,
                        users_roles,
                        privileges,
                        roles,
                        users;
                    """.trimIndent(),
                )
            }
        }
}
