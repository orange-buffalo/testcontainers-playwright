package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Selectors

interface PlaywrightContainerApi {

    fun chromium(): Browser

    fun firefox(): Browser

    fun webkit(): Browser

    /**
     * See [com.microsoft.playwright.Playwright.selectors]
     */
    fun selectors(): Selectors
}
