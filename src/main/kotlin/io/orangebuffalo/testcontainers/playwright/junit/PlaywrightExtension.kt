package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import io.orangebuffalo.testcontainers.playwright.PlaywrightApi
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.support.AnnotationSupport
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

private val log = mu.KotlinLogging.logger {}
private var containers = ConcurrentHashMap<String?, PlaywrightContainer>()
private val extensionNamespace = ExtensionContext.Namespace.create("io.orangebuffalo.testcontainers.playwright")
private const val BROWSER_CONTEXTS_KEY = "browserContexts"

/**
 * JUnit 5 extension that manages [PlaywrightContainer] and provides Playwright primitives bound to its browsers.
 *
 * This extension can be configured via [PlaywrightConfig] annotation (or its meta-annotation). A single (VM-wide)
 * container is created per each configuration. If no configuration is provided, a default one is used.
 *
 * Extensions allows to inject [PlaywrightApi], [BrowserContext] and [Page] instances into test methods and manages
 * their lifecycle (e.g. cleans up browser contexts after each test).
 *
 * When [PlaywrightApi] is injected, test authors have full controls over the browsers and can use them in any way.
 * They are also responsible for closing any resources they create (e.g. browser contexts and/or pages).
 *
 * When [BrowserContext] or [Page] is injected, the extension will close it after each test. Test authors can
 * control which browser these objects are bound to by annotating test methods with [RequiresChromium],
 * [RequiresFirefox] or [RequiresWebkit] annotation. The annotations can be applied to parameter, test method or
 * test class, both directly and via their meta-annotations. If not annotated, Chromium will be used by default.
 */
class PlaywrightExtension : Extension, ParameterResolver, AfterEachCallback {
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
        containerApi: PlaywrightApi,
    ): BrowserContext {
        val configurer = extensionContext.getConfig().instantiateConfigurer()
        val context = if (hasAnnotation(parameterContext, extensionContext, RequiresWebkit::class)) {
            containerApi.webkit().newContext(configurer?.createBrowserContextOptions())
        } else if (hasAnnotation(parameterContext, extensionContext, RequiresFirefox::class)) {
            containerApi.firefox().newContext(configurer?.createBrowserContextOptions())
        } else {
            containerApi.chromium().newContext(configurer?.createBrowserContextOptions())
        }

        configurer?.setupBrowserContext(context)
        extensionContext.getBrowserContexts().add(context)

        return context
    }

    private fun isContainerApiParameter(parameterContext: ParameterContext): Boolean {
        val parameterClass = parameterContext.parameter.type
        return PlaywrightApi::class.java == parameterClass
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
        return AnnotationSupport.isAnnotated(parameterContext.parameter, annotationClass.java)
                || AnnotationSupport.isAnnotated(extensionContext.requiredTestMethod, annotationClass.java)
                || AnnotationSupport.isAnnotated(extensionContext.requiredTestClass, annotationClass.java)
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

    private fun getOrCreateContainerApi(extensionContext: ExtensionContext): PlaywrightApi {
        val config = extensionContext.getConfig()
        val configurer = config.instantiateConfigurer()
        val playwrightContainerApiProvider = configurer?.createPlaywrightApiProvider()
        if (playwrightContainerApiProvider != null) {
            return playwrightContainerApiProvider.getOrCreatePlaywrightApiForCurrentThread()
        }

        val container = containers.computeIfAbsent(config.containerStorageKey) { configKey ->
            log.info { "Starting Playwright container for $configKey config" }

            val container = configurer?.createContainer() ?: PlaywrightContainer()
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
        return container.getPlaywrightApi()
    }
}

private fun ExtensionContext.getConfig() : PlaywrightConfig? = AnnotationSupport
    .findAnnotation(requiredTestClass, PlaywrightConfig::class.java)
    .orElse(null)

private val PlaywrightConfig?.containerStorageKey: String
    get() = this?.configurer?.qualifiedName ?: "default"

private fun PlaywrightConfig?.instantiateConfigurer(): PlaywrightConfigurer? {
    return this?.configurer?.java?.getDeclaredConstructor()?.newInstance()
}
