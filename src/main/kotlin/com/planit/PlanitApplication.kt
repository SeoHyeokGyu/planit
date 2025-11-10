package com.planit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlanitApplication

fun main(args: Array<String>) {
    runApplication<PlanitApplication>(*args)
}
