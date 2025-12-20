package com.planit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class PlanitApplication

fun main(args: Array<String>) {
    println("--- DEPLOYMENT MARKER: v20251110-0930 ---")
    runApplication<PlanitApplication>(*args)
}
