package com.planit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlanitApplication

fun main(args: Array<String>) {
    println("--- DEPLOYMENT MARKER: v20251110-0930 ---")
    runApplication<PlanitApplication>(*args)
}
