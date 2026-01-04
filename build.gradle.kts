import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "3.2.0"
  id("io.spring.dependency-management") version "1.1.4"
  kotlin("jvm") version "1.9.20"
  kotlin("plugin.spring") version "1.9.20"
  kotlin("plugin.jpa") version "1.9.20"
}

group = "com.planit"

version = "0.0.1-SNAPSHOT"

java { sourceCompatibility = JavaVersion.VERSION_17 }

repositories { mavenCentral() }

dependencies {
  // Spring Boot Starters
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security")
  developmentOnly("org.springframework.boot:spring-boot-devtools")

  // Kotlin
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  // PostgreSQL
  runtimeOnly("org.postgresql:postgresql")

  // H2 Database (for testing)
  testImplementation("com.h2database:h2")

  // JWT
  implementation("io.jsonwebtoken:jjwt-api:0.12.3")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

  // Swagger/OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

  // Test
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.mockk:mockk:1.13.8")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  jvmArgs("-XX:+EnableDynamicAgentLoading")
  useJUnitPlatform()
}
