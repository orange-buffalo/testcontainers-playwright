package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Selectors

/**
 * Provides access to the Playwright API for browser automation and testing.
 *
 * This interface serves as a wrapper around [com.microsoft.playwright.Playwright]
 * that ensures compatibility with the underlying browser lifecycle. It provides methods to access
 * Chromium, Firefox, and WebKit browser instances, as well as a selector API.
 *
 * Instances of this interface are not thread-safe. Each thread should have its own instance. This
 * is handled automatically when accessing the API via [PlaywrightContainer.getPlaywrightApi]
 * or corresponding test framework extension.
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
