package com.planit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [OAuth2ClientAutoConfiguration::class])
class PlanitApplication

fun main(args: Array<String>) {
    runApplication<PlanitApplication>(*args)
}
