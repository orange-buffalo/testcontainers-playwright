package io.orangebuffalo.testcontainers.playwright

import mu.KotlinLogging
import kotlin.test.Test

var log = KotlinLogging.logger {}

internal class PlaywrightContainerImperativeTest {

    @Test
    fun shouldProvideValidChromiumBrowser() = withContainer {
        val playwright = registerNewPlaywright()
        val page = playwright.chromium().openAndVerifyTestPage()
        page.shouldRunInChromium()
    }

    @Test
    fun shouldProvideValidFirefoxBrowser() = withContainer {
        val playwright = registerNewPlaywright()
        val page = playwright.firefox().openAndVerifyTestPage()
        page.shouldRunInFirefox()
    }

    @Test
    fun shouldProvideValidWebkitBrowser() = withContainer {
        val playwright = registerNewPlaywright()
        val page = playwright.webkit().openAndVerifyTestPage()
        page.shouldRunInWebkit()
    }

    private fun withContainer(block: PlaywrightContainer.() -> Unit) {
        PlaywrightContainer().use { container ->
            container.logConsumers.add { log.info { "[CONTAINER] ${it.utf8String}" } }
            container.start()
            block(container)
        }
    }
}
