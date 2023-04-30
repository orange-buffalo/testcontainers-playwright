package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainerApi
import org.junit.jupiter.api.extension.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val log = mu.KotlinLogging.logger {}
private var containers = ConcurrentHashMap<String?, PlaywrightContainer>()
private val extensionNamespace = ExtensionContext.Namespace.create("io.orangebuffalo.testcontainers.playwright")
private const val BROWSER_CONTEXTS_KEY = "browserContexts"

class PlaywrightTestcontainersExtension : Extension, ParameterResolver, AfterEachCallback {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return isContainerApiParameter(parameterContext)
                || isBrowserContextParameter(parameterContext)
                || isPage(parameterContext)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        log.debug { "Resolving parameter ${parameterContext.parameter} in ${extensionContext.displayName}" }

        val containerApi = getOrCreateContainerApi(extensionContext)
        return when {
            isContainerApiParameter(parameterContext) -> containerApi

            isBrowserContextParameter(parameterContext) -> createBrowserContext(
                parameterContext,
                extensionContext,
                containerApi
            )

            isPage(parameterContext) -> createBrowserContext(
                parameterContext,
                extensionContext,
                containerApi
            ).newPage()

            else -> throw IllegalArgumentException("Unsupported parameter type: ${parameterContext.parameter.type}")
        }
    }

    private fun createBrowserContext(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
        containerApi: PlaywrightContainerApi,
    ): BrowserContext {
        val configurer = extensionContext.getConfig().instantiateConfigurer()
        val context = if (hasAnnotation(parameterContext, extensionContext, RequiresWebkit::class)) {
            containerApi.webkit().newContext(configurer?.createBrowserContextOptions())
        } else if (hasAnnotation(parameterContext, extensionContext, RequiresFirefox::class)) {
            containerApi.firefox().newContext(configurer?.createBrowserContextOptions())
        } else {
            containerApi.chromium().newContext(configurer?.createBrowserContextOptions())
        }

        configurer?.configureBrowserContext(context)
        extensionContext.getBrowserContexts().add(context)

        return context
    }

    private fun isContainerApiParameter(parameterContext: ParameterContext): Boolean {
        val parameterClass = parameterContext.parameter.type
        return PlaywrightContainerApi::class.java == parameterClass
    }

    private fun isBrowserContextParameter(parameterContext: ParameterContext): Boolean {
        val parameterClass = parameterContext.parameter.type
        return BrowserContext::class.java == parameterClass
    }

    private fun isPage(parameterContext: ParameterContext): Boolean {
        val parameterClass = parameterContext.parameter.type
        return Page::class.java == parameterClass
    }

    private fun hasAnnotation(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
        annotationClass: KClass<out Annotation>
    ): Boolean {
        return hasAnnotation(parameterContext.parameter, annotationClass)
                || hasAnnotation(extensionContext.requiredTestMethod, annotationClass)
                || hasAnnotation(extensionContext.requiredTestClass, annotationClass)
    }

    override fun afterEach(context: ExtensionContext) {
        val contexts = context.getBrowserContexts()
        log.debug { "Closing ${contexts.size} browser contexts" }
        contexts.forEach { it.close() }
        contexts.clear()
    }

    @Suppress("UNCHECKED_CAST")
    private fun ExtensionContext.getBrowserContexts(): MutableList<BrowserContext> {
        return getStore(extensionNamespace)
            .getOrComputeIfAbsent<String, MutableList<BrowserContext>>(BROWSER_CONTEXTS_KEY) {
                mutableListOf()
            } as MutableList<BrowserContext>
    }

    private fun getOrCreateContainerApi(extensionContext: ExtensionContext): PlaywrightContainerApi {
        val config = extensionContext.getConfig()
        val configurer = config.instantiateConfigurer()
        val playwrightContainerApiProvider = configurer?.getPlaywrightContainerApiProvider()
        if (playwrightContainerApiProvider != null) {
            return playwrightContainerApiProvider.getOrCreatePlaywrightContainerApiForCurrentThread()
        }

        val container = containers.computeIfAbsent(config.containerStorageKey) { configKey ->
            log.info { "Starting Playwright container for $configKey config" }

            val container = configurer?.provideContainer() ?: PlaywrightContainer()
            container
                .withLogConsumer {
                    log.debug { "[CONTAINER] ${it.utf8String}" }
                }
                .also {
                    configurer?.setupContainer(it)
                    it.start()
                    log.info { "Playwright container started" }
                }
        }
        return container.getPlaywrightContainerApi()
    }
}

private fun ExtensionContext.getConfig() = getAnnotation(requiredTestClass, PlaywrightTestcontainersConfig::class)

private val PlaywrightTestcontainersConfig?.containerStorageKey: String
    get() = this?.configurer?.qualifiedName ?: "default"

private fun PlaywrightTestcontainersConfig?.instantiateConfigurer(): PlaywrightTestcontainersConfigurer? {
    return this?.configurer?.java?.getDeclaredConstructor()?.newInstance()
}
