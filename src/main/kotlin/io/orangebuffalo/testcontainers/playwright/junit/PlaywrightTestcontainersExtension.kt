package io.orangebuffalo.testcontainers.playwright.junit

import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainerApi
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

private val log = mu.KotlinLogging.logger {}

private var container: PlaywrightContainer? = null
private val containerApiPerThread = ThreadLocal.withInitial {
    log.info { "Registering new API for ${Thread.currentThread().name}" }
    ensureContainer().registerNewPlaywright()
}

@Synchronized
private fun ensureContainer(): PlaywrightContainer {
    if (container == null) {
        log.info { "Starting Playwright container" }
        container = PlaywrightContainer()
            .withLogConsumer {
                log.debug { "[CONTAINER] ${it.utf8String}" }
            }
            .also { it.start() }
        log.info { "Playwright container started" }
    }
    return container!!
}

class PlaywrightTestcontainersExtension : Extension, ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return isContainerApiParameter(parameterContext)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        log.debug { "Resolving parameter ${parameterContext.parameter} in ${extensionContext.displayName}" }

        val containerApi = containerApiPerThread.get()
        return when {
            isContainerApiParameter(parameterContext) -> containerApi
            else -> throw IllegalArgumentException("Unsupported parameter type: ${parameterContext.parameter.type}")
        }
    }

    private fun isContainerApiParameter(parameterContext: ParameterContext): Boolean {
        val parameterClass = parameterContext.parameter.type
        return PlaywrightContainerApi::class.java == parameterClass
    }
}
