plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

group = "io.orange-buffalo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.testcontainers:testcontainers:1.18.0")

    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    compileOnly("com.microsoft.playwright:playwright:1.32.0")
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.9.2")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:4.0.7")
    testImplementation("com.microsoft.playwright:playwright:1.32.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.testcontainers:nginx:1.18.0")

    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
