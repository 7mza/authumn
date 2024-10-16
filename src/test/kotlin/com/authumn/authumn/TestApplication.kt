package com.authumn.authumn

import org.springframework.boot.fromApplication

fun main(args: Array<String>) {
    fromApplication<Application>()
        // .with(TestJarsConfiguration::class)
        .run(*args)
}
