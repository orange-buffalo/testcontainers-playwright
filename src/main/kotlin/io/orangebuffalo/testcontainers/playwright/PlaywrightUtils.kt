package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import java.util.jar.Manifest

internal val log = mu.KotlinLogging.logger("io.orangebuffalo.testcontainers.playwright")

internal fun getPlaywrightVersionOnClasspath(): String? {
    val playwrightVersions = mutableSetOf<String>()
    try {
        val classLoader = Thread.currentThread().contextClassLoader
        val manifests = classLoader.getResources("META-INF/MANIFEST.MF")

        while (manifests.hasMoreElements()) {
            val manifestURL = manifests.nextElement()
            manifestURL.openStream().use { inputStream ->
                val manifest = Manifest()
                manifest.read(inputStream)

                val implementationTitle = manifest.mainAttributes.getValue("Implementation-Title")
                if (implementationTitle == "Playwright - Main Library") {
                    val implementationVersion = manifest.mainAttributes.getValue("Implementation-Version")
                    if (implementationVersion != null) {
                        playwrightVersions.add(implementationVersion)
                        log.debug { "Playwright version $implementationVersion detected on classpath" }
                    }
                }
            }
        }
    } catch (e: Exception) {
        log.warn(e) { "Failed to determine Playwright version from JARs Manifest" }
    }

    if (playwrightVersions.isEmpty()) {
        log.warn("Failed to determine Playwright version from classpath - will use the provided one")
        return null
    }

    if (playwrightVersions.size > 1) {
        log.warn { "Multiple versions of Playwright API found on classpath ($playwrightVersions) - will use the provided one" }
        return null
    }

    val playwrightVersion = playwrightVersions.first()
    log.debug { "Found Playwright version $playwrightVersion" }

    return playwrightVersion
}

internal fun Browser.safeClose() {
    try {
        close()
    } catch (e: Exception) {
        log.warn(e) { "Failed to close browser" }
    }
}

internal fun Playwright.safeClose() {
    try {
        close()
    } catch (e: Exception) {
        log.warn(e) { "Failed to close playwright" }
    }
}
