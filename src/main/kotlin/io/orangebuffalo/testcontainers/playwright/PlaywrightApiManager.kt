package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Selectors
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val log = KotlinLogging.logger {}

/**
 * Manages instances of [PlaywrightContainerApi] per thread, bound to a particular container.
 */
internal class PlaywrightApiManager(
    private val container: PlaywrightContainer,
) {

    private val playwrightApis = ConcurrentHashMap<Long, PlaywrightContainerApiImpl>()

    fun close() {
        playwrightApis.values.forEach { it.close() }
    }

    fun getContainerApi(): PlaywrightContainerApi = playwrightApis.computeIfAbsent(Thread.currentThread().id) {
        val api = PlaywrightContainerApiImpl()
        Runtime.getRuntime().addShutdownHook(Thread {
            api.close()
        })
        api
    }

    /**
     * Per-thread instance, bound to a container
     */
    private inner class PlaywrightContainerApiImpl() : PlaywrightContainerApi {

        private val playwright = Playwright.create()
        private var chromiumBrowser: Browser? = null
        private var firefoxBrowser: Browser? = null
        private var webkitBrowser: Browser? = null

        override fun chromium(): Browser {
            if (chromiumBrowser == null) {
                chromiumBrowser = playwright.chromium().connect(container.getChromiumWsEndpoint())
            }
            return chromiumBrowser!!
        }

        override fun firefox(): Browser {
            if (firefoxBrowser == null) {
                firefoxBrowser = playwright.firefox().connect(container.getFirefoxWsEndpoint())
            }
            return firefoxBrowser!!
        }

        override fun webkit(): Browser {
            if (webkitBrowser == null) {
                webkitBrowser = playwright.webkit().connect(container.getWebkitWsEndpoint())
            }
            return webkitBrowser!!
        }

        override fun selectors(): Selectors = playwright.selectors()

        fun close() {
            chromiumBrowser?.safeClose()
            firefoxBrowser?.safeClose()
            webkitBrowser?.safeClose()
            playwright.safeClose()
        }
    }
}

private fun Browser.safeClose() {
    try {
        close()
    } catch (e: Exception) {
        log.warn(e) { "Failed to close browser" }
    }
}

private fun Playwright.safeClose() {
    try {
        close()
    } catch (e: Exception) {
        log.warn(e) { "Failed to close playwright" }
    }
}



