package io.orangebuffalo.testcontainers.playwright

import mu.KotlinLogging
import org.junit.jupiter.api.Test

var log = KotlinLogging.logger {}

internal class PlaywrightContainerImperativeTest {

    @Test
    fun shouldProvideValidChromiumBrowser() = withContainer {
        getPlaywrightApi()
            .chromium()
            .openAndVerifyTestPage()
            .shouldRunInChromium()
    }

    @Test
    fun shouldProvideValidFirefoxBrowser() = withContainer {
        getPlaywrightApi()
            .firefox()
            .openAndVerifyTestPage()
            .shouldRunInFirefox()
    }

    @Test
    fun shouldProvideValidWebkitBrowser() = withContainer {
        getPlaywrightApi()
            .webkit()
            .openAndVerifyTestPage()
            .shouldRunInWebkit()
    }

    private fun withContainer(block: PlaywrightContainer.() -> Unit) {
        PlaywrightContainer().use { container ->
            container.connectToWebServer()
            container.logConsumers.add { log.info { "[CONTAINER] ${it.utf8String}" } }
            container.start()
            block(container)
        }
    }
}
