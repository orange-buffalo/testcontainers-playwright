package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Selectors

/**
 * Provides access to Playwright API.
 *
 * Essentially is a wrapper around [com.microsoft.playwright.Playwright] that is compatible with the
 * underlying browser lifecycle.
 *
 * Instances are not thread-safe. Each thread should have its own instance. This is handled by [PlaywrightContainer]
 * if this API is accessed via [PlaywrightContainer.getPlaywrightApi].
 */
interface PlaywrightApi {

    /**
     * Returns a Chromium browser instance.
     */
    fun chromium(): Browser

    /**
     * Returns a Firefox browser instance.
     */
    fun firefox(): Browser

    /**
     * Returns a WebKit browser instance.
     */
    fun webkit(): Browser

    /**
     * See [com.microsoft.playwright.Playwright.selectors].
     */
    fun selectors(): Selectors
}
