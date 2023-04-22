package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.BrowserContext
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainerApi
import org.junit.jupiter.api.extension.*
import java.lang.reflect.AnnotatedElement

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

private val extensionNamespace = ExtensionContext.Namespace.create("io.orangebuffalo.testcontainers.playwright")
private const val BROWSER_CONTEXTS_KEY = "browserContexts"

class PlaywrightTestcontainersExtension : Extension, ParameterResolver, AfterEachCallback {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return isContainerApiParameter(parameterContext)
                || isBrowserContextParameter(parameterContext)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        log.debug { "Resolving parameter ${parameterContext.parameter} in ${extensionContext.displayName}" }

        val containerApi = containerApiPerThread.get()
        return when {
            isContainerApiParameter(parameterContext) -> containerApi
            isBrowserContextParameter(parameterContext) -> createBrowserContext(
                parameterContext,
                extensionContext,
                containerApi
            )
            else -> throw IllegalArgumentException("Unsupported parameter type: ${parameterContext.parameter.type}")
        }
    }

    private fun createBrowserContext(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
        containerApi: PlaywrightContainerApi,
    ): BrowserContext {
        val context = if (hasAnnotation(parameterContext, extensionContext, RequiresWebkit::class.java)) {
            containerApi.webkit().newContext()
        } else if (hasAnnotation(parameterContext, extensionContext, RequiresFirefox::class.java)) {
            containerApi.firefox().newContext()
        } else {
            containerApi.chromium().newContext()
        }

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

    private fun hasAnnotation(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
        annotationClass: Class<out Annotation>
    ): Boolean {
        return hasAnnotation(parameterContext.parameter, annotationClass)
                || hasAnnotation(extensionContext.requiredTestMethod, annotationClass)
                || hasAnnotation(extensionContext.requiredTestClass, annotationClass)
    }

    private fun hasAnnotation(element: AnnotatedElement, annotationClass: Class<out Annotation>): Boolean {
        return isAnnotatedWith(element, annotationClass) || isMetaAnnotatedWith(element, annotationClass)
    }

    private fun isAnnotatedWith(element: AnnotatedElement, annotationClass: Class<out Annotation>): Boolean {
        return element.isAnnotationPresent(annotationClass)
    }

    private fun isMetaAnnotatedWith(
        element: AnnotatedElement,
        annotationClass: Class<out Annotation>,
        visited: MutableSet<AnnotatedElement> = mutableSetOf()
    ): Boolean {
        val annotations = element.annotations
        for (annotation in annotations) {
            if (annotation.annotationClass.java == annotationClass) {
                return true
            }
            val next = annotation.annotationClass.java
            if (visited.add(next) && isMetaAnnotatedWith(next, annotationClass, visited)) {
                return true
            }
        }
        return false
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
}
