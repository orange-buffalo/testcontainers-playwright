package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserContext
import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PlaywrightTestcontainersConfig(val configurer: KClass<out PlaywrightTestcontainersConfigurer>)

interface PlaywrightTestcontainersConfigurer {

    fun provideContainer(): PlaywrightContainer = PlaywrightContainer()

    fun setupContainer(container: PlaywrightContainer) {}

    fun createBrowserContextOptions(): NewContextOptions? = null

    fun configureBrowserContext(context: BrowserContext) {}
}
