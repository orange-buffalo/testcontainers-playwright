package io.orangebuffalo.testcontainers.playwright.junit

import io.orangebuffalo.testcontainers.playwright.PlaywrightContainerApi

interface PlaywrightContainerApiProvider {

    fun getOrCreatePlaywrightContainerApiForCurrentThread(): PlaywrightContainerApi
}
