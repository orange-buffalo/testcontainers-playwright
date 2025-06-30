import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jreleaser.model.Active

plugins {
    kotlin("jvm") version "1.7.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    // version pinned as newer has major JGit changed which is not compatible with jreleaser
    id("com.github.jmongard.git-semver-plugin") version "0.13.0"
    id("maven-publish")
    id("signing")
    id("org.jreleaser") version "1.18.0"
}

group = "io.orange-buffalo"
semver {
    // tags managed by jreleaser
    createReleaseTag = false
}
version = semver.version

repositories {
    mavenCentral()
}

// for better control on what we shade, we define private dependencies in a separate configuration;
// it does not extend anything (unlike "implementation") and so we fully in control of what is included
val shadowDependencies: Configuration by configurations.creating
configurations["implementation"].extendsFrom(shadowDependencies)

dependencies {
    api("org.testcontainers:testcontainers:1.21.3")
    api("com.microsoft.playwright:playwright:1.53.0")
    api("org.jetbrains.kotlin:kotlin-stdlib:1.7.22")

    shadowDependencies("io.github.microutils:kotlin-logging:3.0.5")
    shadowDependencies("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    compileOnly("org.junit.jupiter:junit-jupiter-api:5.13.2")

    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("com.microsoft.playwright:playwright:1.53.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.2")
    testImplementation("org.testcontainers:nginx:1.21.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.2")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.18")
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
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    release {
        github {
            val shouldSkipGitHubActions = project.version.get().endsWith("-SNAPSHOT")
            skipTag = shouldSkipGitHubActions
            skipRelease = shouldSkipGitHubActions

            uploadAssets = Active.NEVER
            prerelease {
                enabled = true
            }
            changelog {
                formatted = Active.ALWAYS
                preset = "conventional-commits"
                skipMergeCommits = true
                hide {
                    uncategorized = true
                    contributor("[bot]")
                    contributor("orange-buffalo")
                    contributor("GitHub")
                }
            }
        }
    }
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("release-deploy") {
                    active = Active.RELEASE
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                    javadocJar = false
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active = Active.SNAPSHOT
                    url = "https://central.sonatype.com/repository/maven-snapshots/"
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
                    applyMavenCentralRules = true
                    snapshotSupported = true
                    closeRepository = true
                    releaseRepository = true
                    stagingRepository("build/staging-deploy")
                    javadocJar = false
                }
            }
        }
    }
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
