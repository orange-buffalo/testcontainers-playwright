package io.orangebuffalo.testcontainers.playwright.junit

import io.orangebuffalo.testcontainers.playwright.PlaywrightContainer
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PlaywrightTestcontainersConfig(val configurer: KClass<out PlaywrightTestcontainersConfigurer>)

interface PlaywrightTestcontainersConfigurer {

    fun provideContainer(): PlaywrightContainer = PlaywrightContainer()

    fun setupContainer(container: PlaywrightContainer) {}
}
