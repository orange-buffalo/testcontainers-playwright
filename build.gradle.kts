plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
    id("me.qoomon.git-versioning") version "6.4.2"
    id("org.jetbrains.dokka") version "1.8.10"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
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

java {
    withSourcesJar()
}

val docsJar = tasks.register<Jar>("docsJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

artifacts {
    archives(docsJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["kotlin"])
            artifacts {
                artifact(tasks.named("sourcesJar"))
                artifact(docsJar)
            }

            pom {
                name.set("testcontainers-playwright")
                description.set("Testcontainers-based container for Playwright")
                url.set("https://github.com/orange-buffalo/kiosab")
                packaging = "jar"
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("orange-buffalo")
                        name.set("Bogdan Ilchyshyn")
                        email.set("orange-buffalo@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:orange-buffalo/testcontainers-playwright.git")
                    developerConnection.set("scm:git@github.com:orange-buffalo/testcontainers-playwright.git")
                    url.set("https://github.com/orange-buffalo/testcontainers-playwright")
                }
            }
        }
    }
}

nexusPublishing {
    this.repositories {
        sonatype {
            val ossrhUser: String? by project
            val ossrhPassword: String? by project
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots"))
            username.set(ossrhUser)
            password.set(ossrhPassword)
        }
    }
}

signing {
    val ossrhSigningKey: String? by project
    val ossrhSigningPassword: String? by project
    useInMemoryPgpKeys(ossrhSigningKey, ossrhSigningPassword)
    sign(publishing.publications["maven"])
}
