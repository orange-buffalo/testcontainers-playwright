package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserContext
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PlaywrightConfig(val configurer: KClass<out PlaywrightConfigurer>)

interface PlaywrightConfigurer {

    fun createContainer(): PlaywrightContainer = PlaywrightContainer()

    fun setupContainer(container: PlaywrightContainer) {}

    fun createBrowserContextOptions(): NewContextOptions? = null

    fun setupBrowserContext(context: BrowserContext) {}

    fun createPlaywrightApiProvider(): PlaywrightApiProvider? = null
}
