import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    id("me.qoomon.git-versioning") version "6.4.3"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

// for better control on what we shade, we define private dependencies in a separate configuration;
// it does not extend anything (unlike "implementation") and so we fully in control of what is included
val shadowDependencies: Configuration by configurations.creating
configurations["implementation"].extendsFrom(shadowDependencies)

dependencies {
    api("org.testcontainers:testcontainers:1.19.6")
    api("com.microsoft.playwright:playwright:1.41.2")
    api("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")

    shadowDependencies("io.github.microutils:kotlin-logging:3.0.5")
    shadowDependencies("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    compileOnly("org.junit.jupiter:junit-jupiter-api:5.10.2")

    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("com.microsoft.playwright:playwright:1.41.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.testcontainers:nginx:1.19.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.1")
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

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

val docsJar = tasks.register<Jar>("docsJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

artifacts {
    archives(docsJar)
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowDependencies)

    relocate("org.jetbrains", "io.orangebuffalo.testcontainers.playwright.shadow.org.jetbrains")
    relocate("org.intellij", "io.orangebuffalo.testcontainers.playwright.shadow.org.intellij")
    relocate("kotlinx", "io.orangebuffalo.testcontainers.playwright.shadow.kotlinx")
    relocate("mu", "io.orangebuffalo.testcontainers.playwright.shadow.mu")

    dependencies {
        // we cannot shadow slf4j as it won't integrate with client's logging
        exclude(dependency("org.slf4j:slf4j-api:.*"))
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
    }

    // remove top-level unshadowed meta-data to avoid conflicts with client code
    exclude("META-INF/kotlin*.kotlin_module")
    exclude("META-INF/com.android.tools/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/proguard/**")
    exclude("META-INF/versions/**")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            project.shadow.component(this)
            artifacts {
                artifact(tasks.named("sourcesJar"))
                artifact(docsJar)
            }

            pom {
                name.set("testcontainers-playwright")
                description.set("Testcontainers-based container for Playwright")
                url.set("https://github.com/orange-buffalo/testcontainers-playwright")
                packaging = "jar"
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
                withXml {
                    // shadow plugin is not flexible enough, have to manually craft the dependencies
                    addDependenciesFromConfiguration(configuration = configurations["api"], scope = "compile")
                    addDependenciesFromConfiguration(
                        configuration = configurations["compileOnly"],
                        scope = "provided",
                        optional = true
                    )

                    // as we excluded it from shadowing, we have to add it manually to POM dependencies
                    addDependencyFromConfiguration(
                        configuration = shadowDependencies,
                        groupId = "org.slf4j",
                        artifactId = "slf4j-api"
                    )
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
    // provide this property (-Psigning.skip=true) to disable signing and deploy snapshots into local repo
    setRequired({
        !project.hasProperty("signing.skip")
    })
    val ossrhSigningKey: String? by project
    val ossrhSigningPassword: String? by project
    useInMemoryPgpKeys(ossrhSigningKey, ossrhSigningPassword)
    sign(publishing.publications["maven"])
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
