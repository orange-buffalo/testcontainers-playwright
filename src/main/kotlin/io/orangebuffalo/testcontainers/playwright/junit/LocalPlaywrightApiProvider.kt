package io.orangebuffalo.testcontainers.playwright.junit

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Selectors
import io.orangebuffalo.testcontainers.playwright.PlaywrightApi
import io.orangebuffalo.testcontainers.playwright.safeClose

/**
 * [PlaywrightApiProvider] that creates local browser instances, bypassing Docker containers. Runs browsers
 * in non-headless mode.
 */
class LocalPlaywrightApiProvider : PlaywrightApiProvider {

    private val playwrightApis = ThreadLocal.withInitial {
        val api = LocalPlaywrightApiImpl()
        Runtime.getRuntime().addShutdownHook(Thread {
            api.close()
        })
        api
    }

    override fun getOrCreatePlaywrightApiForCurrentThread(): PlaywrightApi = playwrightApis.get()

    private class LocalPlaywrightApiImpl : PlaywrightApi {

        private val playwright = Playwright.create()
        private var chromiumBrowser: Browser? = null
        private var firefoxBrowser: Browser? = null
        private var webkitBrowser: Browser? = null

        override fun chromium(): Browser {
            if (chromiumBrowser == null) {
                chromiumBrowser = playwright.chromium()
                    .launch(BrowserType.LaunchOptions().setHeadless(false))
            }
            return chromiumBrowser!!
        }

        override fun firefox(): Browser {
            if (firefoxBrowser == null) {
                firefoxBrowser = playwright.firefox()
                    .launch(BrowserType.LaunchOptions().setHeadless(false))
            }
            return firefoxBrowser!!
        }

        override fun webkit(): Browser {
            if (webkitBrowser == null) {
                webkitBrowser = playwright.webkit()
                    .launch(BrowserType.LaunchOptions().setHeadless(false))
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
