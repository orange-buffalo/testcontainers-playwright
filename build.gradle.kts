import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
    id("org.jetbrains.kotlin.libs.publisher") version "1.8.10-dev-40"
    id("me.qoomon.git-versioning") version "6.4.2"
}

group = "io.orange-buffalo"
version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        branch("master") {
            version = "\${describe.tag.version}-SNAPSHOT"
        }
        branch(".+") {
            version = "\${ref}-SNAPSHOT"
        }
        tag("v(?<version>.*)") {
            version = "\${ref.version}"
        }
    }
    rev {
        version = "\${commit}"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.testcontainers:testcontainers:1.18.0")

    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    compileOnly("com.microsoft.playwright:playwright:1.32.0")
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.9.2")

    testImplementation("io.kotest:kotest-assertions-core:4.0.7")
    testImplementation("com.microsoft.playwright:playwright:1.32.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.testcontainers:nginx:1.18.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

kotlinPublications {
    signingCredentials(

    )
    pom {
        githubRepo("orange-buffalo", "testcontainers-playwright")
        description.set("Testcontainers-based container for Playwright")
    }
    publication {
    }
}
