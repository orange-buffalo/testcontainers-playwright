package io.orangebuffalo.testcontainers.playwright.junit

import io.orangebuffalo.testcontainers.playwright.PlaywrightApi

/**
 * Extension point for providing [PlaywrightApi] instances for tests.
 *
 * See [PlaywrightConfigurer.createPlaywrightApiProvider] for detailed explanation.
 */
interface PlaywrightApiProvider {

    /**
     * Returns a [PlaywrightApi] instance for the current thread, creating it if necessary.
     */
    fun getOrCreatePlaywrightApiForCurrentThread(): PlaywrightApi
}
