package io.orangebuffalo.testcontainers.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Selectors
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages instances of [PlaywrightApi] per thread, bound to a particular container.
 */
internal class PlaywrightApiManager(
    private val container: PlaywrightContainer,
) {

    private val playwrightApis = ConcurrentHashMap<Long, PlaywrightApiImpl>()

    fun close() {
        playwrightApis.values.forEach { it.close() }
    }

    fun getPlaywrightApi(): PlaywrightApi = playwrightApis.computeIfAbsent(Thread.currentThread().id) {
        val api = PlaywrightApiImpl()
        Runtime.getRuntime().addShutdownHook(Thread {
            api.close()
        })
        api
    }

    /**
     * Per-thread instance, bound to a container
     */
    private inner class PlaywrightApiImpl : PlaywrightApi {

        private val playwright = Playwright.create(
            Playwright.CreateOptions()
                // skip browser download as we run against Docker image managed binaries
                .setEnv(mapOf("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD" to "1"))
        )
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
