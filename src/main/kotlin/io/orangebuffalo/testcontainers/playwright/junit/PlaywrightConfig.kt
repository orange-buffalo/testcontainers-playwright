package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserContext
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Annotated class will use provided configuration for objects provided by [PlaywrightExtension].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Inherited
annotation class PlaywrightConfig(
    /**
     * Configurer class that will be used to configure [PlaywrightExtension].
     */
    val configurer: KClass<out PlaywrightConfigurer>
)

/**
 * Allows to customize [PlaywrightExtension] behaviour.
 */
interface PlaywrightConfigurer {

    /**
     * Creates or provides a container that will be used to start Playwright. Examples of usage include:
     * - using a custom image
     * - providing fully custom implementation
     * - returning a singleton instance when [Singleton Containers](https://testcontainers.com/guides/testcontainers-container-lifecycle/#_using_singleton_containers)
     * pattern is used
     */
    fun createContainer(): PlaywrightContainer = PlaywrightContainer()

    /**
     * Allows to customize container before it is started.
     *
     * Examples could be connecting container to a network to get access to web-app under test.
     */
    fun setupContainer(container: PlaywrightContainer) {}

    /**
     * Allows to customize browser context options, e.g. to set the default URL.
     *
     * If `null` is returned, default Playwright options will be used.
     *
     * See [NewContextOptions] for more details.
     */
    fun createBrowserContextOptions(): NewContextOptions? = null

    /**
     * Allows to customize browser context once it is created, e.g. to set default timeout.
     *
     * See [BrowserContext] for more details.
     */
    fun setupBrowserContext(context: BrowserContext) {}

    /**
     * Very special extension point. Allows to replace [io.orangebuffalo.testcontainers.playwright.PlaywrightApi]
     * with a custom implementation, i.e. to bypass Testcontainers/Docker.
     *
     * Main purpose is to allow the tests to conditionally switch to local browsers, e.g. when running from IDE.
     * This allows to improve developer experience locally, still running the tests in CI with Docker.
     * See [LocalPlaywrightApiProvider] - either use it in this scenario or provide similar implementation.
     *
     * If `null` is returned, the default implementation will be used.
     */
    fun createPlaywrightApiProvider(): PlaywrightApiProvider? = null
}
