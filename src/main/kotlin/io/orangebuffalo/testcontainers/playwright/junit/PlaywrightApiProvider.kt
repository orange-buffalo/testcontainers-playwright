package io.orangebuffalo.testcontainers.playwright.junit

import io.orangebuffalo.testcontainers.playwright.PlaywrightApi

interface PlaywrightApiProvider {

    fun getOrCreatePlaywrightApiForCurrentThread(): PlaywrightApi
}
